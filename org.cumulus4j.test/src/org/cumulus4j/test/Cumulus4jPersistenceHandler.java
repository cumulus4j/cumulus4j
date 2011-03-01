package org.cumulus4j.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.cumulus4j.test.model.ClassMeta;
import org.cumulus4j.test.model.DataEntry;
import org.cumulus4j.test.model.FieldMeta;
import org.cumulus4j.test.model.IndexEntry;
import org.cumulus4j.test.model.IndexValue;
import org.cumulus4j.test.model.ObjectContainer;
import org.datanucleus.ClassLoaderResolver;
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

	public Cumulus4jPersistenceHandler(Cumulus4jStoreManager storeManager) {
		if (storeManager == null)
			throw new IllegalArgumentException("storeManager == null");

		this.storeManager = storeManager;
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
				ObjectContainer objectContainer = decryptDataEntry(dataEntry, executionContext.getClassLoaderResolver());
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
						IndexValue indexValue = decryptIndexEntry(indexEntry);
						indexValue.removeDataEntryID(dataEntry.getDataEntryID());
						if (indexValue.isEmpty())
							pm.deletePersistent(indexEntry);
						else
							encryptIndexEntry(indexEntry, indexValue);
					}
				}
				pm.deletePersistent(dataEntry);
			}

		} finally {
			mconn.release();
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

			ObjectContainer objectContainer = decryptDataEntry(dataEntry, executionContext.getClassLoaderResolver());

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
			objectContainer.setVersion(op.getVersion());

			// persist data
			DataEntry dataEntry = new DataEntry(classMeta, objectID.toString());
			encryptDataEntry(dataEntry, objectContainer);
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
					IndexValue indexValue = decryptIndexEntry(indexEntry);
					indexValue.addDataEntryID(dataEntry.getDataEntryID());
					encryptIndexEntry(indexEntry, indexValue);
				}
			}

// necessary?! probably not.
//			// Perform any reachability
//			// int[] fieldNumbers = op.getClassMetaData().getAllMemberPositions();
//			op.provideFields(allFieldNumbers, new PersistFieldManager(op, true));
		} finally {
			mconn.release();
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

		// TODO implement this!
	}






	private static final byte[] dummyKey = { 43, -113, 119, 2 };

	private static byte[] dummyEncrypt(byte[] plain)
	{
		if (plain == null)
			return null;

		byte[] result = new byte[plain.length];
		int keyIdx = 0;
		for (int i = 0; i < plain.length; i++) {
			result[i] = (byte) (plain[i] ^ dummyKey[keyIdx]);

			if (++keyIdx >= dummyKey.length)
				keyIdx = 0;
		}
		return result;
	}

	private static byte[] dummyDecrypt(byte[] encrypted)
	{
		// it is symmetric => use dummyEncrypt.
		return dummyEncrypt(encrypted);
	}

	/**
	 * Get a plain (unencrypted) {@link ObjectContainer} from the encrypted byte-array in
	 * the {@link DataEntry#getValue() DataEntry.value} property.
	 * @param dataEntry the {@link DataEntry} holding the encrypted data.
	 * @param classLoaderResolver the {@link ClassLoaderResolver} to use for deserialising the {@link ObjectContainer}.
	 * @return the plain {@link ObjectContainer}
	 */
	private ObjectContainer decryptDataEntry(DataEntry dataEntry, ClassLoaderResolver classLoaderResolver)
	{
		byte[] encrypted = dataEntry.getValue();

		// TODO *real* decryption here!
		byte[] plain = dummyDecrypt(encrypted);

		ObjectContainer objectContainer;
		ByteArrayInputStream in = new ByteArrayInputStream(plain);
		try {
			ObjectInputStream objIn = new DataNucleusObjectInputStream(in, classLoaderResolver);
			objectContainer = (ObjectContainer) objIn.readObject();
			objIn.close();
		} catch (IOException x) {
			throw new RuntimeException(x);
		} catch (ClassNotFoundException x) {
			throw new RuntimeException(x);
		}
		return objectContainer;
	}

	private void encryptDataEntry(DataEntry dataEntry, ObjectContainer objectContainer)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			objOut.writeObject(objectContainer);
			objOut.close();
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
		byte[] plain = out.toByteArray(); out = null;

		// TODO *real* encryption here!
		byte[] encrypted = dummyEncrypt(plain);

		dataEntry.setValue(encrypted);
	}

	private IndexValue decryptIndexEntry(IndexEntry indexEntry)
	{
		byte[] encrypted = indexEntry.getIndexValue();

		// TODO *real* decryption here!
		byte[] plain = dummyDecrypt(encrypted);

		IndexValue indexValue = new IndexValue(plain);
		return indexValue;
	}

	private void encryptIndexEntry(IndexEntry indexEntry, IndexValue indexValue)
	{
		byte[] plain = indexValue.toByteArray();

		// TODO *real* encryption here!
		byte[] encrypted = dummyEncrypt(plain);

		indexEntry.setIndexValue(encrypted);
	}
}
