package org.cumulus4j.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

import javax.jdo.PersistenceManager;

import org.cumulus4j.test.model.DataEntry;
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

//		if (sm.getClassMetaData().getIdentityType() == IdentityType.APPLICATION)
//        {
//            // Check existence of the object since XML doesn't enforce application identity
//            try
//            {
//                locateObject(sm);
//                throw new NucleusUserException(LOCALISER.msg("XML.Insert.ObjectWithIdAlreadyExists",
//                    sm.toPrintableID(), sm.getInternalObjectId()));
//            }
//            catch (NucleusObjectNotFoundException onfe)
//            {
//                // Do nothing since object with this id doesn't exist
//            }
//        }

		ManagedConnection mconn = storeManager.getConnection(op.getExecutionContext());
		try {
			PersistenceManager pm = (PersistenceManager) mconn.getConnection();
			pm.currentTransaction().begin(); // TODO make aware of JTA and skip Tx handling if JTA is used
			try {
				Object object = op.getObject();

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				try {
					ObjectOutputStream objOut = new ObjectOutputStream(out);
					objOut.writeObject(object);
					objOut.close();
				} catch (IOException x) {
					throw new RuntimeException(x);
				}

				DataEntry dataEntry = new DataEntry(out.toByteArray()); // TODO this should be encrypted
				dataEntry = pm.makePersistent(dataEntry);

				// TODO create/update index entries

				pm.currentTransaction().commit();
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();
			}
		} finally {
			mconn.release();
		}
	}

	@Override
	public void locateObject(ObjectProvider op) {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateObject(ObjectProvider op, int[] fieldNumbers)
	{
		// Check if read-only so update not permitted
		storeManager.assertReadOnlyForUpdateOfObject(op);

	}

}
