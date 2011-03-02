package org.cumulus4j.nightlabsprototype;

import java.util.Arrays;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.cumulus4j.nightlabsprototype.model.ClassMeta;
import org.cumulus4j.nightlabsprototype.model.DataEntry;
import org.cumulus4j.nightlabsprototype.model.FieldMeta;
import org.cumulus4j.nightlabsprototype.model.IndexEntry;
import org.cumulus4j.nightlabsprototype.model.IndexValue;
import org.cumulus4j.nightlabsprototype.model.ObjectContainer;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.AbstractPersistenceHandler;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.connection.ManagedConnection;

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

					Class<?> fieldType = dnMemberMetaData.getType();
					removeIndexEntry(pm, dataEntry.getDataEntryID(), fieldMeta, fieldType, fieldValue);
				}
				pm.deletePersistent(dataEntry);
			}

		} finally {
			mconn.release();
		}
	}

	private void removeIndexEntry(PersistenceManager pm, long dataEntryID, FieldMeta fieldMeta, Class<?> fieldType, Object fieldValue)
	{
		IndexEntry indexEntry = null;
		if (String.class.isAssignableFrom(fieldType)) {
			indexEntry = IndexEntry.getIndexEntry(pm, fieldMeta, (String)fieldValue);
		}
		else if (Long.class.isAssignableFrom(fieldType) || Integer.class.isAssignableFrom(fieldType) || Short.class.isAssignableFrom(fieldType) || Byte.class.isAssignableFrom(fieldType) || long.class == fieldType || int.class == fieldType || short.class == fieldType || byte.class == fieldType) {
			Long v = fieldValue == null ? null : ((Number)fieldValue).longValue();
			indexEntry = IndexEntry.getIndexEntry(pm, fieldMeta, v);
		}
		else if (Double.class.isAssignableFrom(fieldType) || Float.class.isAssignableFrom(fieldType) || double.class == fieldType || float.class == fieldType) {
			Long v = fieldValue == null ? null : ((Number)fieldValue).longValue();
			indexEntry = IndexEntry.getIndexEntry(pm, fieldMeta, v);
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
			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), executionContext.getClassLoaderResolver());

			int[] allFieldNumbers = dnClassMetaData.getAllMemberPositions();
			ObjectContainer objectContainer = new ObjectContainer();
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

				Class<?> fieldType = dnMemberMetaData.getType();
				addIndexEntry(pm, dataEntry.getDataEntryID(), fieldMeta, fieldType, fieldValue);
			}

// necessary?! probably not.
//			// Perform any reachability
//			// int[] fieldNumbers = op.getClassMetaData().getAllMemberPositions();
//			op.provideFields(allFieldNumbers, new PersistFieldManager(op, true));
		} finally {
			mconn.release();
		}
	}

	private void addIndexEntry(PersistenceManager pm, long dataEntryID, FieldMeta fieldMeta, Class<?> fieldType, Object fieldValue)
	{
		IndexEntry indexEntry = null;
		if (String.class.isAssignableFrom(fieldType)) {
			indexEntry = IndexEntry.getIndexEntry(pm, fieldMeta, (String)fieldValue);
			if (indexEntry == null)
				indexEntry = pm.makePersistent(new IndexEntry(fieldMeta, (String)fieldValue));
		}
		else if (Long.class.isAssignableFrom(fieldType) || Integer.class.isAssignableFrom(fieldType) || Short.class.isAssignableFrom(fieldType) || Byte.class.isAssignableFrom(fieldType) || long.class == fieldType || int.class == fieldType || short.class == fieldType || byte.class == fieldType) {
			Long v = fieldValue == null ? null : ((Number)fieldValue).longValue();
			indexEntry = IndexEntry.getIndexEntry(pm, fieldMeta, v);
			if (indexEntry == null)
				indexEntry = pm.makePersistent(new IndexEntry(fieldMeta, v));
		}
		else if (Double.class.isAssignableFrom(fieldType) || Float.class.isAssignableFrom(fieldType) || double.class == fieldType || float.class == fieldType) {
			Long v = fieldValue == null ? null : ((Number)fieldValue).longValue();
			indexEntry = IndexEntry.getIndexEntry(pm, fieldMeta, v);
			if (indexEntry == null)
				indexEntry = pm.makePersistent(new IndexEntry(fieldMeta, v));
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
					Class<?> fieldType = dnMemberMetaData.getType();
					removeIndexEntry(pm, dataEntryID, fieldMeta, fieldType, fieldValueOld);
					addIndexEntry(pm, dataEntryID, fieldMeta, fieldType, fieldValueNew);
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
