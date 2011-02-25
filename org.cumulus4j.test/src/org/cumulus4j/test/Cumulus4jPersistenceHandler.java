package org.cumulus4j.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.jdo.PersistenceManager;

import org.cumulus4j.test.model.ClassMeta;
import org.cumulus4j.test.model.DataEntry;
import org.cumulus4j.test.model.ObjectContainer;
import org.datanucleus.exceptions.NucleusObjectNotFoundException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.store.AbstractPersistenceHandler;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.connection.ManagedConnection;
import org.datanucleus.store.fieldmanager.PersistFieldManager;

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
		// TODO Auto-generated method stub

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
//			pm.currentTransaction().begin(); // TODO proper tx handling - this should definitely not be done here!
//			try {
				Object object = op.getObject();
				Object objectID = op.getExternalObjectId();
				ClassMeta classMeta = storeManager.getClassMeta(executionContext, object.getClass());
				AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), executionContext.getClassLoaderResolver());

				int[] fieldNumbers = dnClassMetaData.getAllMemberPositions();
				ObjectContainer objectContainer = new ObjectContainer();
				op.provideFields(fieldNumbers, new InsertFieldManager(op, classMeta, dnClassMetaData, objectContainer));

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					ObjectOutputStream objOut = new ObjectOutputStream(out);
					objOut.writeObject(objectContainer);
					objOut.close();
				} catch (IOException x) {
					throw new RuntimeException(x);
				}

				DataEntry dataEntry = new DataEntry(classMeta, objectID.toString(), out.toByteArray()); // TODO this should be encrypted
				dataEntry = pm.makePersistent(dataEntry);

				// Perform any reachability
//	            int[] fieldNumbers = op.getClassMetaData().getAllMemberPositions();
	            op.provideFields(fieldNumbers, new PersistFieldManager(op, true));
		} finally {
			mconn.release();
		}
	}

//	private void internalInsertOrUpdate(ExecutionContext executionContext, PersistenceManager pm, Object object)
//	{
//		ClassMeta classMeta = storeManager.getClassMeta(executionContext, object.getClass());
//		AbstractClassMetaData dnClassMetaData = storeManager.getMetaDataManager().getMetaDataForClass(object.getClass(), executionContext.getClassLoaderResolver());
//
//		ObjectContainer objectContainer = new ObjectContainer();
//		for (AbstractMemberMetaData memberMetaData : dnClassMetaData.getManagedMembers()) {
//			FieldMeta fieldMeta = classMeta.getFieldMeta(memberMetaData.getName());
//			if (fieldMeta == null)
//				continue; // TODO add logging here!
//
//			if (memberMetaData.getCollection() != null) {
//				// 1-n- or m-n-relationship via collection
//
//			}
//			else if (memberMetaData.getMap() != null) {
//				// 1-n- or m-n-relationship via map
//
//			}
//			else if (memberMetaData.getArray() != null) {
//				// 1-n- or m-n-relationship via array
//				throw new UnsupportedOperationException("Arrays not yet supported!");
//			}
//			else if (PersistenceCapable.class.isAssignableFrom(memberMetaData.getType())) {
//				// 1-1-relationship
//
//				objectContainer.setValue(fieldMeta.getFieldID(), value);
//			}
//			else {
//
//				objectContainer.setValue(fieldMeta.getFieldID(), value);
//			}
//		}
//
//
//		ByteArrayOutputStream out = new ByteArrayOutputStream();
//		try {
//			ObjectOutputStream objOut = new ObjectOutputStream(out);
//			objOut.writeObject(object);
//			objOut.close();
//		} catch (IOException x) {
//			throw new RuntimeException(x);
//		}
//
//		DataEntry dataEntry = new DataEntry(classMeta, objectID.toString(), out.toByteArray()); // TODO this should be encrypted
//		dataEntry = pm.makePersistent(dataEntry);
//	}

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
