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
import java.util.List;
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
import org.cumulus4j.store.model.DetachedClassMetaModel;
import org.cumulus4j.store.model.EmbeddedClassMeta;
import org.cumulus4j.store.model.EmbeddedFieldMeta;
import org.cumulus4j.store.model.FetchGroupsMetaData;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.FieldMetaDAO;
import org.cumulus4j.store.model.FieldMetaRole;
import org.cumulus4j.store.model.IndexEntryFactoryRegistry;
import org.cumulus4j.store.model.PostDetachRunnableManager;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.ExecutionContext;
import org.datanucleus.NucleusContext;
import org.datanucleus.api.jdo.JDOPersistenceManagerFactory;
import org.datanucleus.identity.OID;
import org.datanucleus.identity.SCOID;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.MapMetaData.MapType;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.store.AbstractStoreManager;
import org.datanucleus.store.Extent;
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
	private Map<Long, ClassMeta> classID2classMeta = Collections.synchronizedMap(new HashMap<Long, ClassMeta>());
	private Map<Long, FieldMeta> fieldID2fieldMeta = Collections.synchronizedMap(new HashMap<Long, FieldMeta>());

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

//	private ThreadLocal<Set<Long>> fieldIDsCurrentlyLoading = new ThreadLocal<Set<Long>>() {
//		@Override
//		protected Set<Long> initialValue() {
//			return new HashSet<Long>();
//		}
//	};

	public FieldMeta getFieldMeta(ExecutionContext ec, long fieldID, boolean throwExceptionIfNotFound) {
		if (ec == null)
			throw new IllegalArgumentException("ec == null");

		if (fieldID < 0)
			throw new IllegalArgumentException("fieldID < 0");

//		if (!fieldIDsCurrentlyLoading.get().add(fieldID)) {
//			if (throwExceptionIfNotFound)
//				throw new IllegalStateException("Circular loading! This is only allowed, if throwExceptionIfNotFound == false and results in null being returned.");
//
//			return null;
//		}
//		try {
			FieldMeta result = fieldID2fieldMeta.get(fieldID);
			if (result != null) {
				logger.trace("getFieldMetaByFieldID: found cache entry. fieldID={}", fieldID);
				return result;
			}

			long beginLoadingTimestamp = System.currentTimeMillis();
			long classID;
			ManagedConnection mconn = this.getConnection(ec);
			try {
				PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
				PersistenceManager pm = pmConn.getDataPM();
				FieldMetaDAO dao = new FieldMetaDAO(pm);
				FieldMeta fieldMeta = dao.getFieldMeta(fieldID, throwExceptionIfNotFound);
				if (fieldMeta == null)
					return null;

				classID = fieldMeta.getClassMeta().getClassID();
			} finally {
				mconn.release(); mconn = null;
			}

			getClassMeta(ec, classID, true);

			result = fieldID2fieldMeta.get(fieldID);
			if (result == null)
				throw new IllegalStateException("Even after loading the class " + classID + " , the field " + fieldID + " is still not cached!");

			logger.debug("getFieldMetaByFieldID: end loading (took {} ms). fieldID={}", System.currentTimeMillis() - beginLoadingTimestamp, fieldID);
			return result;
//		} finally {
//			Set<Long> set = fieldIDsCurrentlyLoading.get();
//			set.remove(fieldID);
//			if (set.isEmpty())
//				fieldIDsCurrentlyLoading.remove();
//		}
	}

	public ClassMeta getClassMeta(ExecutionContext ec, long classID, boolean throwExceptionIfNotFound) {
		if (ec == null)
			throw new IllegalArgumentException("ec == null");

		if (classID < 0)
			throw new IllegalArgumentException("classID < 0");

		ClassMeta result = classID2classMeta.get(classID);
		if (result != null) {
			logger.trace("getClassMetaByClassID: found cache entry. classID={}", classID);
			return result;
		}

		logger.debug("getClassMetaByClassID: begin loading. classID={}", classID);
		long beginLoadingTimestamp = System.currentTimeMillis();
		String className;
		ManagedConnection mconn = this.getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pm = pmConn.getDataPM();
			CryptoContext cryptoContext = new CryptoContext(encryptionCoordinateSetManager, keyStoreRefManager, ec, pmConn);
			datastoreVersionManager.applyOnce(cryptoContext);

			ClassMetaDAO dao = new ClassMetaDAO(pm);
			ClassMeta classMeta = dao.getClassMeta(classID, throwExceptionIfNotFound);
			if (classMeta == null)
				return null;

			className = classMeta.getClassName();
		} finally {
			mconn.release(); mconn = null;
		}

		Class<?> clazz = ec.getClassLoaderResolver().classForName(className, true);

		result = getClassMeta(ec, clazz);

		// This is not necessarily the right result, because getClassMeta(ec, clazz) NEVER returns an EmbeddedClassMeta
		// and the classID might belong to an embeddedClassMeta.
		if (result.getClassID() != classID) {
			result = null;

//			DetachedClassMetaModel.setInstance(new DetachedClassMetaModel() {
//				@Override
//				public ClassMeta getClassMeta(long classID, boolean throwExceptionIfNotFound) {
//					ClassMeta result = classID2classMeta.get(classID);
//					if (result == null && throwExceptionIfNotFound)
//						throw new IllegalArgumentException("No ClassMeta found for classID=" + classID);
//
//					return result;
//				}
//			});
//			try {
				mconn = this.getConnection(ec);
				try {
					PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
					PersistenceManager pm = pmConn.getDataPM();
					ClassMetaDAO dao = new ClassMetaDAO(pm);
					ClassMeta classMeta = dao.getClassMeta(classID, throwExceptionIfNotFound);
					result = detachClassMeta(ec, pm, classMeta);
				} finally {
					mconn.release(); mconn = null;
				}
//			} finally {
//				DetachedClassMetaModel.setInstance(null);
//			}
		}
		logger.debug("getClassMetaByClassID: end loading (took {} ms). classID={}", System.currentTimeMillis() - beginLoadingTimestamp, classID);

		putClassMetaIntoCache(result);
		return result;
	}

	protected void putClassMetaIntoCache(ClassMeta classMeta) {
		if (classMeta == null)
			return;

		classID2classMeta.put(classMeta.getClassID(), classMeta);
		putFieldMetasIntoCache(classMeta);
	}

	protected void putFieldMetasIntoCache(ClassMeta classMeta) {
		if (classMeta == null)
			return;

		putFieldMetasIntoCache(classMeta.getFieldMetas());
	}

	protected void putFieldMetasIntoCache(Collection<FieldMeta> fieldMetas) {
		if (fieldMetas == null)
			return;

		for (FieldMeta fieldMeta : fieldMetas) {
			if (fieldID2fieldMeta.put(fieldMeta.getFieldID(), fieldMeta) != null)
				continue; // already added before => no recursion

			putFieldMetasIntoCache(fieldMeta.getEmbeddedClassMeta());
			putFieldMetasIntoCache(fieldMeta.getSubFieldMetas());
		}
	}

	protected ClassMeta detachClassMeta(final ExecutionContext ec, PersistenceManager pm, ClassMeta classMeta) {
		boolean clearDetachedClassMetaModel = false;
		if (DetachedClassMetaModel.getInstance() == null) {
			clearDetachedClassMetaModel = true;
			DetachedClassMetaModel.setInstance(new DetachedClassMetaModel() {
				private Set<Long> pendingClassIDs = new HashSet<Long>();
				private Set<Long> pendingFieldIDs = new HashSet<Long>();

				@Override
				protected ClassMeta getClassMetaImpl(long classID, boolean throwExceptionIfNotFound) {
					if (!pendingClassIDs.add(classID)) {
						throw new IllegalStateException("Circular detachment of classID=" + classID);
					}
					try {
						ClassMeta result = Cumulus4jStoreManager.this.getClassMeta(ec, classID, throwExceptionIfNotFound);
						return result;
					} finally {
						pendingClassIDs.remove(classID);
					}
				}
				@Override
				protected FieldMeta getFieldMetaImpl(long fieldID, boolean throwExceptionIfNotFound) {
					if (!pendingFieldIDs.add(fieldID)) {
						throw new IllegalStateException("Circular detachment of fieldID=" + fieldID);
					}
					try {
						FieldMeta result = Cumulus4jStoreManager.this.getFieldMeta(ec, fieldID, throwExceptionIfNotFound);
						return result;
					} finally {
						pendingFieldIDs.remove(fieldID);
					}
				}
			});
		}
		try {
			ClassMeta result;
			pm.flush();
			pm.evictAll();
			pm.getFetchPlan().setGroups(FetchPlan.ALL, FetchGroupsMetaData.ALL);
			pm.getFetchPlan().setMaxFetchDepth(-1);
			final PostDetachRunnableManager postDetachRunnableManager = PostDetachRunnableManager.getInstance();
			postDetachRunnableManager.enterScope();
			try {
				result = pm.detachCopy(classMeta);
			} finally {
				postDetachRunnableManager.exitScope();
			}
			return result;
		} finally {
			if (clearDetachedClassMetaModel)
				DetachedClassMetaModel.setInstance(null);
		}
	}

	public List<ClassMeta> getClassMetaWithSubClassMetas(ExecutionContext ec, ClassMeta classMeta) {
		final List<ClassMeta> result = getSubClassMetas(ec, classMeta, true);
//		result.add(0, classMeta);
		result.add(classMeta); // I think, the order does not matter ;-)
		return result;
	}

	public List<ClassMeta> getSubClassMetas(ExecutionContext ec, ClassMeta classMeta, boolean includeDescendents) {
		return getSubClassMetas(ec, classMeta.getClassName(), includeDescendents);
	}

	public List<ClassMeta> getSubClassMetas(ExecutionContext ec, Class<?> clazz, boolean includeDescendents) {
		return getSubClassMetas(ec, clazz.getName(), includeDescendents);
	}

	public List<ClassMeta> getSubClassMetas(ExecutionContext ec, String className, boolean includeDescendents) {
		ClassLoaderResolver clr = ec.getClassLoaderResolver();
		Collection<String> subClassesForClass = getSubClassesForClass(className, includeDescendents, clr);
		List<ClassMeta> result = new ArrayList<ClassMeta>(subClassesForClass.size());
		for (String subClassName : subClassesForClass) {
			Class<?> subClass = clr.classForName(subClassName);
			ClassMeta subClassMeta = getClassMeta(ec, subClass);
			result.add(subClassMeta);
		}
		return result;
	}

	/**
	 * Get the persistent meta-data of a certain class. This persistent meta-data is primarily used for efficient
	 * mapping using long-identifiers instead of fully qualified class names.
	 *
	 * @param ec
	 * @param clazz the {@link Class} for which to query the meta-data. Must not be <code>null</code>.
	 * @return the meta-data. Never returns <code>null</code>.
	 */
	public ClassMeta getClassMeta(ExecutionContext ec, Class<?> clazz)
	{
		if (clazz == null)
			throw new IllegalArgumentException("clazz == null");

		ClassMeta result = class2classMeta.get(clazz);
		if (result != null) {
			logger.trace("getClassMetaByClass: found cache entry. class={}", clazz.getName());
			return result;
		}

		logger.debug("getClassMetaByClass: begin loading. class={}", clazz.getName());
		long beginLoadingTimestamp = System.currentTimeMillis();
		ManagedConnection mconn = this.getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pm = pmConn.getDataPM();

			CryptoContext cryptoContext = new CryptoContext(encryptionCoordinateSetManager, keyStoreRefManager, ec, pmConn);
			datastoreVersionManager.applyOnce(cryptoContext);

			synchronized (this) { // Synchronise in case we have data and index backends // why? what about multiple instances? shouldn't the replication be safe? is this just for lower chance of exceptions (causing a rollback and being harmless)?
				// Register the class
				pm.getFetchPlan().setGroups(FetchPlan.ALL, FetchGroupsMetaData.ALL);
				result = registerClass(ec, pm, clazz);

				// Detach the class in order to cache only detached objects. Make sure fetch-plan detaches all
				result = detachClassMeta(ec, pm, result);

				if (pmConn.indexHasOwnPM()) {
					// Replicate ClassMeta+FieldMeta to Index datastore
					PersistenceManager pmIndex = pmConn.getIndexPM();
					pm.getFetchPlan().setGroups(FetchPlan.ALL, FetchGroupsMetaData.ALL); // not sure, if this is necessary before persisting, but don't have time to find it out - leaving it.
					pmIndex.getFetchPlan().setMaxFetchDepth(-1); // not sure, if this is necessary before persisting, but don't have time to find it out - leaving it.
					result = pmIndex.makePersistent(result);
					result = detachClassMeta(ec, pmIndex, result);
				}
			}

			class2classMeta.put(clazz, result);
			putClassMetaIntoCache(result);

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
					putClassMetaIntoCache(result);
				}
			}
		} finally {
			mconn.release();
		}
		logger.debug("getClassMetaByClass: end loading (took {} ms). class={}", System.currentTimeMillis() - beginLoadingTimestamp, clazz.getName());

		return result;
	}

	public ClassMeta getAttachedClassMeta(ExecutionContext ec, PersistenceManager pm, Class<?> clazz)
	{
		ClassMeta classMeta = new ClassMetaDAO(pm).getClassMeta(clazz, false);
		if (classMeta == null) {
			classMeta = registerClass(ec, pm, clazz);
		}
		return classMeta;
	}

	private ClassMeta registerClass(ExecutionContext ec, PersistenceManager pm, Class<?> clazz)
	{
		logger.debug("registerClass: clazz={}", clazz == null ? null : clazz.getName());
		AbstractClassMetaData dnClassMetaData = getMetaDataManager().getMetaDataForClass(clazz, ec.getClassLoaderResolver());
		if (dnClassMetaData == null)
			throw new IllegalArgumentException("The class " + clazz.getName() + " does not have persistence-meta-data! Is it persistence-capable? Is it enhanced?");

		ClassMeta classMeta = new ClassMetaDAO(pm).getClassMeta(clazz, false);

		List<FieldMeta> primaryFieldMetas = new ArrayList<FieldMeta>();
//		final PostStoreRunnableManager postStoreRunnableManager = PostStoreRunnableManager.getInstance();
//		postStoreRunnableManager.enterScope();
//		try {

			if (classMeta == null) {
				// We need to find this class already, because embedded-handling might be recursive.
				// Additionally, we have our IDs immediately this way and can store long-field-references
				// without any problem.
				classMeta = pm.makePersistent(new ClassMeta(clazz));
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
					//				setEmbeddedClassMeta(ec, subFieldMeta);
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
					//				setEmbeddedClassMeta(ec, subFieldMeta);
				}
				else if (memberMetaData.hasMap()) {
					// register "map" field-meta, if appropriate
					primaryFieldMeta.removeAllSubFieldMetasExcept(FieldMetaRole.mapKey, FieldMetaRole.mapValue);

					// key
					FieldMeta subFieldMeta = primaryFieldMeta.getSubFieldMeta(FieldMetaRole.mapKey);
					if (subFieldMeta == null) {
						// adding field that's so far unknown
						subFieldMeta = new FieldMeta(primaryFieldMeta, FieldMetaRole.mapKey);
						primaryFieldMeta.addSubFieldMeta(subFieldMeta);
					}
					//				setEmbeddedClassMeta(ec, subFieldMeta);

					// value
					subFieldMeta = primaryFieldMeta.getSubFieldMeta(FieldMetaRole.mapValue);
					if (subFieldMeta == null) {
						// adding field that's so far unknown
						subFieldMeta = new FieldMeta(primaryFieldMeta, FieldMetaRole.mapValue);
						primaryFieldMeta.addSubFieldMeta(subFieldMeta);
					}
					//				setEmbeddedClassMeta(ec, subFieldMeta);
				}
				else {
					primaryFieldMeta.removeAllSubFieldMetasExcept();
				}
//				setEmbeddedClassMeta(ec, primaryFieldMeta); // defer due to possible recursion to this method!
				primaryFieldMetas.add(primaryFieldMeta);
			}

			for (FieldMeta fieldMeta : new ArrayList<FieldMeta>(classMeta.getFieldMetas())) {
				if (persistentMemberNames.contains(fieldMeta.getFieldName()))
					continue;

				// The field is not in the class anymore => remove its persistent reference.
				classMeta.removeFieldMeta(fieldMeta);
			}

			pm.flush(); // Get exceptions as soon as possible by forcing a flush here

//		} finally {
//			postStoreRunnableManager.exitScope();
//			pm.flush(); // Get exceptions as soon as possible by forcing a flush here
//		}

//		postStoreRunnableManager.enterScope();
//		try {
			for (FieldMeta primaryFieldMeta : primaryFieldMetas) {
				setEmbeddedClassMeta(ec, primaryFieldMeta);
				pm.flush(); // Get exceptions as soon as possible by forcing a flush here
			}
//		} finally {
//			postStoreRunnableManager.exitScope();
//			pm.flush(); // Get exceptions as soon as possible by forcing a flush here
//		}

		return classMeta;
	}

	private boolean isEmbedded(AbstractMemberMetaData memberMetaData) {
		return isEmbeddedOneToOne(memberMetaData)
				|| isEmbeddedArray(memberMetaData)
				|| isEmbeddedCollection(memberMetaData)
				|| isEmbeddedMap(memberMetaData);
	}

	private boolean isEmbeddedOneToOne(AbstractMemberMetaData memberMetaData) {
		return memberMetaData.isEmbedded();
	}

	private boolean isEmbeddedCollection(AbstractMemberMetaData memberMetaData) {
		return memberMetaData.hasCollection() && memberMetaData.getCollection().isEmbeddedElement();
	}

	private boolean isEmbeddedArray(AbstractMemberMetaData memberMetaData) {
		return memberMetaData.hasArray() && memberMetaData.getArray().isEmbeddedElement();
	}

	private boolean isEmbeddedMap(AbstractMemberMetaData memberMetaData) {
		return memberMetaData.hasMap()
				&& MapType.MAP_TYPE_JOIN.equals(memberMetaData.getMap().getMapType())
				&& (memberMetaData.getMap().isEmbeddedKey() || memberMetaData.getMap().isEmbeddedValue());
	}

	private void setEmbeddedClassMeta(ExecutionContext ec, FieldMeta fieldMeta) {
		AbstractMemberMetaData memberMetaData = fieldMeta.getDataNucleusMemberMetaData(ec);
		if (isEmbedded(memberMetaData)) {
			if (fieldMeta.getSubFieldMetas().isEmpty()) {
				// only assign this to the leafs (map-key, map-value, collection-element, etc.)
				// if we have no sub-field-metas, our fieldMeta is a leaf.
				if (fieldMeta.getEmbeddedClassMeta() == null) {
					ClassMeta fieldOrElementTypeClassMeta = fieldMeta.getFieldOrElementTypeClassMeta(ec);
					if (fieldOrElementTypeClassMeta != null) {
						fieldMeta.setEmbeddedClassMeta(new EmbeddedClassMeta(ec, fieldOrElementTypeClassMeta, fieldMeta));
					}
				}

				if (fieldMeta.getEmbeddedClassMeta() != null)
					updateEmbeddedFieldMetas(ec, fieldMeta);
			}
			else {
				fieldMeta.setEmbeddedClassMeta(null);
				for (FieldMeta subFieldMeta : fieldMeta.getSubFieldMetas()) {
					boolean subEmbedded = true;
					if (memberMetaData.hasMap()) {
						switch (subFieldMeta.getRole()) {
							case mapKey:
								subEmbedded = memberMetaData.getMap().isEmbeddedKey();
								break;
							case mapValue:
								subEmbedded = memberMetaData.getMap().isEmbeddedValue();
								break;
							default:
								throw new IllegalStateException("Unexpected subFieldMeta.role=" + subFieldMeta.getRole());
						}
					}
					if (subEmbedded)
						setEmbeddedClassMeta(ec, subFieldMeta);
					else
						subFieldMeta.setEmbeddedClassMeta(null);
				}
			}
		}
		else {
			fieldMeta.setEmbeddedClassMeta(null);
			for (FieldMeta subFieldMeta : fieldMeta.getSubFieldMetas()) {
				subFieldMeta.setEmbeddedClassMeta(null);
			}
		}
	}

	private void updateEmbeddedFieldMetas(ExecutionContext ec, FieldMeta embeddingFieldMeta)
	{
		EmbeddedClassMeta embeddedClassMeta = embeddingFieldMeta.getEmbeddedClassMeta();

		for (FieldMeta fieldMeta : embeddedClassMeta.getNonEmbeddedClassMeta().getFieldMetas()) {
			EmbeddedFieldMeta embeddedFieldMeta = embeddedClassMeta.getEmbeddedFieldMetaForNonEmbeddedFieldMeta(fieldMeta);
			if (embeddedFieldMeta == null) {
				embeddedFieldMeta = new EmbeddedFieldMeta(embeddedClassMeta, null, fieldMeta);
				embeddedClassMeta.addFieldMeta(embeddedFieldMeta);
			}
			setEmbeddedClassMeta(ec, embeddedFieldMeta);
			updateEmbeddedFieldMetas_subFieldMetas(embeddedClassMeta, fieldMeta, embeddedFieldMeta);
		}
	}

	private void updateEmbeddedFieldMetas_subFieldMetas(EmbeddedClassMeta embeddedClassMeta, FieldMeta fieldMeta, EmbeddedFieldMeta embeddedFieldMeta) {
		for (FieldMeta subFieldMeta : fieldMeta.getSubFieldMetas()) {
			EmbeddedFieldMeta subEmbeddedFieldMeta = embeddedClassMeta.getEmbeddedFieldMetaForNonEmbeddedFieldMeta(subFieldMeta);
			if (subEmbeddedFieldMeta == null) {
				subEmbeddedFieldMeta = new EmbeddedFieldMeta(embeddedClassMeta, embeddedFieldMeta, subFieldMeta);
				embeddedFieldMeta.addSubFieldMeta(subEmbeddedFieldMeta);
			}
			updateEmbeddedFieldMetas_subFieldMetas(embeddedClassMeta, subFieldMeta, subEmbeddedFieldMeta);
		}
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
			(Cumulus4jConnectionFactory) connectionMgr.lookupConnectionFactory(primaryConnectionFactoryName);
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
			(Cumulus4jConnectionFactory) connectionMgr.lookupConnectionFactory(primaryConnectionFactoryName);
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
			(Cumulus4jConnectionFactory) connectionMgr.lookupConnectionFactory(primaryConnectionFactoryName);
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

	@Override
	public Extent getExtent(ExecutionContext ec, @SuppressWarnings("rawtypes") Class c, boolean subclasses) {
		getClassMeta(ec, c); // Ensure, we initialise our meta-data, too.
		return super.getExtent(ec, c, subclasses);
	}

	public void assertReadOnlyForUpdateOfObject(ObjectProvider op) {
		// TODO this method disappeared in DataNucleus 3.2 (it was still present in 3.1). Need to find out how to replace it!
	}
}