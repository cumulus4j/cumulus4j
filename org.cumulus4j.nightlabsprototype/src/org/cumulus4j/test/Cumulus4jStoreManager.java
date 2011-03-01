package org.cumulus4j.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.cumulus4j.test.model.ClassMeta;
import org.cumulus4j.test.model.DataEntry;
import org.cumulus4j.test.model.FieldMeta;
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
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Cumulus4jStoreManager
extends AbstractStoreManager
{
	private Map<Class<?>, ClassMeta> class2classMeta = Collections.synchronizedMap(new HashMap<Class<?>, ClassMeta>());

	public Cumulus4jStoreManager(ClassLoaderResolver clr, NucleusContext nucleusContext, Map<String, Object> props)
	{
		super("cumulus4j", clr, nucleusContext, props);

		persistenceHandler = new Cumulus4jPersistenceHandler(this);
	}

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
			pm.getFetchPlan().setGroup(FetchPlan.ALL);
			pm.getFetchPlan().setMaxFetchDepth(-1);
			result = pm.detachCopy(result);

			class2classMeta.put(clazz, result);

			// TODO Later, we should register the super-class' infos here, too, but that's just optimization (not essentially necessary).

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
			FieldMeta fieldMeta = classMeta.getFieldMeta(memberMetaData.getName());
			if (fieldMeta == null) {
				// adding field that's so far unknown
				fieldMeta = new FieldMeta(classMeta, memberMetaData.getType(), memberMetaData.getName());
				classMeta.addFieldMeta(fieldMeta);
			}
			fieldMeta.setDataNucleusAbsoluteFieldNumber(memberMetaData.getAbsoluteFieldNumber());
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

	@Override
	public String getClassNameForObjectID(Object id, ClassLoaderResolver clr, ExecutionContext ec)
	{
		// Copied all except for the application-identity part from the super-implementation.
		// The super-implementation simply returns the first (maybe abstract) class and not necessarily
		// the one that really matches the object referenced by id.

		if (id == null)
        {
            // User stupidity
            return null;
        }
        else if (id instanceof SCOID)
        {
            // Object is a SCOID
            return ((SCOID) id).getSCOClass();
        }
        else if (id instanceof OID)
        {
            // Object is an OID
            return ((OID)id).getPcClass();
        }
        else if (getApiAdapter().isSingleFieldIdentity(id))
        {
            // Using SingleFieldIdentity so can assume that object is of the target class
            return getApiAdapter().getTargetClassNameForSingleFieldIdentity(id);
        }
        else
        {
            // Application identity with user PK class, so find all using this PK
            Collection<AbstractClassMetaData> cmds = getMetaDataManager().getClassMetaDataWithApplicationId(id.getClass().getName());
            if (cmds != null) {
            	if (cmds.size() == 1)
            		return cmds.iterator().next().getFullClassName();

            	ManagedConnection mconn = this.getConnection(ec);
            	try {
            		PersistenceManager pm = (PersistenceManager) mconn.getConnection();
            		String objectIDString = id.toString();
            		for (AbstractClassMetaData cmd : cmds) {
            			Class<?> clazz = clr.classForName(cmd.getFullClassName());
            			ClassMeta classMeta = getClassMeta(ec, clazz);
            			DataEntry dataEntry = DataEntry.getDataEntry(pm, classMeta, objectIDString);
            			if (dataEntry != null)
            				return cmd.getFullClassName();
            		}
        		} finally {
        			mconn.release();
        		}
            }
            return null;
        }
	}
}
