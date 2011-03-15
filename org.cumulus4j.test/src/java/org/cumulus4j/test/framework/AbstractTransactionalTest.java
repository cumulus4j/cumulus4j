package org.cumulus4j.test.framework;

import javax.jdo.PersistenceManager;

import org.junit.runner.RunWith;

@RunWith(TransactionalRunner.class)
public class AbstractTransactionalTest implements TransactionalTest {

	protected PersistenceManager pm;

	@Override
	public PersistenceManager getPersistenceManager() {
		return pm;
	}

	@Override
	public void setPersistenceManager(PersistenceManager persistenceManager) {
		this.pm = persistenceManager;
	}

}
