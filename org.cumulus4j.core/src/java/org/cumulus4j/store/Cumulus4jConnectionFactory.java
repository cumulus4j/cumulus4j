package org.cumulus4j.store;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.jar.Manifest;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.cumulus4j.annotation.Dummy;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.IndexEntryDate;
import org.cumulus4j.store.model.IndexEntryDouble;
import org.cumulus4j.store.model.IndexEntryLong;
import org.cumulus4j.store.model.IndexEntryStringLong;
import org.cumulus4j.store.model.IndexEntryStringShort;
import org.cumulus4j.store.model.Sequence;
import org.cumulus4j.store.resource.ResourceHelper;
import org.cumulus4j.store.util.ManifestUtil;
import org.datanucleus.PersistenceConfiguration;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.AbstractConnectionFactory;
import org.datanucleus.store.connection.AbstractManagedConnection;
import org.datanucleus.store.connection.ManagedConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Cumulus4jConnectionFactory extends AbstractConnectionFactory
{
	private static final Logger logger = LoggerFactory.getLogger(Cumulus4jConnectionFactory.class);

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

	private static final String CUMULUS4J_PROPERTY_PREFIX = "cumulus4j.";
	private static final String[] CUMULUS4J_FORWARD_PROPERTY_PREFIXES = {
		CUMULUS4J_PROPERTY_PREFIX + "datanucleus.",
		CUMULUS4J_PROPERTY_PREFIX + "javax."
	};

	private String[] getBundleNameAndVersion(Class<?> clazz)
	{
		String[] result = new String[2];

		Manifest manifest;
		try {
			manifest = ManifestUtil.readManifest(clazz);
		} catch (IOException e) {
			logger.warn("Could not read MANIFEST.MF from JAR containing " + clazz, e);
			return result;
		}
		String bundleName = manifest.getMainAttributes().getValue("Bundle-SymbolicName");
		if (bundleName != null) {
			// strip options after actual bundle name
			int idx = bundleName.indexOf(';');
			if (idx >= 0)
				bundleName = bundleName.substring(0, idx);
		}

		String version = manifest.getMainAttributes().getValue("Bundle-Version");

		result[0] = bundleName;
		result[1] = version;
		return result;
	}

	public Cumulus4jConnectionFactory(StoreManager storeMgr, String resourceType) {
		super(storeMgr, resourceType);

		logger.info("====================== Cumulus4j ======================");
		{ // org.cumulus4j.store
			String[] bundleNameAndVersion = getBundleNameAndVersion(Cumulus4jConnectionFactory.class);
			logger.info("Bundle: " + bundleNameAndVersion[0] + " - Version: " + bundleNameAndVersion[1]);
		}
		{ // org.cumulus4j.api
			String[] bundleNameAndVersion = getBundleNameAndVersion(Dummy.class);
			logger.info("Bundle: " + bundleNameAndVersion[0] + " - Version: " + bundleNameAndVersion[1]);
		}
		logger.info("=======================================================");

		Map<String, Object> cumulus4jBackendProperties = ResourceHelper.getCumulus4jBackendProperties();

		PersistenceConfiguration persistenceConfiguration = storeMgr.getNucleusContext().getPersistenceConfiguration();

		// Copy the properties that are directly (as is) forwarded.
		for (String propKey : propertiesToForward) {
			Object propValue = persistenceConfiguration.getProperty(propKey);
			if (propValue != null)
				cumulus4jBackendProperties.put(propKey.toLowerCase(Locale.ENGLISH), propValue);
		}

		// Copy the properties that are prefixed with "cumulus4j." and thus forwarded.
		for (Map.Entry<String, Object> me : persistenceConfiguration.getPersistenceProperties().entrySet()) {
			if (me.getKey() == null) // don't know if null keys can ever occur, but better play safe
				continue;

			for (String prefix : CUMULUS4J_FORWARD_PROPERTY_PREFIXES) {
				if (me.getKey().startsWith(prefix)) {
					String propKey = me.getKey().substring(CUMULUS4J_PROPERTY_PREFIX.length());
					cumulus4jBackendProperties.put(propKey.toLowerCase(Locale.ENGLISH), me.getValue());
				}
			}
		}

		// The password might be encrypted, but the getConnectionPassword(...) method decrypts it.
		String pw = storeMgr.getConnectionPassword();
		if (pw != null)
			cumulus4jBackendProperties.put("datanucleus.ConnectionPassword".toLowerCase(Locale.ENGLISH), pw);

		pmf = JDOHelper.getPersistenceManagerFactory(cumulus4jBackendProperties);

		// initialise meta-data (which partially tests it)
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			pm.getExtent(ClassMeta.class);
			pm.getExtent(DataEntry.class);
			pm.getExtent(FieldMeta.class);
			pm.getExtent(IndexEntryDouble.class);
			pm.getExtent(IndexEntryLong.class);
			pm.getExtent(IndexEntryStringShort.class);
			pm.getExtent(IndexEntryStringLong.class);
			pm.getExtent(IndexEntryDate.class);
			pm.getExtent(Sequence.class);
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
        	if ((resource instanceof Cumulus4jXAResource) && pm.equals(((Cumulus4jXAResource)resource).pm))
        		return true;
        	else
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
