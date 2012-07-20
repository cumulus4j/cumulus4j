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
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.ObjectContainer;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.AbstractPersistenceHandler;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
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

	private IndexEntryAction addIndexEntry;
	private IndexEntryAction removeIndexEntry;

	public Cumulus4jPersistenceHandler(Cumulus4jStoreManager storeManager) {
		if (storeManager == null)
			throw new IllegalArgumentException("storeManager == null");

		this.storeManager = storeManager;
		this.encryptionCoordinateSetManager = storeManager.getEncryptionCoordinateSetManager();
		this.keyStoreRefManager = storeManager.getKeyStoreRefManager();
		this.encryptionHandler = storeManager.getEncryptionHandler();

		this.addIndexEntry = new IndexEntryAction.Add(this);
		this.removeIndexEntry = new IndexEntryAction.Remove(this);
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

			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());
			DataEntry dataEntry = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntry(classMeta, objectIDString);
			//			if (dataEntry == null)
			//				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			if (dataEntry != null) {
				// decrypt object-container in order to identify index entries for deletion
				ObjectContainer objectContainer = encryptionHandler.decryptDataEntry(cryptoContext, dataEntry);
				AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), ec.getClassLoaderResolver());

				for (Map.Entry<Long, ?> me : objectContainer.getFieldID2value().entrySet()) {
					long fieldID = me.getKey();
					Object fieldValue = me.getValue();
					FieldMeta fieldMeta = classMeta.getFieldMeta(fieldID);
					AbstractMemberMetaData dnMemberMetaData = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(fieldMeta.getDataNucleusAbsoluteFieldNumber());

					// sanity checks
					if (dnMemberMetaData == null)
						throw new IllegalStateException("dnMemberMetaData == null!!! class == \"" + classMeta.getClassName() + "\" fieldMeta.dataNucleusAbsoluteFieldNumber == " + fieldMeta.getDataNucleusAbsoluteFieldNumber() + " fieldMeta.fieldName == \"" + fieldMeta.getFieldName() + "\"");

					if (!fieldMeta.getFieldName().equals(dnMemberMetaData.getName()))
						throw new IllegalStateException("Meta data inconsistency!!! class == \"" + classMeta.getClassName() + "\" fieldMeta.dataNucleusAbsoluteFieldNumber == " + fieldMeta.getDataNucleusAbsoluteFieldNumber() + " fieldMeta.fieldName == \"" + fieldMeta.getFieldName() + "\" != dnMemberMetaData.name == \"" + dnMemberMetaData.getName() + "\"");

					removeIndexEntry.perform(cryptoContext, dataEntry.getDataEntryID(), fieldMeta, dnMemberMetaData, fieldValue);
				}
				pmData.deletePersistent(dataEntry);
			}

		} finally {
			mconn.release();
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

			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());
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
	public void insertObject(ObjectProvider op)
	{
		// Check if read-only so update not permitted
		storeManager.assertReadOnlyForUpdateOfObject(op);

		ExecutionContext ec = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pmData = pmConn.getDataPM();
			CryptoContext cryptoContext = new CryptoContext(encryptionCoordinateSetManager, keyStoreRefManager, ec, pmConn);

			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());

			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), ec.getClassLoaderResolver());

			int[] allFieldNumbers = dnClassMetaData.getAllMemberPositions();
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

//			// In case we work with deferred datastore operations, the DataEntry might already have been written by
//			// ObjectContainerHelper.entityToReference(...). We therefore, check, if it already exists (and update it then instead of insert).
//			DataEntry dataEntry;
//			dataEntry = DataEntry.getDataEntry(pmData, classMeta, objectIDString);
//			if (dataEntry != null)
//				logger.trace("insertObject: Found existing DataEntry for: {}", objectIDString);
//			else {
//				dataEntry = pmData.makePersistent(new DataEntry(classMeta, objectIDString));
//				logger.trace("insertObject: Persisted DataEntry for: {}", objectIDString);
//			}

			// This performs reachability on this input object so that all related objects are persisted.
			op.provideFields(allFieldNumbers, new StoreFieldManager(op, cryptoContext, pmData, classMeta, dnClassMetaData, cryptoContext.getKeyStoreRefID(), objectContainer));
			objectContainer.setVersion(op.getTransactionalVersion());

			// The DataEntry might already have been written by ObjectContainerHelper.entityToReference(...),
			// if it was needed for a reference. We therefore check, if it already exists (and update it then instead of insert).
			boolean persistDataEntry = false;
			DataEntry dataEntry = ObjectContainerHelper.popTemporaryReferenceDataEntry(cryptoContext, pmData, objectIDString);
			if (dataEntry != null)
				logger.trace("insertObject: Found temporary-reference-DataEntry for: {}", objectIDString);
			else {
				persistDataEntry = true;
				dataEntry = new DataEntry(classMeta, cryptoContext.getKeyStoreRefID(), objectIDString);
				logger.trace("insertObject: Created new DataEntry for: {}", objectIDString);
			}

			// persist data
			encryptionHandler.encryptDataEntry(cryptoContext, dataEntry, objectContainer);

			if (persistDataEntry) {
				dataEntry = pmData.makePersistent(dataEntry);
				logger.trace("insertObject: Persisted new DataEntry for: {}", objectIDString);
			}

			// persist index
			for (Map.Entry<Long, ?> me : objectContainer.getFieldID2value().entrySet()) {
				long fieldID = me.getKey();
				Object fieldValue = me.getValue();
				FieldMeta fieldMeta = classMeta.getFieldMeta(fieldID);
				AbstractMemberMetaData dnMemberMetaData = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(fieldMeta.getDataNucleusAbsoluteFieldNumber());

				// sanity checks
				if (dnMemberMetaData == null)
					throw new IllegalStateException("dnMemberMetaData == null!!! class == \"" + classMeta.getClassName() + "\" fieldMeta.dataNucleusAbsoluteFieldNumber == " + fieldMeta.getDataNucleusAbsoluteFieldNumber() + " fieldMeta.fieldName == \"" + fieldMeta.getFieldName() + "\"");

				if (!fieldMeta.getFieldName().equals(dnMemberMetaData.getName()))
					throw new IllegalStateException("Meta data inconsistency!!! class == \"" + classMeta.getClassName() + "\" fieldMeta.dataNucleusAbsoluteFieldNumber == " + fieldMeta.getDataNucleusAbsoluteFieldNumber() + " fieldMeta.fieldName == \"" + fieldMeta.getFieldName() + "\" != dnMemberMetaData.name == \"" + dnMemberMetaData.getName() + "\"");

				addIndexEntry.perform(cryptoContext, dataEntry.getDataEntryID(), fieldMeta, dnMemberMetaData, fieldValue);
			}
		} finally {
			mconn.release();
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

		ExecutionContext ec = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(ec);
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pmData = pmConn.getDataPM();
			CryptoContext cryptoContext = new CryptoContext(encryptionCoordinateSetManager, keyStoreRefManager, ec, pmConn);

			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());
			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), ec.getClassLoaderResolver());

			DataEntry dataEntry = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntry(classMeta, objectIDString);
			if (dataEntry == null)
				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			long dataEntryID = dataEntry.getDataEntryID();

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

					removeIndexEntry.perform(cryptoContext, dataEntryID, fieldMeta, dnMemberMetaData, fieldValueOld);
					addIndexEntry.perform(cryptoContext, dataEntryID, fieldMeta, dnMemberMetaData, fieldValueNew);
				}
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

	@Override
	public boolean useReferentialIntegrity() {
		// https://sourceforge.net/tracker/?func=detail&aid=3515527&group_id=517465&atid=2102914
		return super.useReferentialIntegrity();
	}
}
