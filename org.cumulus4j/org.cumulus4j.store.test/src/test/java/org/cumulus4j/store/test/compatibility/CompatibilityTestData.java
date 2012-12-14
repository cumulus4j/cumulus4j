package org.cumulus4j.store.test.compatibility;

import javax.jdo.PersistenceManager;

public abstract class CompatibilityTestData {

	protected static final String VERSION_1_0_0 = "1.0.0";
	protected static final String VERSION_1_1_0 = "1.1.0";

	protected PersistenceManager pm;

	public PersistenceManager getPersistenceManager() {
		return pm;
	}

	public void setPersistenceManager(PersistenceManager pm) {
		this.pm = pm;
	}

	/**
	 * {@link CompatibilityTestData} implementations are added over time. To prevent trying to verify a new <code>CompatibilityTestData</code>
	 * implementation on an old datastore, each <code>CompatibilityTestData</code> indicates here since which version it exists.
	 * @return the first version this <code>CompatibilityTestData</code> exists (and thus should verify). Never <code>null</code>.
	 * @see #VERSION_1_0_0
	 * @see #VERSION_1_1_0
	 */
	public abstract String getSinceVersion();

	public abstract void create();

	public abstract void verify();

}
