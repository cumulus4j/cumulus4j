package org.cumulus4j.store;

import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Transaction;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.IndexEntryContainerSize;
import org.cumulus4j.store.model.Sequence;
import org.cumulus4j.store.resource.ResourceHelper;
import org.datanucleus.PersistenceConfiguration;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.AbstractConnectionFactory;
import org.datanucleus.store.connection.AbstractManagedConnection;
import org.datanucleus.store.connection.ManagedConnection;

/**
 * A "connection" in Cumulus4J is a PersistenceManager for the backing datastore.
 * When the transaction in Cumulus4J is committed, the equivalent transaction is committed in the PM(s) of the
 * backing datastore(s).
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Cumulus4jConnectionFactory extends AbstractConnectionFactory
{
	// TODO Have PMF for DataEntry, and PMF for meta+index
	/** PMF for DataEntry, ClassMeta+FieldMeta, plus index data. */
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

	public Cumulus4jConnectionFactory(StoreManager storeMgr, String resourceType) {
		super(storeMgr, resourceType);

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

		// This block is an alternative to getting Extent of each Cumulus4j schema class
/*		StringBuffer classNameStr = new StringBuffer();
		classNameStr.append(ClassMeta.class.getName()).append(",");
		classNameStr.append(DataEntry.class.getName()).append(",");
		classNameStr.append(FieldMeta.class.getName()).append(",");
		classNameStr.append(IndexEntryContainerSize.class.getName()).append(",");
		classNameStr.append(Sequence.class.getName());
		PluginManager pluginMgr = storeMgr.getNucleusContext().getPluginManager();
		ConfigurationElement[] elems = pluginMgr.getConfigurationElementsForExtension(
				"org.cumulus4j.store.index_mapping", null, null);
		if (elems != null && elems.length > 0) {
			HashSet<Class> initialisedClasses = new HashSet<Class>();
			for (int i=0;i<elems.length;i++) {
				String indexTypeName = elems[i].getAttribute("index-entry-type");
				Class cls = pluginMgr.loadClass("org.cumulus4j.store.index_mapping", indexTypeName);
				if (!initialisedClasses.contains(cls)) {
					initialisedClasses.add(cls);
					classNameStr.append(",").append(indexTypeName);
				}
			}
		}
		cumulus4jBackendProperties.put("datanucleus.autostartmechanism", "Classes");
		cumulus4jBackendProperties.put("datanucleus.autostartclassnames", classNameStr.toString());*/

		pmf = JDOHelper.getPersistenceManagerFactory(cumulus4jBackendProperties);

		// initialise meta-data (which partially tests it)
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			pm.getExtent(ClassMeta.class);
			pm.getExtent(DataEntry.class);
			pm.getExtent(FieldMeta.class);
			pm.getExtent(IndexEntryContainerSize.class);
			pm.getExtent(Sequence.class);

			PluginManager pluginMgr = storeMgr.getNucleusContext().getPluginManager();
			ConfigurationElement[] elems = pluginMgr.getConfigurationElementsForExtension(
					"org.cumulus4j.store.index_mapping", null, null);
			if (elems != null && elems.length > 0) {
				HashSet<Class<?>> initialisedClasses = new HashSet<Class<?>>();
				for (int i=0;i<elems.length;i++) {
					String indexTypeName = elems[i].getAttribute("index-entry-type");
					Class<?> cls = pluginMgr.loadClass("org.cumulus4j.store.index_mapping", indexTypeName);
					if (!initialisedClasses.contains(cls)) {
						initialisedClasses.add(cls);
						pm.getExtent(cls);
					}
				}
			}
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
			return new Cumulus4jXAResource((PersistenceManager)getConnection());
		}

		public Cumulus4jManagedConnection(Object poolKey, @SuppressWarnings("unchecked") Map options) {
			this.poolKey = poolKey;
			this.options = options;
		}

		@Override
		public void close() {
			if (pm != null) {
				pm.close();
			}
			pm = null;
		}

		@Override
		public Object getConnection() {
			if (pm == null) {
				this.pm = pmf.getPersistenceManager();
			}
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

        @Override
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

        @Override
				public void end(Xid arg0, int arg1) throws XAException
        {
            //ignore
        }

        @Override
				public void forget(Xid arg0) throws XAException
        {
            //ignore
        }

        @Override
				public int getTransactionTimeout() throws XAException
        {
            return 0;
        }

        @Override
				public boolean isSameRM(XAResource resource) throws XAException
        {
        	if ((resource instanceof Cumulus4jXAResource) && pm.equals(((Cumulus4jXAResource)resource).pm))
        		return true;
        	else
        		return false;
        }

        @Override
				public int prepare(Xid arg0) throws XAException
        {
            return 0;
        }

        @Override
				public Xid[] recover(int arg0) throws XAException
        {
            throw new XAException("Unsupported operation");
        }

        @Override
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

        @Override
				public boolean setTransactionTimeout(int arg0) throws XAException
        {
            return false;
        }

        @Override
				public void start(Xid xid, int arg1) throws XAException
        {
//        	if (this.xid != null)
//        		throw new IllegalStateException("Transaction already started! Cannot start twice!");

        	tx.begin();
//        	this.xid = xid;
        }
    }
}
