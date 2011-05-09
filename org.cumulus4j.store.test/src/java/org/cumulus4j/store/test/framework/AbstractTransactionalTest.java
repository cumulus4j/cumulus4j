package org.cumulus4j.store.test.framework;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.junit.runner.RunWith;

@RunWith(TransactionalRunner.class)
public abstract class AbstractTransactionalTest implements TransactionalTest {

	protected PersistenceManagerFactory pmf;
	protected PersistenceManager pm;

	@Override
	public PersistenceManager getPersistenceManager() {
		return pm;
	}

	@Override
	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.pm = persistenceManager;
		this.pmf = pm == null ? null : pm.getPersistenceManagerFactory();
	}

	protected void commitAndBeginNewTransaction()
	{
		pm.currentTransaction().commit();

		// TODO BEGIN workaround for the pm being closed :-(
		pm.close();
		pm = pmf.getPersistenceManager();
		TransactionalRunner.setEncryptionCoordinates(pm);
		// END workaround

		pm.currentTransaction().begin();
	}
}
