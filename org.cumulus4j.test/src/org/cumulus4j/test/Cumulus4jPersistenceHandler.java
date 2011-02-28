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

		op.getObjectId();
	}

	@Override
	public void fetchObject(ObjectProvider op, int[] fieldNumbers) {
		ExecutionContext executionContext = op.getExecutionContext();
		ManagedConnection mconn = storeManager.getConnection(executionContext);
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();
			Object object = op.getObject();
			Object objectID = op.getExternalObjectId();
			String objectIDString = objectID.toString();
			ClassMeta classMeta = storeManager.getClassMeta(executionContext, object.getClass());
			AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), executionContext.getClassLoaderResolver());

			// We always load ALL SIMPLE fields, because the decryption happens on a per-row-level and thus loading
			// only some fields makes no sense performance-wise. Marco.
			// TODO Check, if this strategy is really good (the above is only my assumption). Marco.
//			int[] allFieldNumbers = dnClassMetaData.getAllMemberPositions();
//			fieldNumbers = allFieldNumbers;
			// TODO implement this correctly (only simple fields! no 1-n-relations! and maybe not even 1-1-relations!).

			DataEntry dataEntry = DataEntry.getDataEntry(pm, classMeta, objectIDString);
			if (dataEntry == null)
				throw new NucleusObjectNotFoundException("Object does not exist in datastore: class=" + classMeta.getClassName() + " oid=" + objectIDString);

			byte[] plainValue = dataEntry.getValue(); // TODO decrypt!

			ObjectContainer objectContainer;
			ByteArrayInputStream in = new ByteArrayInputStream(plainValue);
			try {
				// TODO is this fine for the class-loading of object-id-classes?
				ObjectInputStream objIn = new DataNucleusObjectInputStream(in, executionContext.getClassLoaderResolver());
				objectContainer = (ObjectContainer) objIn.readObject();
				objIn.close();
			} catch (IOException x) {
				throw new RuntimeException(x);
			} catch (ClassNotFoundException x) {
				throw new RuntimeException(x);
			}

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

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			try {
				ObjectOutputStream objOut = new ObjectOutputStream(out);
				objOut.writeObject(objectContainer);
				objOut.close();
			} catch (IOException x) {
				throw new RuntimeException(x);
			}

			// persist data
			DataEntry dataEntry = new DataEntry(classMeta, objectID.toString(), out.toByteArray()); // TODO encrypt this!!!
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
					byte[] indexValueByteArray = indexEntry.getIndexValue(); // TODO decrypt!
					IndexValue indexValue = new IndexValue(indexValueByteArray);
					indexValue.addDataEntryID(dataEntry.getDataEntryID());
					indexEntry.setIndexValue(indexValue.toByteArray()); // TODO encrypt!
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

	}

}
