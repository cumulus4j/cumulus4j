package org.datanucleus.test;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.log4j.Logger;
import org.datanucleus.test.id.AnchorID;
import org.datanucleus.test.id.LocalAccountantDelegateID;

public class Main {
	private static final Logger logger = Logger.getLogger(Main.class);

	private PersistenceManagerFactory persistenceManagerFactory;

	private PersistenceManager createPersistenceManager()
	{
		if (persistenceManagerFactory == null)
			persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");

		return persistenceManagerFactory.getPersistenceManager();
	}

	public void closePersistenceManagerFactory()
	{
		if (this.persistenceManagerFactory != null) {
			this.persistenceManagerFactory.close();
			this.persistenceManagerFactory = null;
		}
	}

	public static interface TransRunnable {
		public void run(PersistenceManager pm) throws Exception;
	}

	public void executeInTransaction(TransRunnable runner) throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			try {
				pm.currentTransaction().begin();

				runner.run(pm);

				pm.currentTransaction().commit();
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();
			}
		} finally {
			pm.close();
		}
	}

	private static final String ORGANISATION_ID = "jfire.my.org";

	private static class CreateDataTransRunnable implements TransRunnable
	{
		public void run(PersistenceManager pm)
		{
//			Account account = new Account(ORGANISATION_ID, "voucher.00");
//			LocalAccountantDelegate localAccountantDelegate = pm.makePersistent(
//					new LocalAccountantDelegate(ORGANISATION_ID, "0")
//			);
//			localAccountantDelegate.setAccount("EUR", account);

			Account account = new Account(ORGANISATION_ID, "voucher.00");
			LocalAccountantDelegate localAccountantDelegate = new LocalAccountantDelegate(ORGANISATION_ID, "0");
			localAccountantDelegate.setAccount("EUR", account);
			pm.makePersistent(localAccountantDelegate);
		}
	}

	private static class QueryDataTransRunnable0 implements TransRunnable
	{
		public void run(PersistenceManager pm) throws IOException
		{
			LocalAccountantDelegateID id = LocalAccountantDelegateID.create(ORGANISATION_ID, "0");
			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(id);
			localAccountantDelegate.test();
		}
	}

	private static class QueryDataTransRunnable1 implements TransRunnable
	{
		public void run(PersistenceManager pm) throws IOException
		{
			LocalAccountantDelegate localAccountantDelegate = pm.getExtent(LocalAccountantDelegate.class).iterator().next();
			localAccountantDelegate.test();
		}
	}

	private static class DeleteDataTransRunnable implements TransRunnable
	{
		public void run(PersistenceManager pm) throws IOException
		{
			LocalAccountantDelegateID localAccountantDelegateID = LocalAccountantDelegateID.create(ORGANISATION_ID, "0");
			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(localAccountantDelegateID);
			pm.deletePersistent(localAccountantDelegate);

			AnchorID accountID = AnchorID.create(ORGANISATION_ID, Account.ANCHOR_TYPE_ID_ACCOUNT, "voucher.00");
			Account account = (Account) pm.getObjectById(accountID);
			pm.deletePersistent(account);
		}
	}

	public static void main(String[] args)
	{
		try {
			Main test = new Main();

			Enumeration<URL> resources = test.getClass().getClassLoader().getResources("plugin.xml");
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				logger.info(url);
			}


			// Ensure we have a completely empty database, before we start up DataNucleus.
			CleanupUtil.dropAllTables();

			// Create the data required for our test.
			CreateDataTransRunnable createDataTransRunnable = new CreateDataTransRunnable();
			test.executeInTransaction(createDataTransRunnable);

			logger.info("*** Executing query 0 ***");
			QueryDataTransRunnable0 queryDataTransRunnable0 = new QueryDataTransRunnable0();
			test.executeInTransaction(queryDataTransRunnable0);
			logger.info("*** Successfully executed query 0 ***");

//			logger.info("*** Executing query 1 ***");
//			QueryDataTransRunnable1 queryDataTransRunnable1 = new QueryDataTransRunnable1();
//			test.executeInTransaction(queryDataTransRunnable1);
//			logger.info("*** Successfully executed query 1 ***");

			logger.info("*** Deleting data ***");
			DeleteDataTransRunnable deleteDataTransRunnable = new DeleteDataTransRunnable();
			test.executeInTransaction(deleteDataTransRunnable);
			logger.info("*** Successfully deleted data ***");
		} catch (Throwable e) {
			logger.error("main: " + e.getLocalizedMessage(), e);
		}
	}
}
