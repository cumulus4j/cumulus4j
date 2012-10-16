/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.WeakHashMap;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.datastoreversion.DatastoreVersionManager;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.ClassMetaDAO;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.DataEntryDAO;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.FieldMetaRole;
import org.cumulus4j.store.model.IndexEntryFactoryRegistry;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.NucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.identity.OID;
import org.datanucleus.identity.SCOID;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.AbstractStoreManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.schema.SchemaAwareStoreManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Store Manager for Cumulus4J operation.
 * This StoreManager handles a backend StoreManager for the persistence to the chosen datastore, and optionally
 * a second backend StoreManager for the persistence of index data to the chosen index datastore.
 * The user will persist objects of their own classes, and these will be translated into the persistence of
 * DataEntry, ClassMeta, FieldMeta for the data, as well as various IndexXXX types.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Cumulus4jStoreManager extends AbstractStoreManager implements SchemaAwareStoreManager
{
	private static final Logger logger = LoggerFactory.getLogger(Cumulus4jStoreManager.class);

	/** Extension key for marking field as not queryable */
	public static final String CUMULUS4J_QUERYABLE = "cumulus4j-queryable";

//	private static final SequenceMetaData SEQUENCE_META_DATA_DATA_ENTRY;
//	static {
//		SEQUENCE_META_DATA_DATA_ENTRY = new SequenceMetaData(DataEntry.class.getName(), SequenceStrategy.NONTRANSACTIONAL.toString());
//		SEQUENCE_META_DATA_DATA_ENTRY.setAllocationSize(100);
//		SEQUENCE_META_DATA_DATA_ENTRY.setDatastoreSequence(DataEntry.class.getName());
//	}

	private Map<Class<?>, ClassMeta> class2classMeta = Collections.synchronizedMap(new HashMap<Class<?>, ClassMeta>());

	/**
	 * For every class, we keep a set of all known sub-classes (all inheritance-levels down). Note, that the class in
	 * the map-key is contained in the Set (in the map-value).
	 */
	private Map<Class<?>, Set<Class<?>>> class2subclasses = Collections.synchronizedMap(new HashMap<Class<?>, Set<Class<?>>>());

	private EncryptionHandler encryptionHandler;
	private EncryptionCoordinateSetManager encryptionCoordinateSetManager;
	private KeyStoreRefManager keyStoreRefManager;
	private DatastoreVersionManager datastoreVersionManager = new DatastoreVersionManager(this);

	private IndexEntryFactoryRegistry indexFactoryRegistry;

	public Cumulus4jStoreManager(ClassLoaderResolver clr, NucleusContext nucleusContext, Map<String, Object> props)
	{
		super("cumulus4j", clr, nucleusContext, props);

		logger.info("====================== Cumulus4j ======================");
		String bundleName = "org.cumulus4j.store";
		String version = nucleusContext.getPluginManager().getVersionForBundle(bundleName);
		logger.info("Bundle: " + bundleName + " - Version: " + version);
		logger.info("=======================================================");

		encryptionHandler = new EncryptionHandler();
		encryptionCoordinateSetManager = new EncryptionCoordinateSetManager();
		keyStoreRefManager = new KeyStoreRefManager();
		persistenceHandler = new Cumulus4jPersistenceHandler(this);
	}

	public EncryptionHandler getEncryptionHandler() {
		return encryptionHandler;
	}

	public EncryptionCoordinateSetManager getEncryptionCoordinateSetManager() {
		return encryptionCoordinateSetManager;
	}

	public KeyStoreRefManager getKeyStoreRefManager() {
		return keyStoreRefManager;
	}

	public IndexEntryFactoryRegistry getIndexFactoryRegistry() {
		if (indexFactoryRegistry == null)
			indexFactoryRegistry = new IndexEntryFactoryRegistry(this);

		return indexFactoryRegistry;
	}

	public DatastoreVersionManager getDatastoreVersionManager() {
		return datastoreVersionManager;
	}

	/**
	 * Get the persistent meta-data of a certain class. This persistent meta-data is primarily used for efficient
	 * mapping using long-identifiers instead of fully qualified class names.
	 *
	 * @param ec
	 * @param clazz the {@link Class} for which to query the meta-data.
	 * @return the meta-data. Never returns <code>null</code>.
	 */
	public ClassMeta getClassMeta(ExecutionContext ec, Class<?> clazz)
	{
		ClassMeta result = class2classMeta.get(clazz);
		if (result != null) {
			return result;
		}

		ManagedConnection mconn = this.getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pm = pmConn.getDataPM();

			synchronized (this) { // Synchronise in case we have data and index backends // why? what about multiple instances? shouldn't the replication be safe? is this just for lower chance of exceptions (causing a rollback and being harmless)?
				// Register the class
				pm.getFetchPlan().setGroup(FetchPlan.ALL);
				result = registerClass(ec, pm, clazz);

				// Detach the class in order to cache only detached objects. Make sure fetch-plan detaches all
				pm.getFetchPlan().setGroup(FetchPlan.ALL);
				pm.getFetchPlan().setMaxFetchDepth(-1);
				result = pm.detachCopy(result);

				if (pmConn.indexHasOwnPM()) {
					// Replicate ClassMeta+FieldMeta to Index datastore
					PersistenceManager pmIndex = pmConn.getIndexPM();
					pmIndex.getFetchPlan().setGroup(FetchPlan.ALL);
					pmIndex.getFetchPlan().setMaxFetchDepth(-1);
					pmIndex.makePersistent(result);
				}
			}

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

	private ClassMeta registerClass(ExecutionContext ec, PersistenceManager pm, Class<?> clazz)
	{
		logger.debug("registerClass: clazz={}", clazz == null ? null : clazz.getName());
		AbstractClassMetaData dnClassMetaData = getMetaDataManager().getMetaDataForClass(clazz, ec.getClassLoaderResolver());
		if (dnClassMetaData == null)
			throw new IllegalArgumentException("The class " + clazz.getName() + " does not have persistence-meta-data! Is it persistence-capable? Is it enhanced?");

		ClassMeta classMeta = new ClassMetaDAO(pm).getClassMeta(clazz, false);
		boolean classExists = (classMeta != null);
		if (!classExists) {
			classMeta = new ClassMeta(clazz);
		}

		Class<?> superclass = clazz.getSuperclass();
		if (superclass != null && getMetaDataManager().hasMetaDataForClass(superclass.getName())) {
			ClassMeta superClassMeta = registerClass(ec, pm, superclass);
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
					subFieldMeta = new FieldMeta(primaryFieldMeta, FieldMetaRole.collectionElement);
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
					subFieldMeta = new FieldMeta(primaryFieldMeta, FieldMetaRole.arrayElement);
					primaryFieldMeta.addSubFieldMeta(subFieldMeta);
				}
			}
			else if (memberMetaData.hasMap()) {
				// register "map" field-meta, if appropriate
				primaryFieldMeta.removeAllSubFieldMetasExcept(FieldMetaRole.mapKey, FieldMetaRole.mapValue);

				FieldMeta subFieldMeta = primaryFieldMeta.getSubFieldMeta(FieldMetaRole.mapKey);
				if (subFieldMeta == null) {
					// adding field that's so far unknown
					subFieldMeta = new FieldMeta(primaryFieldMeta, FieldMetaRole.mapKey);
					primaryFieldMeta.addSubFieldMeta(subFieldMeta);
				}

				subFieldMeta = primaryFieldMeta.getSubFieldMeta(FieldMetaRole.mapValue);
				if (subFieldMeta == null) {
					// adding field that's so far unknown
					subFieldMeta = new FieldMeta(primaryFieldMeta, FieldMetaRole.mapValue);
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

		if (!classExists) {
			// Persist the new class and its fields in one call, minimising updates
			classMeta = pm.makePersistent(classMeta);
		}
		pm.flush(); // Get exceptions as soon as possible by forcing a flush here

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
		if (id == null) {
			return null;
		}

		String className = objectID2className.get(id);
		if (className != null) {
			return className;
		}

		if (id instanceof SCOID) {
			// Object is a SCOID
			className = ((SCOID) id).getSCOClass();
		}
		else if (id instanceof OID) {
			// Object is an OID
			className = ((OID)id).getPcClass();
		}
		else if (getApiAdapter().isSingleFieldIdentity(id)) {
			// Using SingleFieldIdentity so can assume that object is of the target class
			className = getApiAdapter().getTargetClassNameForSingleFieldIdentity(id);
		}
		else {
			// Application identity with user PK class, so find all using this PK
			Collection<AbstractClassMetaData> cmds = getMetaDataManager().getClassMetaDataWithApplicationId(id.getClass().getName());
			if (cmds != null) {
				if (cmds.size() == 1) {
					className = cmds.iterator().next().getFullClassName();
				}
				else {
					ManagedConnection mconn = this.getConnection(ec);
					try {
						PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
						PersistenceManager pmData = pmConn.getDataPM();
						CryptoContext cryptoContext = new CryptoContext(encryptionCoordinateSetManager, keyStoreRefManager, ec, pmConn);
						datastoreVersionManager.applyOnce(cryptoContext);

						String objectIDString = id.toString();
						for (AbstractClassMetaData cmd : cmds) {
							Class<?> clazz = clr.classForName(cmd.getFullClassName());
							ClassMeta classMeta = getClassMeta(ec, clazz);
							DataEntry dataEntry = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntry(classMeta, objectIDString);
							if (dataEntry != null) {
								className = cmd.getFullClassName();
							}
						}
					} finally {
						mconn.release();
					}
				}
			}
		}

		if (className != null) {
			objectID2className.put(id, className);
		}

		return className;
	}

//	public long nextDataEntryID(ExecutionContext executionContext)
//	{
//		NucleusSequence nucleusSequence = getNucleusSequence(executionContext, SEQUENCE_META_DATA_DATA_ENTRY);
//		return nucleusSequence.nextValue();
//	}

//	@Override
//	protected String getStrategyForNative(AbstractClassMetaData cmd, int absFieldNumber) {
//		return "increment";
////		AbstractMemberMetaData mmd = cmd.getMetaDataForManagedMemberAtAbsolutePosition(absFieldNumber);
////		if (String.class.isAssignableFrom(mmd.getType()) || UUID.class.isAssignableFrom(mmd.getType()))
////			return "uuid-hex";
////		else
////			return "increment";
//	}

	@Override
	public void createSchema(Set<String> classNames, Properties props) {
		Cumulus4jConnectionFactory cf =
			(Cumulus4jConnectionFactory) connectionMgr.lookupConnectionFactory(txConnectionFactoryName);
		JDOPersistenceManagerFactory pmfData = (JDOPersistenceManagerFactory) cf.getPMFData();
		JDOPersistenceManagerFactory pmfIndex = (JDOPersistenceManagerFactory) cf.getPMFIndex();
		if (pmfData.getNucleusContext().getStoreManager() instanceof SchemaAwareStoreManager) {
			// Create Cumulus4J "Data" (plus "Index" if not separate) schema
			SchemaAwareStoreManager schemaMgr = (SchemaAwareStoreManager) pmfData.getNucleusContext().getStoreManager();
			Set<String> cumulus4jClassNames = new HashSet<String>();
			Collection<Class> pmfClasses = pmfData.getManagedClasses();
			for (Class cls : pmfClasses) {
				cumulus4jClassNames.add(cls.getName());
			}
			schemaMgr.createSchema(cumulus4jClassNames, new Properties());
		}
		if (pmfIndex != null && pmfIndex.getNucleusContext().getStoreManager() instanceof SchemaAwareStoreManager) {
			// Create Cumulus4J "Index" schema
			SchemaAwareStoreManager schemaMgr = (SchemaAwareStoreManager) pmfIndex.getNucleusContext().getStoreManager();
			Set<String> cumulus4jClassNames = new HashSet<String>();
			Collection<Class> pmfClasses = pmfIndex.getManagedClasses();
			for (Class cls : pmfClasses) {
				cumulus4jClassNames.add(cls.getName());
			}
			schemaMgr.createSchema(cumulus4jClassNames, new Properties());
		}
	}

	@Override
	public void deleteSchema(Set<String> classNames, Properties props) {
		Cumulus4jConnectionFactory cf =
			(Cumulus4jConnectionFactory) connectionMgr.lookupConnectionFactory(txConnectionFactoryName);
		JDOPersistenceManagerFactory pmfData = (JDOPersistenceManagerFactory) cf.getPMFData();
		JDOPersistenceManagerFactory pmfIndex = (JDOPersistenceManagerFactory) cf.getPMFIndex();
		if (pmfData.getNucleusContext().getStoreManager() instanceof SchemaAwareStoreManager) {
			SchemaAwareStoreManager schemaMgr = (SchemaAwareStoreManager) pmfData.getNucleusContext().getStoreManager();
			Set<String> cumulus4jClassNames = new HashSet<String>();
			Collection<Class> pmfClasses = pmfData.getManagedClasses();
			for (Class cls : pmfClasses) {
				cumulus4jClassNames.add(cls.getName());
			}
			schemaMgr.deleteSchema(cumulus4jClassNames, new Properties());
		}
		if (pmfIndex != null && pmfIndex.getNucleusContext().getStoreManager() instanceof SchemaAwareStoreManager) {
			SchemaAwareStoreManager schemaMgr = (SchemaAwareStoreManager) pmfIndex.getNucleusContext().getStoreManager();
			Set<String> cumulus4jClassNames = new HashSet<String>();
			Collection<Class> pmfClasses = pmfIndex.getManagedClasses();
			for (Class cls : pmfClasses) {
				cumulus4jClassNames.add(cls.getName());
			}
			schemaMgr.deleteSchema(cumulus4jClassNames, new Properties());
		}
	}

	@Override
	public void validateSchema(Set<String> classNames, Properties props) {
		Cumulus4jConnectionFactory cf =
			(Cumulus4jConnectionFactory) connectionMgr.lookupConnectionFactory(txConnectionFactoryName);
		JDOPersistenceManagerFactory pmfData = (JDOPersistenceManagerFactory) cf.getPMFData();
		JDOPersistenceManagerFactory pmfIndex = (JDOPersistenceManagerFactory) cf.getPMFIndex();
		if (pmfData.getNucleusContext().getStoreManager() instanceof SchemaAwareStoreManager) {
			SchemaAwareStoreManager schemaMgr = (SchemaAwareStoreManager) pmfData.getNucleusContext().getStoreManager();
			Set<String> cumulus4jClassNames = new HashSet<String>();
			Collection<Class> pmfClasses = pmfData.getManagedClasses();
			for (Class cls : pmfClasses) {
				cumulus4jClassNames.add(cls.getName());
			}
			schemaMgr.validateSchema(cumulus4jClassNames, new Properties());
		}
		if (pmfIndex != null && pmfIndex.getNucleusContext().getStoreManager() instanceof SchemaAwareStoreManager) {
			SchemaAwareStoreManager schemaMgr = (SchemaAwareStoreManager) pmfIndex.getNucleusContext().getStoreManager();
			Set<String> cumulus4jClassNames = new HashSet<String>();
			Collection<Class> pmfClasses = pmfIndex.getManagedClasses();
			for (Class cls : pmfClasses) {
				cumulus4jClassNames.add(cls.getName());
			}
			schemaMgr.validateSchema(cumulus4jClassNames, new Properties());
		}
	}

	@Override
	public Cumulus4jPersistenceHandler getPersistenceHandler() {
		return (Cumulus4jPersistenceHandler) super.getPersistenceHandler();
	}

	@Override
	public boolean isStrategyDatastoreAttributed(AbstractClassMetaData cmd, int absFieldNumber) {
		// We emulate all strategies via our Cumulus4jIncrementGenerator - none is really datastore-attributed.
		return false;
	}
}