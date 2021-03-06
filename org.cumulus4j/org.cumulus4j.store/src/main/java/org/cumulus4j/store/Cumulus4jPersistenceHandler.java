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

import java.util.Arrays;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.fieldmanager.FetchFieldManager;
import org.cumulus4j.store.fieldmanager.StoreFieldManager;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.DataEntryDAO;
import org.cumulus4j.store.model.EmbeddedClassMeta;
import org.cumulus4j.store.model.EmbeddedFieldMeta;
import org.cumulus4j.store.model.EmbeddedObjectContainer;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.ObjectContainer;
import org.datanucleus.ExecutionContext;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.state.ObjectProvider;
import org.datanucleus.store.AbstractPersistenceHandler;
import org.datanucleus.store.connection.ManagedConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for all persistence calls from the StoreManager, communicating with the backend datastore(s).
 * Manages all inserts/updates/deletes/fetches/locates of the users own objects and translates them
 * into inserts/updates/deletes/fetches/locates of Cumulus4J model objects.
 */
public class Cumulus4jPersistenceHandler extends AbstractPersistenceHandler
{
	private static final Logger logger = LoggerFactory.getLogger(Cumulus4jPersistenceHandler.class);

	private Cumulus4jStoreManager storeManager;
	private EncryptionCoordinateSetManager encryptionCoordinateSetManager;
	private KeyStoreRefManager keyStoreRefManager;
	private EncryptionHandler encryptionHandler;

	private IndexEntryAction addIndexEntryAction;
	private IndexEntryAction removeIndexEntryAction;

	private static <T> T assertNotNull(String objectName, T object) {
		if (object == null)
			throw new IllegalArgumentException(objectName + " == null");

		return object;
	}

	public Cumulus4jPersistenceHandler(Cumulus4jStoreManager storeManager) {
		super(assertNotNull("storeManager", storeManager));
		this.storeManager = storeManager;
		this.encryptionCoordinateSetManager = storeManager.getEncryptionCoordinateSetManager();
		this.keyStoreRefManager = storeManager.getKeyStoreRefManager();
		this.encryptionHandler = storeManager.getEncryptionHandler();

		this.addIndexEntryAction = new IndexEntryAction.Add(this);
		this.removeIndexEntryAction = new IndexEntryAction.Remove(this);
	}

	public Cumulus4jStoreManager getStoreManager() {
		return storeManager;
	}

	@Override
	public void close() {
		// No resources require to be closed here.
	}

	@Override
	public void deleteObject(ObjectProvider op) {
		// Check if read-only so update not permitted
		storeManager.assertReadOnlyForUpdateOfObject(op);

		ExecutionContext ec = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pmData = pmConn.getDataPM();
			CryptoContext cryptoContext = new CryptoContext(encryptionCoordinateSetManager, keyStoreRefManager, ec, pmConn);
			getStoreManager().getDatastoreVersionManager().applyOnce(cryptoContext);

			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			final ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());
			DataEntry dataEntry = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntry(classMeta, objectIDString);
			//			if (dataEntry == null)
			//				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			if (dataEntry != null) {
				// decrypt object-container in order to identify index entries for deletion
				ObjectContainer objectContainer = encryptionHandler.decryptDataEntry(cryptoContext, dataEntry);
				if (objectContainer != null) {
					AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), ec.getClassLoaderResolver());

					deleteObjectIndex(cryptoContext, classMeta, dataEntry, objectContainer, dnClassMetaData);
				}
				pmData.deletePersistent(dataEntry);
			}

		} finally {
			mconn.release();
		}
	}

	protected void deleteObjectIndex(
			CryptoContext cryptoContext, final ClassMeta classMeta, DataEntry dataEntry,
			ObjectContainer objectContainer, AbstractClassMetaData dnClassMetaData
	)
	{
		for (Map.Entry<Long, ?> me : objectContainer.getFieldID2value().entrySet()) {
			long fieldID = me.getKey();
			Object fieldValue = me.getValue();
			FieldMeta fieldMeta = classMeta.getFieldMeta(fieldID);
			deleteObjectIndex(cryptoContext, classMeta, dataEntry, fieldMeta, fieldValue);
		}
	}

	protected void deleteObjectIndex(
			CryptoContext cryptoContext, ClassMeta classMeta, DataEntry dataEntry,
			FieldMeta fieldMeta, EmbeddedObjectContainer embeddedObjectContainer
	)
	{
		ClassMeta embeddedClassMeta = storeManager.getClassMeta(cryptoContext.getExecutionContext(), embeddedObjectContainer.getClassID(), true);
		EmbeddedClassMeta ecm = (EmbeddedClassMeta) embeddedClassMeta;
		for (Map.Entry<Long, ?> me : embeddedObjectContainer.getFieldID2value().entrySet()) {
			long embeddedFieldID = me.getKey();
			Object embeddedFieldValue = me.getValue();
			EmbeddedFieldMeta embeddedFieldMeta = (EmbeddedFieldMeta) ecm.getFieldMeta(embeddedFieldID);
			deleteObjectIndex(cryptoContext, embeddedClassMeta, dataEntry, embeddedFieldMeta, embeddedFieldValue);
		}
	}

	protected void deleteObjectIndex(
			CryptoContext cryptoContext, ClassMeta classMeta, DataEntry dataEntry,
			FieldMeta fieldMeta, Object fieldValue
	)
	{
		if (fieldValue instanceof EmbeddedObjectContainer) {
			EmbeddedObjectContainer embeddedObjectContainer = (EmbeddedObjectContainer) fieldValue;
			deleteObjectIndex(cryptoContext, classMeta, dataEntry, fieldMeta, embeddedObjectContainer);
		}
		else if (fieldValue instanceof EmbeddedObjectContainer[]) {
			EmbeddedObjectContainer[] embeddedObjectContainers = (EmbeddedObjectContainer[]) fieldValue;
			for (EmbeddedObjectContainer embeddedObjectContainer : embeddedObjectContainers) {
				if (embeddedObjectContainer != null)
					deleteObjectIndex(cryptoContext, classMeta, dataEntry, fieldMeta, embeddedObjectContainer);
			}
		}
		else {
//			AbstractMemberMetaData dnMemberMetaData = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(fieldMeta.getDataNucleusAbsoluteFieldNumber());
			AbstractMemberMetaData dnMemberMetaData = fieldMeta.getDataNucleusMemberMetaData(cryptoContext.getExecutionContext());

			// sanity checks
			if (dnMemberMetaData == null)
				throw new IllegalStateException("dnMemberMetaData == null!!! class == \"" + classMeta.getClassName() + "\" fieldMeta.dataNucleusAbsoluteFieldNumber == " + fieldMeta.getDataNucleusAbsoluteFieldNumber() + " fieldMeta.fieldName == \"" + fieldMeta.getFieldName() + "\"");

			if (!fieldMeta.getFieldName().equals(dnMemberMetaData.getName()))
				throw new IllegalStateException("Meta data inconsistency!!! class == \"" + classMeta.getClassName() + "\" fieldMeta.dataNucleusAbsoluteFieldNumber == " + fieldMeta.getDataNucleusAbsoluteFieldNumber() + " fieldMeta.fieldName == \"" + fieldMeta.getFieldName() + "\" != dnMemberMetaData.name == \"" + dnMemberMetaData.getName() + "\"");

			removeIndexEntryAction.perform(cryptoContext, dataEntry.getDataEntryID(), fieldMeta, dnMemberMetaData, classMeta, fieldValue);
		}
	}

	@Override
	public void fetchObject(ObjectProvider op, int[] fieldNumbers)
	{
		ExecutionContext ec = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pmData = pmConn.getDataPM();
			CryptoContext cryptoContext = new CryptoContext(encryptionCoordinateSetManager, keyStoreRefManager, ec, pmConn);
			getStoreManager().getDatastoreVersionManager().applyOnce(cryptoContext);

			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			final ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());
			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), ec.getClassLoaderResolver());

			// TODO Maybe we should load ALL *SIMPLE* fields, because the decryption happens on a per-row-level and thus
			// loading only some fields makes no sense performance-wise. However, maybe DataNucleus already optimizes
			// calls to this method. It makes definitely no sense to load 1-n- or 1-1-fields and it makes no sense to
			// optimize things that already are optimal. Hence we have to analyze first, how often this method is really
			// called in normal operation.
			// Marco.

			DataEntry dataEntry = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntry(classMeta, objectIDString);
			if (dataEntry == null)
				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			ObjectContainer objectContainer = encryptionHandler.decryptDataEntry(cryptoContext, dataEntry);

			op.replaceFields(fieldNumbers, new FetchFieldManager(op, cryptoContext, classMeta, dnClassMetaData, objectContainer));
			if (op.getVersion() == null) // null-check prevents overwriting in case this method is called multiple times (for different field-numbers) - TODO necessary?
				op.setVersion(objectContainer.getVersion());
		} finally {
			mconn.release();
		}
	}

	@Override
	public Object findObject(ExecutionContext ec, Object id) {
		// Since we don't manage the memory instantiation of objects this just returns null.
		return null;
	}

	@Override
	public void insertObjects(ObjectProvider ... ops) {
		boolean error = true;
		ObjectContainerHelper.enterTemporaryReferenceScope();
		try {
			super.insertObjects(ops);

			error = false;
		} finally {
			ObjectContainerHelper.exitTemporaryReferenceScope(error);
		}
	}

	@Override
	public void deleteObjects(ObjectProvider... ops) {
		boolean error = true;
		ObjectContainerHelper.enterTemporaryReferenceScope();
		try {
			super.deleteObjects(ops);

			error = false;
		} finally {
			ObjectContainerHelper.exitTemporaryReferenceScope(error);
		}
	}


	@Override
	public void insertObject(ObjectProvider op)
	{
		// Check if read-only so update not permitted
		storeManager.assertReadOnlyForUpdateOfObject(op);

		if (op.getEmbeddedOwners() != null && op.getEmbeddedOwners().length > 0) {
			return; // don't handle embedded objects here!
		}

		ExecutionContext ec = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pmData = pmConn.getDataPM();
			CryptoContext cryptoContext = new CryptoContext(encryptionCoordinateSetManager, keyStoreRefManager, ec, pmConn);
			getStoreManager().getDatastoreVersionManager().applyOnce(cryptoContext);

			boolean error = true;
			ObjectContainerHelper.enterTemporaryReferenceScope();
			try {
				Object object = op.getObject();
				Object objectID = op.getExternalObjectId();
				if (objectID == null) {
					throw new IllegalStateException("op.getExternalObjectId() returned null! Maybe Cumulus4jStoreManager.isStrategyDatastoreAttributed(...) is incorrect?");
				}
				final ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());

				AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), ec.getClassLoaderResolver());

				ObjectContainer objectContainer = new ObjectContainer();
				String objectIDString = objectID.toString();

				// We have to persist the DataEntry before the call to provideFields(...), because the InsertFieldManager recursively
				// persists other fields which might back-reference (=> mapped-by) and thus need this DataEntry to already exist.
				// TO DO Try to make this persistent afterwards and solve the problem by only allocating the ID before [keeping it in memory] (see Cumulus4jStoreManager#nextDataEntryID(), which is commented out currently).
				//   Even though reducing the INSERT + UPDATE to one single INSERT in the handling of IndexEntry made
				//   things faster, it seems not to have a performance benefit here. But we should still look at this
				//   again later.
				// Marco.
				//
				// 2012-02-02: Refactored this because of a Heisenbug with optimistic transactions. At the same time solved
				// the above to do. Marco :-)

				// This performs reachability on this input object so that all related objects are persisted.
				op.provideFields(
						dnClassMetaData.getAllMemberPositions(),
						new StoreFieldManager(op, cryptoContext, pmData, classMeta, dnClassMetaData, cryptoContext.getKeyStoreRefID(), objectContainer));
				objectContainer.setVersion(op.getTransactionalVersion());

				// The DataEntry might already have been written by ObjectContainerHelper.entityToReference(...),
				// if it was needed for a reference. We therefore check, if it already exists (and update it then instead of insert).
				boolean persistDataEntry = false;
				DataEntry dataEntry = ObjectContainerHelper.getTemporaryReferenceDataEntry(cryptoContext, pmData, objectIDString);
				if (dataEntry != null)
					logger.trace("insertObject: Found temporary-reference-DataEntry for: {}", objectIDString);
				else {
					persistDataEntry = true;
					dataEntry = new DataEntry(classMeta, cryptoContext.getKeyStoreRefID(), objectIDString);
					logger.trace("insertObject: Created new DataEntry for: {}", objectIDString);
				}

				encryptionHandler.encryptDataEntry(cryptoContext, dataEntry, objectContainer);

				// persist data
				if (persistDataEntry) {
					dataEntry = pmData.makePersistent(dataEntry);
					logger.trace("insertObject: Persisted new non-embedded DataEntry for: {}", objectIDString);
				}

				insertObjectIndex(op, cryptoContext, classMeta, dnClassMetaData, objectContainer, dataEntry);

				error = false;
			} finally {
				ObjectContainerHelper.exitTemporaryReferenceScope(error);
			}
		} finally {
			mconn.release();
		}
	}

	protected void insertObjectIndex(
			ObjectProvider op, CryptoContext cryptoContext,
			ClassMeta classMeta, AbstractClassMetaData dnClassMetaData,
			ObjectContainer objectContainer, DataEntry dataEntry
	)
	{
		// persist index
		for (Map.Entry<Long, ?> me : objectContainer.getFieldID2value().entrySet()) {
			long fieldID = me.getKey();
			Object fieldValue = me.getValue();
			FieldMeta fieldMeta = classMeta.getFieldMeta(fieldID);
			if (fieldMeta == null)
				throw new IllegalStateException("fieldMeta not found: " + classMeta + ": fieldID=" + fieldID);

			insertObjectIndex(cryptoContext, classMeta, dataEntry, fieldMeta, fieldValue);
		}
	}

	protected void insertObjectIndex(
			CryptoContext cryptoContext, ClassMeta classMeta, DataEntry dataEntry,
			FieldMeta fieldMeta, EmbeddedObjectContainer embeddedObjectContainer
	)
	{
		ClassMeta embeddedClassMeta = storeManager.getClassMeta(cryptoContext.getExecutionContext(), embeddedObjectContainer.getClassID(), true);
		EmbeddedClassMeta ecm = (EmbeddedClassMeta) embeddedClassMeta;
		for (Map.Entry<Long, ?> me : embeddedObjectContainer.getFieldID2value().entrySet()) {
			long embeddedFieldID = me.getKey();
			Object embeddedFieldValue = me.getValue();
			EmbeddedFieldMeta embeddedFieldMeta = (EmbeddedFieldMeta) ecm.getFieldMeta(embeddedFieldID);
			if (embeddedFieldMeta == null)
				throw new IllegalStateException("fieldMeta not found: " + classMeta + ": embeddedFieldID=" + embeddedFieldID);

			insertObjectIndex(cryptoContext, embeddedClassMeta, dataEntry, embeddedFieldMeta, embeddedFieldValue);
		}
	}

	protected void insertObjectIndex(
			CryptoContext cryptoContext, ClassMeta classMeta, DataEntry dataEntry,
			FieldMeta fieldMeta, Object fieldValue
	)
	{
		if (cryptoContext == null)
			throw new IllegalArgumentException("cryptoContext == null");

		if (classMeta == null)
			throw new IllegalArgumentException("classMeta == null");

		if (dataEntry == null)
			throw new IllegalArgumentException("dataEntry == null");

		if (fieldMeta == null)
			throw new IllegalArgumentException("fieldMeta == null");

		if (fieldValue instanceof EmbeddedObjectContainer) {
			EmbeddedObjectContainer embeddedObjectContainer = (EmbeddedObjectContainer) fieldValue;
			insertObjectIndex(cryptoContext, classMeta, dataEntry, fieldMeta, embeddedObjectContainer);
		}
		else if (fieldValue instanceof EmbeddedObjectContainer[]) {
			EmbeddedObjectContainer[] embeddedObjectContainers = (EmbeddedObjectContainer[]) fieldValue;
			for (EmbeddedObjectContainer embeddedObjectContainer : embeddedObjectContainers) {
				if (embeddedObjectContainer != null)
					insertObjectIndex(cryptoContext, classMeta, dataEntry, fieldMeta, embeddedObjectContainer);
			}
		}
		else {
			AbstractMemberMetaData dnMemberMetaData = fieldMeta.getDataNucleusMemberMetaData(cryptoContext.getExecutionContext());

			// sanity checks
			if (dnMemberMetaData == null)
				throw new IllegalStateException("dnMemberMetaData == null!!! class == \"" + classMeta.getClassName() + "\" fieldMeta.dataNucleusAbsoluteFieldNumber == " + fieldMeta.getDataNucleusAbsoluteFieldNumber() + " fieldMeta.fieldName == \"" + fieldMeta.getFieldName() + "\"");

			if (!fieldMeta.getFieldName().equals(dnMemberMetaData.getName()))
				throw new IllegalStateException("Meta data inconsistency!!! class == \"" + classMeta.getClassName() + "\" fieldMeta.dataNucleusAbsoluteFieldNumber == " + fieldMeta.getDataNucleusAbsoluteFieldNumber() + " fieldMeta.fieldName == \"" + fieldMeta.getFieldName() + "\" != dnMemberMetaData.name == \"" + dnMemberMetaData.getName() + "\"");

			addIndexEntryAction.perform(cryptoContext, dataEntry.getDataEntryID(), fieldMeta, dnMemberMetaData, classMeta, fieldValue);
		}
	}

	@Override
	public void locateObject(ObjectProvider op)
	{
		ExecutionContext ec = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(op.getExecutionContext());
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pmData = pmConn.getDataPM();
			CryptoContext cryptoContext = new CryptoContext(encryptionCoordinateSetManager, keyStoreRefManager, ec, pmConn);
			getStoreManager().getDatastoreVersionManager().applyOnce(cryptoContext);

			ClassMeta classMeta = storeManager.getClassMeta(op.getExecutionContext(), op.getObject().getClass());
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();

			DataEntry dataEntry = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntry(classMeta, objectIDString);
			if (dataEntry == null)
				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);
		} finally {
			mconn.release();
		}
	}

	@Override
	public void updateObject(ObjectProvider op, int[] fieldNumbers)
	{
		// Check if read-only so update not permitted
		storeManager.assertReadOnlyForUpdateOfObject(op);

		if (op.getEmbeddedOwners() != null && op.getEmbeddedOwners().length > 0) {
			return; // don't handle embedded objects here!
		}

		ExecutionContext ec = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pmData = pmConn.getDataPM();
			CryptoContext cryptoContext = new CryptoContext(encryptionCoordinateSetManager, keyStoreRefManager, ec, pmConn);
			getStoreManager().getDatastoreVersionManager().applyOnce(cryptoContext);

			boolean error = true;
			ObjectContainerHelper.enterTemporaryReferenceScope();
			try {

				Object object = op.getObject();
				Object objectID = op.getExternalObjectId();
				String objectIDString = objectID.toString();
				final ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());
				AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), ec.getClassLoaderResolver());

				DataEntry dataEntry = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntry(classMeta, objectIDString);
				if (dataEntry == null)
					throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

				ObjectContainer objectContainerOld = encryptionHandler.decryptDataEntry(cryptoContext, dataEntry);
				ObjectContainer objectContainerNew = objectContainerOld.clone();

				// This performs reachability on this input object so that all related objects are persisted
				op.provideFields(fieldNumbers, new StoreFieldManager(op, cryptoContext, pmData, classMeta, dnClassMetaData, cryptoContext.getKeyStoreRefID(), objectContainerNew));
				objectContainerNew.setVersion(op.getTransactionalVersion());

				// update persistent data
				encryptionHandler.encryptDataEntry(cryptoContext, dataEntry, objectContainerNew);

				// update persistent index
				for (int fieldNumber : fieldNumbers) {
					AbstractMemberMetaData dnMemberMetaData = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
					if (dnMemberMetaData == null)
						throw new IllegalStateException("dnMemberMetaData == null!!! class == \"" + classMeta.getClassName() + "\" fieldNumber == " + fieldNumber);

					if (dnMemberMetaData.getMappedBy() != null)
						continue; // TODO is this sufficient to take 'mapped-by' into account?

					FieldMeta fieldMeta = classMeta.getFieldMeta(dnMemberMetaData.getClassName(), dnMemberMetaData.getName());
					if (fieldMeta == null)
						throw new IllegalStateException("fieldMeta == null!!! class == \"" + classMeta.getClassName() + "\" dnMemberMetaData.className == \"" + dnMemberMetaData.getClassName() + "\" dnMemberMetaData.name == \"" + dnMemberMetaData.getName() + "\"");

					Object fieldValueOld = objectContainerOld.getValue(fieldMeta.getFieldID());
					Object fieldValueNew = objectContainerNew.getValue(fieldMeta.getFieldID());

					if (!fieldsEqual(fieldValueOld, fieldValueNew)){

//						removeIndexEntryAction.perform(cryptoContext, dataEntryID, fieldMeta, dnMemberMetaData, classMeta, fieldValueOld);
//						addIndexEntryAction.perform(   cryptoContext, dataEntryID, fieldMeta, dnMemberMetaData, classMeta, fieldValueNew);
						deleteObjectIndex(cryptoContext, classMeta, dataEntry, fieldMeta, fieldValueOld);
						insertObjectIndex(cryptoContext, classMeta, dataEntry, fieldMeta, fieldValueNew);
					}
				}

				error = false;
			} finally {
				ObjectContainerHelper.exitTemporaryReferenceScope(error);
			}
		} finally {
			mconn.release();
		}
	}

	private static boolean fieldsEqual(Object obj0, Object obj1) {
		if (obj0 instanceof Object[] && obj1 instanceof Object[])
			return obj0 == obj1 || Arrays.equals((Object[])obj0, (Object[])obj1);
		return obj0 == obj1 || (obj0 != null && obj0.equals(obj1));
	}

// TODO what happened to this method? Was it moved or renamed? I don't find it.
//	@Override
	public boolean useReferentialIntegrity() {
		// https://sourceforge.net/tracker/?func=detail&aid=3515527&group_id=517465&atid=2102914
//		return super.useReferentialIntegrity();
		return false;
	}

	/**
	 * Get the {@link IndexEntryAction} used to add an index-entry.
	 * @return the {@link IndexEntryAction} used to add an index-entry. Never <code>null</code>.
	 */
	public IndexEntryAction getAddIndexEntryAction() {
		return addIndexEntryAction;
	}

	/**
	 * Get the {@link IndexEntryAction} used to remove an index-entry.
	 * @return the {@link IndexEntryAction} used to remove an index-entry. Never <code>null</code>.
	 */
	public IndexEntryAction getRemoveIndexEntryAction() {
		return removeIndexEntryAction;
	}
}
