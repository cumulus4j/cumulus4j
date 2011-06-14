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

import org.cumulus4j.store.Cumulus4jConnectionFactory.PersistenceManagerConnection;
import org.cumulus4j.store.fieldmanager.FetchFieldManager;
import org.cumulus4j.store.fieldmanager.StoreFieldManager;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.ObjectContainer;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.AbstractPersistenceHandler;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.connection.ManagedConnection;

/**
 * TODO Support one StoreManager for persistable data and one for indexed data. With this you could
 * just hand off all persistable data to storeManager1.persistenceHandler, and hand off all indexed data
 * to storeManager2.persistenceHandler
 */
public class Cumulus4jPersistenceHandler extends AbstractPersistenceHandler
{
	private Cumulus4jStoreManager storeManager;
	private EncryptionHandler encryptionHandler;

	private IndexEntryAction addIndexEntry;
	private IndexEntryAction removeIndexEntry;

	public Cumulus4jPersistenceHandler(Cumulus4jStoreManager storeManager) {
		if (storeManager == null)
			throw new IllegalArgumentException("storeManager == null");

		this.storeManager = storeManager;
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
			PersistenceManager pmIndex = pmConn.getIndexPM();

			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());
			DataEntry dataEntry = DataEntry.getDataEntry(pmData, classMeta, objectIDString);
			//			if (dataEntry == null)
			//				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			if (dataEntry != null) {
				// decrypt object-container in order to identify index entries for deletion
				ObjectContainer objectContainer = encryptionHandler.decryptDataEntry(ec, dataEntry);
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

					removeIndexEntry.perform(ec, pmIndex, dataEntry.getDataEntryID(), fieldMeta, dnMemberMetaData, fieldValue);
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
			PersistenceManager pmIndex = pmConn.getIndexPM();

			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());
			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), ec.getClassLoaderResolver());

			// TODO Mabe we should load ALL *SIMPLE* fields, because the decryption happens on a per-row-level and thus
			// loading only some fields makes no sense performance-wise. However, maybe DataNucleus already optimizes
			// calls to this method. It makes definitely no sense to load 1-n- or 1-1-fields and it makes no sense to
			// optimize things that already are optimal. Hence we have to analyze first, how often this method is really
			// called in normal operation.
			// Marco.

			DataEntry dataEntry = DataEntry.getDataEntry(pmData, classMeta, objectIDString);
			if (dataEntry == null)
				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			ObjectContainer objectContainer = encryptionHandler.decryptDataEntry(ec, dataEntry);

			op.replaceFields(fieldNumbers, new FetchFieldManager(op, pmData, pmIndex, classMeta, dnClassMetaData, objectContainer));
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
			PersistenceManager pmIndex = pmConn.getIndexPM();

			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());

			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), ec.getClassLoaderResolver());

			int[] allFieldNumbers = dnClassMetaData.getAllMemberPositions();
			ObjectContainer objectContainer = new ObjectContainer();

			// We have to persist the DataEntry before the call to provideFields(...), because the InsertFieldManager recursively
			// persists other fields which might back-reference (=> mapped-by) and thus need this DataEntry to already exist.
			// TODO Try to make this persistent afterwards and solve the problem by only allocating the ID before [keeping it in memory] (see Cumulus4jStoreManager#nextDataEntryID(), which is commented out currently).
			//   Even though reducing the INSERT + UPDATE to one single INSERT in the handling of IndexEntry made
			//   things faster, it seems not to have a performance benefit here. But we should still look at this
			//   again later.
			// Marco.
			DataEntry dataEntry = pmData.makePersistent(new DataEntry(classMeta, objectID.toString()));

			// This performs reachability on this input object so that all related objects are persisted
			op.provideFields(allFieldNumbers, new StoreFieldManager(op, pmData, classMeta, dnClassMetaData, objectContainer));
			objectContainer.setVersion(op.getTransactionalVersion());

			// persist data
			encryptionHandler.encryptDataEntry(ec, dataEntry, objectContainer);

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

				addIndexEntry.perform(ec, pmIndex, dataEntry.getDataEntryID(), fieldMeta, dnMemberMetaData, fieldValue);
			}
		} finally {
			mconn.release();
		}
	}

	@Override
	public void locateObject(ObjectProvider op)
	{
		ManagedConnection mconn = storeManager.getConnection(op.getExecutionContext());
		try {
			PersistenceManagerConnection pmConn = (PersistenceManagerConnection)mconn.getConnection();
			PersistenceManager pmData = pmConn.getDataPM();

			ClassMeta classMeta = storeManager.getClassMeta(op.getExecutionContext(), op.getObject().getClass());
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();

			DataEntry dataEntry = DataEntry.getDataEntry(pmData, classMeta, objectIDString);
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
			PersistenceManager pmIndex = pmConn.getIndexPM();

			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			ClassMeta classMeta = storeManager.getClassMeta(ec, object.getClass());
			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), ec.getClassLoaderResolver());

			DataEntry dataEntry = DataEntry.getDataEntry(pmData, classMeta, objectIDString);
			if (dataEntry == null)
				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			long dataEntryID = dataEntry.getDataEntryID();

			ObjectContainer objectContainerOld = encryptionHandler.decryptDataEntry(ec, dataEntry);
			ObjectContainer objectContainerNew = objectContainerOld.clone();

			// This performs reachability on this input object so that all related objects are persisted
			op.provideFields(fieldNumbers, new StoreFieldManager(op, pmData, classMeta, dnClassMetaData, objectContainerNew));
			objectContainerNew.setVersion(op.getTransactionalVersion());

			// update persistent data
			encryptionHandler.encryptDataEntry(ec, dataEntry, objectContainerNew);

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

				if (!fieldsEqual(fieldValueOld, fieldValueNew)) {
					removeIndexEntry.perform(ec, pmIndex, dataEntryID, fieldMeta, dnMemberMetaData, fieldValueOld);
					addIndexEntry.perform(ec, pmIndex, dataEntryID, fieldMeta, dnMemberMetaData, fieldValueNew);
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
}
