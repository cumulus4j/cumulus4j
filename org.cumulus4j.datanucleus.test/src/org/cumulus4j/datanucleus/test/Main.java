package org.cumulus4j.datanucleus.test;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.apache.log4j.Logger;
import org.cumulus4j.datanucleus.test.id.AnchorID;
import org.cumulus4j.datanucleus.test.id.LocalAccountantDelegateID;

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
	private static final LocalAccountantDelegateID LOCAL_ACCOUNTANT_DELEGATE_ID_0 = LocalAccountantDelegateID.create(ORGANISATION_ID, "0");
	private static final AnchorID ACCOUNT_ID_0 = AnchorID.create(ORGANISATION_ID, Account.ANCHOR_TYPE_ID_ACCOUNT, "voucher.00");
	private static final AnchorID ACCOUNT_ID_1 = AnchorID.create(ORGANISATION_ID, Account.ANCHOR_TYPE_ID_ACCOUNT, "voucher.01");

	private static class CreateDataTransRunnable implements TransRunnable
	{
		public void run(PersistenceManager pm)
		{
			Account account = new Account(ACCOUNT_ID_0);
			LocalAccountantDelegate localAccountantDelegate = new LocalAccountantDelegate(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.setAccount("EUR", account);
			pm.makePersistent(localAccountantDelegate); // this should implicitely persist the account
		}
	}

	private static class QueryDataTransRunnable0 implements TransRunnable
	{
		public void run(PersistenceManager pm) throws IOException
		{
			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.test();

			Account account = (Account) pm.getObjectById(ACCOUNT_ID_0);
			account.getBalance();
		}
	}

	private static class UpdateDataTransRunnable0 implements TransRunnable
	{
		public void run(PersistenceManager pm) throws IOException
		{
			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.setName("Test 0000");
		}
	}

	private static class UpdateDataTransRunnable1 implements TransRunnable
	{
		public void run(PersistenceManager pm) throws IOException
		{
			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			localAccountantDelegate.setAccount("CHF", new Account(ACCOUNT_ID_1));
			localAccountantDelegate.setName("New test bla bla bla.");
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
			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
			pm.deletePersistent(localAccountantDelegate);

			Account account = (Account) pm.getObjectById(ACCOUNT_ID_0);
			pm.deletePersistent(account);

			account = (Account) pm.getObjectById(ACCOUNT_ID_1);
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

			logger.info("*** Update data 0 ***");
			UpdateDataTransRunnable0 updateDataTransRunnable0 = new UpdateDataTransRunnable0();
			test.executeInTransaction(updateDataTransRunnable0);
			logger.info("*** Successfully updated data 0 ***");

			logger.info("*** Update data 1 ***");
			UpdateDataTransRunnable1 updateDataTransRunnable1 = new UpdateDataTransRunnable1();
			test.executeInTransaction(updateDataTransRunnable1);
			logger.info("*** Successfully updated data 1 ***");

			logger.info("*** Executing query 1 ***");
			QueryDataTransRunnable1 queryDataTransRunnable1 = new QueryDataTransRunnable1();
			test.executeInTransaction(queryDataTransRunnable1);
			logger.info("*** Successfully executed query 1 ***");

			logger.info("*** Deleting data ***");
			DeleteDataTransRunnable deleteDataTransRunnable = new DeleteDataTransRunnable();
			test.executeInTransaction(deleteDataTransRunnable);
			logger.info("*** Successfully deleted data ***");
		} catch (Throwable e) {
			logger.error("main: " + e.getLocalizedMessage(), e);
		}
	}
}
