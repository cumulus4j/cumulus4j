package org.cumulus4j.store.test.framework;

import javax.jdo.PersistenceManager;

public interface TransactionalTest
{
	PersistenceManager getPersistenceManager();
	void setPersistenceManager(PersistenceManager persistenceManager);
}
