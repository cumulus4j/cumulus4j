package org.cumulus4j.store.test.framework;

import javax.jdo.PersistenceManager;

public interface JDOTransactionalTest
{
	PersistenceManager getPersistenceManager();
	void setPersistenceManager(PersistenceManager persistenceManager);
}
