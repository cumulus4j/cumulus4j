package org.cumulus4j.store.model;

import javax.jdo.PersistenceManager;

public abstract class AbstractDAO {

	protected PersistenceManager pm;

	public AbstractDAO() { }

	public AbstractDAO(PersistenceManager pm) {
		this.pm = pm;
	}

	public PersistenceManager getPersistenceManager() {
		return pm;
	}
	public void setPersistenceManager(PersistenceManager pm) {
		this.pm = pm;
	}
}
