package org.cumulus4j.nightlabsprototype;

import java.util.Map;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.cumulus4j.nightlabsprototype.model.ClassMeta;
import org.cumulus4j.nightlabsprototype.model.DataEntry;
import org.cumulus4j.nightlabsprototype.model.FieldMeta;
import org.cumulus4j.nightlabsprototype.model.IndexEntry;
import org.cumulus4j.nightlabsprototype.resource.ResourceHelper;
import org.datanucleus.PersistenceConfiguration;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.AbstractConnectionFactory;
import org.datanucleus.store.connection.AbstractManagedConnection;
import org.datanucleus.store.connection.ManagedConnection;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Cumulus4jConnectionFactory extends AbstractConnectionFactory
{
	private PersistenceManagerFactory pmf;

	private String[] propertiesToForward = {
			"datanucleus.ConnectionDriverName",
			"datanucleus.ConnectionURL",
			"datanucleus.ConnectionUserName",

			"datanucleus.ConnectionFactory",
			"datanucleus.ConnectionFactoryName",
			"datanucleus.ConnectionFactory2",
			"datanucleus.ConnectionFactory2Name"
	};

	public Cumulus4jConnectionFactory(StoreManager storeMgr, String resourceType) {
		super(storeMgr, resourceType);

		Properties cumulus4jBackendProperties = ResourceHelper.getCumulus4jBackendProperties();

		PersistenceConfiguration persistenceConfiguration = storeMgr.getNucleusContext().getPersistenceConfiguration();
		for (String propKey : propertiesToForward) {
			Object propValue = persistenceConfiguration.getProperty(propKey);
			if (propValue != null)
				cumulus4jBackendProperties.setProperty(propKey, String.valueOf(propValue));
		}

		// The password might be encrypted, but the getConnectionPassword(...) method decrypts it.
		String pw = storeMgr.getConnectionPassword();
		if (pw != null)
			cumulus4jBackendProperties.put("datanucleus.ConnectionPassword", pw);

		pmf = JDOHelper.getPersistenceManagerFactory(cumulus4jBackendProperties);

		// initialise meta-data (which partially tests it)
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			pm.getExtent(ClassMeta.class);
			pm.getExtent(FieldMeta.class);
			pm.getExtent(DataEntry.class);
			pm.getExtent(IndexEntry.class);
		} finally {
			pm.close();
		}
	}

	@Override
	public ManagedConnection createManagedConnection(Object poolKey, @SuppressWarnings("unchecked") Map transactionOptions)
	{
		return new Cumulus4jManagedConnection(poolKey, transactionOptions);
	}

	private class Cumulus4jManagedConnection extends AbstractManagedConnection
	{
		@SuppressWarnings("unused")
		private Object poolKey;

		@SuppressWarnings({"unchecked","unused"})
		private Map options;

		private PersistenceManager pm;

		@Override
		public XAResource getXAResource() {
			// TODO How to handle REAL XA?
			return new Cumulus4jXAResource(pm);
		}

		public Cumulus4jManagedConnection(Object poolKey, @SuppressWarnings("unchecked") Map options) {
			this.poolKey = poolKey;
			this.options = options;
			this.pm = pmf.getPersistenceManager();
		}

		@Override
		public void close() {
			pm.close();
		}

		@Override
		public Object getConnection() {
			return pm;
		}
	}

	class Cumulus4jXAResource implements XAResource
    {
        private PersistenceManager pm;
        private Transaction tx;
//        private Xid xid;

        Cumulus4jXAResource(PersistenceManager pm)
        {
            this.pm = pm;
            this.tx = pm.currentTransaction();
        }

        public void commit(Xid xid, boolean arg1) throws XAException
        {
//        	if (this.xid == null)
//        		throw new IllegalStateException("Transaction not active!");
//
//        	if (!this.xid.equals(xid))
//        		throw new IllegalStateException("Transaction mismatch! this.xid=" + this.xid + " otherXid=" + xid);

            tx.commit();
//            this.xid = null;
        }

        public void end(Xid arg0, int arg1) throws XAException
        {
            //ignore
        }

        public void forget(Xid arg0) throws XAException
        {
            //ignore
        }

        public int getTransactionTimeout() throws XAException
        {
            return 0;
        }

        public boolean isSameRM(XAResource resource) throws XAException
        {
            if (resource instanceof Cumulus4jXAResource)
            {
                if (pm.equals(((Cumulus4jXAResource)resource).pm))
                {
                    return true;
                }
            }
            return false;
        }

        public int prepare(Xid arg0) throws XAException
        {
            return 0;
        }

        public Xid[] recover(int arg0) throws XAException
        {
            throw new XAException("Unsupported operation");
        }

        public void rollback(Xid xid) throws XAException
        {
//        	if (this.xid == null)
//        		throw new IllegalStateException("Transaction not active!");
//
//        	if (!this.xid.equals(xid))
//        		throw new IllegalStateException("Transaction mismatch! this.xid=" + this.xid + " otherXid=" + xid);

            tx.rollback();
//            this.xid = null;
        }

        public boolean setTransactionTimeout(int arg0) throws XAException
        {
            return false;
        }

        public void start(Xid xid, int arg1) throws XAException
        {
//        	if (this.xid != null)
//        		throw new IllegalStateException("Transaction already started! Cannot start twice!");

        	tx.begin();
//        	this.xid = xid;
        }
    }
}
