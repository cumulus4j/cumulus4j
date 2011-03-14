package org.cumulus4j.core;

import java.util.Arrays;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexEntryFactory;
import org.cumulus4j.core.model.IndexEntryFactoryRegistry;
import org.cumulus4j.core.model.IndexEntryOneToOneRelationHelper;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.model.ObjectContainer;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
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

	public Cumulus4jPersistenceHandler(Cumulus4jStoreManager storeManager) {
		if (storeManager == null)
			throw new IllegalArgumentException("storeManager == null");

		this.storeManager = storeManager;
		this.encryptionHandler = storeManager.getEncryptionHandler();
	}

	@Override
	public void close() {
		// No resources require to be closed here.
	}

	@Override
	public void deleteObject(ObjectProvider op) {
		// Check if read-only so update not permitted
		storeManager.assertReadOnlyForUpdateOfObject(op);

		ExecutionContext executionContext = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(executionContext);
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();
			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			ClassMeta classMeta = storeManager.getClassMeta(executionContext, object.getClass());
			DataEntry dataEntry = DataEntry.getDataEntry(pm, classMeta, objectIDString);
//			if (dataEntry == null)
//				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			if (dataEntry != null) {
				// decrypt object-container in order to identify index entries for deletion
				ObjectContainer objectContainer = encryptionHandler.decryptDataEntry(dataEntry, executionContext.getClassLoaderResolver());
				AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), executionContext.getClassLoaderResolver());

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

					removeIndexEntry(executionContext, pm, dataEntry.getDataEntryID(), fieldMeta, dnMemberMetaData, fieldValue);
				}
				pm.deletePersistent(dataEntry);
			}

		} finally {
			mconn.release();
		}
	}

	private void removeIndexEntry(
			ExecutionContext executionContext,
			PersistenceManager pm, long dataEntryID,
			FieldMeta fieldMeta, AbstractMemberMetaData dnMemberMetaData,
			Object fieldValue
	)
	{
		IndexEntry indexEntry = null;

		int relationType = dnMemberMetaData.getRelationType(executionContext.getClassLoaderResolver());

		if (Relation.NONE == relationType) {
			IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(dnMemberMetaData, false);
			indexEntry = indexEntryFactory == null ? null : indexEntryFactory.getIndexEntry(pm, fieldMeta, fieldValue);
		}
		else if (Relation.isRelationSingleValued(relationType)) {
			// 1-1-relationship to another persistence-capable object.
			// The fieldValue is already the object-id, hence find out the type of the original object.
			Long otherDataEntryID = null;
			if (fieldValue != null) {
				// The fieldValue is only the object-id, hence we need to find out the type of the persistable object.
				String fieldValueClassName = storeManager.getClassNameForObjectID(fieldValue, executionContext.getClassLoaderResolver(), executionContext);
				Class<?> fieldValueClass = executionContext.getClassLoaderResolver().classForName(fieldValueClassName);
				ClassMeta classMeta = storeManager.getClassMeta(executionContext, fieldValueClass);
				otherDataEntryID = DataEntry.getDataEntryID(pm, classMeta, fieldValue.toString());
			}
			indexEntry = IndexEntryOneToOneRelationHelper.getIndexEntry(pm, fieldMeta, otherDataEntryID);
		}
		else if (Relation.isRelationMultiValued(relationType)) {
			// collection
			// map
			// TODO index collections and maps, too!

		}

		if (indexEntry != null) {
			IndexValue indexValue = encryptionHandler.decryptIndexEntry(indexEntry);
			indexValue.removeDataEntryID(dataEntryID);
			if (indexValue.isDataEntryIDsEmpty())
				pm.deletePersistent(indexEntry);
			else
				encryptionHandler.encryptIndexEntry(indexEntry, indexValue);
		}
	}

	@Override
	public void fetchObject(ObjectProvider op, int[] fieldNumbers)
	{
		ExecutionContext executionContext = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(executionContext);
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();
			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			ClassMeta classMeta = storeManager.getClassMeta(executionContext, object.getClass());
			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), executionContext.getClassLoaderResolver());

			// TODO Mabe we should load ALL *SIMPLE* fields, because the decryption happens on a per-row-level and thus
			// loading only some fields makes no sense performance-wise. However, maybe DataNucleus already optimizes
			// calls to this method. It makes definitely no sense to load 1-n- or 1-1-fields and it makes no sense to
			// optimize things that already are optimal. Hence we have to analyze first, how often this method is really
			// called in normal operation.
			// Marco.

			DataEntry dataEntry = DataEntry.getDataEntry(pm, classMeta, objectIDString);
			if (dataEntry == null)
				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			ObjectContainer objectContainer = encryptionHandler.decryptDataEntry(dataEntry, executionContext.getClassLoaderResolver());

			op.replaceFields(fieldNumbers, new FetchFieldManager(op, classMeta, dnClassMetaData, objectContainer));
			if (op.getVersion() == null) // null-check prevents overwriting in case this method is called multiple times (for different field-numbers) - TODO necessary?
				op.setVersion(objectContainer.getVersion());
		} finally {
			mconn.release();
		}
	}

	@Override
	public Object findObject(ExecutionContext ectx, Object id) {
		// Since we don't manage the memory instantiation of objects this just returns null.
		return null;
	}

	@Override
	public boolean useReferentialIntegrity() {
		return true; // According to an email from Andy received on 2011-03-14 13:56 CET, I should return true here. Marco.
	}

	@Override
	public void insertObject(ObjectProvider op)
	{
		// Check if read-only so update not permitted
		storeManager.assertReadOnlyForUpdateOfObject(op);

		ExecutionContext executionContext = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(executionContext);
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();
			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			ClassMeta classMeta = storeManager.getClassMeta(executionContext, object.getClass());

//			// BEGIN workaround for changed behaviour of DN's core
//			// TODO DN calls this insertObject(...) *instead* of updateObject(...) - this needs to be removed once the original behaviour is restored.
//			if (DataEntry.getDataEntryID(pm, classMeta, objectID.toString()) != null) {
//				// Delegate to updateObject(...) instead.
//				updateObject(op, op.getDirtyFieldNumbers());
//				return;
//			}
//			// END workaround for changed behaviour of DN's core

			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), executionContext.getClassLoaderResolver());

			int[] allFieldNumbers = dnClassMetaData.getAllMemberPositions();
			ObjectContainer objectContainer = new ObjectContainer();
			// This performs reachability on this input object so that all related objects are persisted
			op.provideFields(allFieldNumbers, new InsertFieldManager(op, classMeta, dnClassMetaData, objectContainer));
			objectContainer.setVersion(op.getTransactionalVersion());

			// persist data
			DataEntry dataEntry = new DataEntry(classMeta, objectID.toString());
			encryptionHandler.encryptDataEntry(dataEntry, objectContainer);
			dataEntry = pm.makePersistent(dataEntry);

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

				addIndexEntry(executionContext, pm, dataEntry.getDataEntryID(), fieldMeta, dnMemberMetaData, fieldValue);
			}
		} finally {
			mconn.release();
		}
	}

	private void addIndexEntry(
			ExecutionContext executionContext, PersistenceManager pm, long dataEntryID,
			FieldMeta fieldMeta, AbstractMemberMetaData dnMemberMetaData,
			Object fieldValue
	)
	{
		IndexEntry indexEntry = null;

		int relationType = dnMemberMetaData.getRelationType(executionContext.getClassLoaderResolver());

		if (Relation.NONE == relationType) {
			IndexEntryFactory indexEntryFactory = IndexEntryFactoryRegistry.sharedInstance().getIndexEntryFactory(dnMemberMetaData, false);
			indexEntry = indexEntryFactory == null ? null : indexEntryFactory.createIndexEntry(pm, fieldMeta, fieldValue);
		}
		else if (Relation.isRelationSingleValued(relationType)) {
			// 1-1-relationship to another persistence-capable object.
			Long otherDataEntryID = null;
			if (fieldValue != null) {
				// The fieldValue is only the object-id, hence we need to find out the type of the persistable object.
				String fieldValueClassName = storeManager.getClassNameForObjectID(fieldValue, executionContext.getClassLoaderResolver(), executionContext);
				Class<?> fieldValueClass = executionContext.getClassLoaderResolver().classForName(fieldValueClassName);
				ClassMeta classMeta = storeManager.getClassMeta(executionContext, fieldValueClass);
				otherDataEntryID = DataEntry.getDataEntryID(pm, classMeta, fieldValue.toString());
			}
			indexEntry = IndexEntryOneToOneRelationHelper.createIndexEntry(pm, fieldMeta, otherDataEntryID);
		}
		else if (Relation.isRelationMultiValued(relationType)) {
			// collection
			// map
			// TODO index collections and maps, too!

		}

		if (indexEntry != null) {
			IndexValue indexValue = encryptionHandler.decryptIndexEntry(indexEntry);
			indexValue.addDataEntryID(dataEntryID);
			encryptionHandler.encryptIndexEntry(indexEntry, indexValue);
		}
	}

	@Override
	public void locateObject(ObjectProvider op)
	{
		ManagedConnection mconn = storeManager.getConnection(op.getExecutionContext());
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();

			ClassMeta classMeta = storeManager.getClassMeta(op.getExecutionContext(), op.getObject().getClass());
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();

			DataEntry dataEntry = DataEntry.getDataEntry(pm, classMeta, objectIDString);
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

		ExecutionContext executionContext = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(executionContext);
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();
			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			ClassMeta classMeta = storeManager.getClassMeta(executionContext, object.getClass());
			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), executionContext.getClassLoaderResolver());

			DataEntry dataEntry = DataEntry.getDataEntry(pm, classMeta, objectIDString);
			if (dataEntry == null)
				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			long dataEntryID = dataEntry.getDataEntryID();

			ObjectContainer objectContainerOld = encryptionHandler.decryptDataEntry(dataEntry, executionContext.getClassLoaderResolver());
			ObjectContainer objectContainerNew = objectContainerOld.clone();

            // This performs reachability on this input object so that all related objects are persisted
			op.provideFields(fieldNumbers, new InsertFieldManager(op, classMeta, dnClassMetaData, objectContainerNew));
			objectContainerNew.setVersion(op.getTransactionalVersion());

			// update persistent data
			encryptionHandler.encryptDataEntry(dataEntry, objectContainerNew);

			// update persistent index
			for (int fieldNumber : fieldNumbers) {
				AbstractMemberMetaData dnMemberMetaData = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
				if (dnMemberMetaData == null)
					throw new IllegalStateException("dnMemberMetaData == null!!! class == \"" + classMeta.getClassName() + "\" fieldNumber == " + fieldNumber);

				FieldMeta fieldMeta = classMeta.getFieldMeta(dnMemberMetaData.getClassName(), dnMemberMetaData.getName());
				if (fieldMeta == null)
					throw new IllegalStateException("fieldMeta == null!!! class == \"" + classMeta.getClassName() + "\" dnMemberMetaData.className == \"" + dnMemberMetaData.getClassName() + "\" dnMemberMetaData.name == \"" + dnMemberMetaData.getName() + "\"");

				Object fieldValueOld = objectContainerOld.getValue(fieldMeta.getFieldID());
				Object fieldValueNew = objectContainerNew.getValue(fieldMeta.getFieldID());

				if (!equals(fieldValueOld, fieldValueNew)) {
					removeIndexEntry(executionContext, pm, dataEntryID, fieldMeta, dnMemberMetaData, fieldValueOld);
					addIndexEntry(executionContext, pm, dataEntryID, fieldMeta, dnMemberMetaData, fieldValueNew);
				}
			}
		} finally {
			mconn.release();
		}
	}

	private static boolean equals(Object obj0, Object obj1) {
		if (obj0 instanceof Object[] && obj1 instanceof Object[])
			return obj0 == obj1 || Arrays.equals((Object[])obj0, (Object[])obj1);
		return obj0 == obj1 || (obj0 != null && obj0.equals(obj1));
	}

}
