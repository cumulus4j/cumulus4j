package org.cumulus4j.test;

import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.test.model.ClassMeta;
import org.cumulus4j.test.model.DataEntry;
import org.cumulus4j.test.model.FieldMeta;
import org.cumulus4j.test.model.IndexEntry;
import org.cumulus4j.test.resource.ResourceHelper;
import org.datanucleus.store.StoreManager;
import org.datanucleus.store.connection.AbstractConnectionFactory;
import org.datanucleus.store.connection.AbstractManagedConnection;
import org.datanucleus.store.connection.ManagedConnection;

public class Cumulus4jConnectionFactory extends AbstractConnectionFactory
{
	private PersistenceManagerFactory pmf;

	public Cumulus4jConnectionFactory(StoreManager storeMgr, String resourceType) {
		super(storeMgr, resourceType);
		pmf = JDOHelper.getPersistenceManagerFactory(ResourceHelper.openCumulus4jBackendProperties());

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

}
