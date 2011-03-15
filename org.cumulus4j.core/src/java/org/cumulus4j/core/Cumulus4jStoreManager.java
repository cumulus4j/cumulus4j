package org.cumulus4j.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.FieldMetaRole;
import org.cumulus4j.core.model.IndexEntryFactoryRegistry;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.NucleusContext;
import org.datanucleus.identity.OID;
import org.datanucleus.identity.SCOID;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.AbstractStoreManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.connection.ManagedConnection;

/**
 * TODO Support one StoreManager for persistable objects and one StoreManager for indexed data
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Cumulus4jStoreManager
extends AbstractStoreManager
{
	private Map<Class<?>, ClassMeta> class2classMeta = Collections.synchronizedMap(new HashMap<Class<?>, ClassMeta>());

	/**
	 * For every class, we keep a set of all known sub-classes (all inheritance-levels down). Note, that the class in
	 * the map-key is contained in the Set (in the map-value).
	 */
	private Map<Class<?>, Set<Class<?>>> class2subclasses = Collections.synchronizedMap(new HashMap<Class<?>, Set<Class<?>>>());

	private EncryptionHandler encryptionHandler;

	public Cumulus4jStoreManager(ClassLoaderResolver clr, NucleusContext nucleusContext, Map<String, Object> props)
	{
		super("cumulus4j", clr, nucleusContext, props);

		IndexEntryFactoryRegistry.createSharedInstance(this);
		encryptionHandler = new EncryptionHandler();
		persistenceHandler = new Cumulus4jPersistenceHandler(this);
	}

	public EncryptionHandler getEncryptionHandler() {
		return encryptionHandler;
	}

	/**
	 * Get the persistent meta-data of a certain class. This persistent meta-data is primarily used for efficient
	 * mapping using long-identifiers instead of fully qualified class names.
	 *
	 * @param executionContext
	 * @param clazz the {@link Class} for which to query the meta-data.
	 * @return the meta-data. Never returns <code>null</code>.
	 */
	public ClassMeta getClassMeta(ExecutionContext executionContext, Class<?> clazz)
	{
		ClassMeta result = class2classMeta.get(clazz);
		if (result != null)
			return result;

		ManagedConnection mconn = this.getConnection(executionContext);
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();
			pm.getFetchPlan().setGroup(FetchPlan.ALL);
			result = registerClass(executionContext, pm, clazz);

			// We set the fetch-plan again, just in case registerClass modified it.
			pm.getFetchPlan().setGroup(FetchPlan.ALL);
			pm.getFetchPlan().setMaxFetchDepth(-1);

			// Detach in order to cache only detached objects.
			result = pm.detachCopy(result);

			class2classMeta.put(clazz, result);

			// register in class2subclasses-map
			Set<Class<?>> currentSubclasses = new HashSet<Class<?>>();
			Class<?> c = clazz;
			ClassMeta cm = result;
			while (cm != null) {
				currentSubclasses.add(c);

				Set<Class<?>> subclasses;
				synchronized (class2subclasses) {
					subclasses = class2subclasses.get(c);
					if (subclasses == null) {
						subclasses = Collections.synchronizedSet(new HashSet<Class<?>>());
						class2subclasses.put(c, subclasses);
					}
				}

				subclasses.addAll(currentSubclasses);

				c = c.getSuperclass();
				cm = cm.getSuperClassMeta();
				if (cm != null) {
					if (c == null)
						throw new IllegalStateException("c == null && cm.className == " + cm.getClassName());

					if (!cm.getClassName().equals(c.getName()))
						throw new IllegalStateException("cm.className != c.name :: cm.className=" + cm.getClassName() + " c.name=" + c.getName());

					// Store the super-class-meta-data for optimisation reasons (not necessary, but [hopefully] better).
					class2classMeta.put(c, cm);
				}
			}
		} finally {
			mconn.release();
		}

		return result;
	}

	private ClassMeta registerClass(ExecutionContext executionContext, PersistenceManager pm, Class<?> clazz)
	{
		AbstractClassMetaData dnClassMetaData = getMetaDataManager().getMetaDataForClass(clazz, executionContext.getClassLoaderResolver());
		if (dnClassMetaData == null)
			throw new IllegalArgumentException("The class " + clazz.getName() + " does not have persistence-meta-data! Is it persistence-capable? Is it enhanced?");

		ClassMeta classMeta = ClassMeta.getClassMeta(pm, clazz, false);
		if (classMeta == null) {
			classMeta = new ClassMeta(clazz);
			classMeta = pm.makePersistent(classMeta);
			pm.flush(); // Get exceptions as soon as possible by forcing a flush already here.
		}

		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null && getMetaDataManager().hasMetaDataForClass(superclass.getName())) {
			ClassMeta superClassMeta = registerClass(executionContext, pm, superclass);
			classMeta.setSuperClassMeta(superClassMeta);
		}

		Set<String> persistentMemberNames = new HashSet<String>();
		for (AbstractMemberMetaData memberMetaData : dnClassMetaData.getManagedMembers()) {
			if (!memberMetaData.isFieldToBePersisted())
				continue;

			persistentMemberNames.add(memberMetaData.getName());
			int dnAbsoluteFieldNumber = memberMetaData.getAbsoluteFieldNumber();

			// register primary field-meta
			FieldMeta primaryFieldMeta = classMeta.getFieldMeta(memberMetaData.getName());
			if (primaryFieldMeta == null) {
				// adding field that's so far unknown
				primaryFieldMeta = new FieldMeta(classMeta, memberMetaData.getName());
				classMeta.addFieldMeta(primaryFieldMeta);
			}
			primaryFieldMeta.setDataNucleusAbsoluteFieldNumber(dnAbsoluteFieldNumber);

			if (memberMetaData.hasCollection()) {
				// register "collection" field-meta, if appropriate
				primaryFieldMeta.removeAllSubFieldMetasExcept(FieldMetaRole.collectionElement);
				FieldMeta subFieldMeta = primaryFieldMeta.getSubFieldMeta(FieldMetaRole.collectionElement);
				if (subFieldMeta == null) {
					// adding field that's so far unknown
					subFieldMeta = new FieldMeta(primaryFieldMeta, memberMetaData.getName(), FieldMetaRole.collectionElement);
					primaryFieldMeta.addSubFieldMeta(subFieldMeta);
				}
			}
			else if (memberMetaData.hasArray()) {
				// register "array" field-meta, if appropriate
				// TODO shouldn't we handle it exactly as a collection, including reusing 'FieldMetaRole.collectionElement' for this case?
				primaryFieldMeta.removeAllSubFieldMetasExcept(FieldMetaRole.arrayElement);
				FieldMeta subFieldMeta = primaryFieldMeta.getSubFieldMeta(FieldMetaRole.arrayElement);
				if (subFieldMeta == null) {
					// adding field that's so far unknown
					subFieldMeta = new FieldMeta(primaryFieldMeta, memberMetaData.getName(), FieldMetaRole.arrayElement);
					primaryFieldMeta.addSubFieldMeta(subFieldMeta);
				}
			}
			else if (memberMetaData.hasMap()) {
				// register "map" field-meta, if appropriate
				primaryFieldMeta.removeAllSubFieldMetasExcept(FieldMetaRole.mapKey, FieldMetaRole.mapValue);

				FieldMeta subFieldMeta = primaryFieldMeta.getSubFieldMeta(FieldMetaRole.mapKey);
				if (subFieldMeta == null) {
					// adding field that's so far unknown
					subFieldMeta = new FieldMeta(primaryFieldMeta, memberMetaData.getName(), FieldMetaRole.mapKey);
					primaryFieldMeta.addSubFieldMeta(subFieldMeta);
				}

				subFieldMeta = primaryFieldMeta.getSubFieldMeta(FieldMetaRole.mapValue);
				if (subFieldMeta == null) {
					// adding field that's so far unknown
					subFieldMeta = new FieldMeta(primaryFieldMeta, memberMetaData.getName(), FieldMetaRole.mapValue);
					primaryFieldMeta.addSubFieldMeta(subFieldMeta);
				}
			}
			else
				primaryFieldMeta.removeAllSubFieldMetasExcept();
		}

		for (FieldMeta fieldMeta : new ArrayList<FieldMeta>(classMeta.getFieldMetas())) {
			if (persistentMemberNames.contains(fieldMeta.getFieldName()))
				continue;

			// The field is not in the class anymore => remove its persistent reference.
			classMeta.removeFieldMeta(fieldMeta);
		}

		pm.flush(); // Get exceptions as soon as possible by forcing a flush already here.
		return classMeta;
	}

	private Map<Object, String> objectID2className = Collections.synchronizedMap(new WeakHashMap<Object, String>());

	/**
	 * Store the association between an objectID and the class-name of the corresponding persistable object in
	 * a {@link WeakHashMap}. This is used for performance optimization of
	 * {@link #getClassNameForObjectID(Object, ClassLoaderResolver, ExecutionContext)}.
	 */
	public void setClassNameForObjectID(Object id, String className)
	{
		objectID2className.put(id, className);
	}

	@Override
	public String getClassNameForObjectID(Object id, ClassLoaderResolver clr, ExecutionContext ec)
	{
		if (id == null)
			return null;

		String className = objectID2className.get(id);
		if (className != null)
			return className;

		if (id instanceof SCOID)
		{
			// Object is a SCOID
			className = ((SCOID) id).getSCOClass();
		}
		else if (id instanceof OID)
		{
			// Object is an OID
			className = ((OID)id).getPcClass();
		}
		else if (getApiAdapter().isSingleFieldIdentity(id))
		{
			// Using SingleFieldIdentity so can assume that object is of the target class
			className = getApiAdapter().getTargetClassNameForSingleFieldIdentity(id);
		}
		else
		{
			// Application identity with user PK class, so find all using this PK
			Collection<AbstractClassMetaData> cmds = getMetaDataManager().getClassMetaDataWithApplicationId(id.getClass().getName());
			if (cmds != null) {
				if (cmds.size() == 1)
					className = cmds.iterator().next().getFullClassName();
				else {
					ManagedConnection mconn = this.getConnection(ec);
					try {
						PersistenceManager pm = (PersistenceManager) mconn.getConnection();
						String objectIDString = id.toString();
						for (AbstractClassMetaData cmd : cmds) {
							Class<?> clazz = clr.classForName(cmd.getFullClassName());
							ClassMeta classMeta = getClassMeta(ec, clazz);
							DataEntry dataEntry = DataEntry.getDataEntry(pm, classMeta, objectIDString);
							if (dataEntry != null)
								className = cmd.getFullClassName();
						}
					} finally {
						mconn.release();
					}
				}
			}
		}

		if (className != null)
			objectID2className.put(id, className);

		return className;
	}

	@Override
	protected String getStrategyForNative(AbstractClassMetaData cmd, int absFieldNumber) {
		return "increment";
//		AbstractMemberMetaData mmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(absFieldNumber);
//		if (String.class.isAssignableFrom(mmd.getType()) || UUID.class.isAssignableFrom(mmd.getType()))
//			return "uuid-hex";
//		else
//			return "increment";
	}
}
