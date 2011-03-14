package org.cumulus4j.test.core;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractTest
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractTest.class);

	protected static PersistenceManagerFactory pmf;

	protected PersistenceManager pm;

	@BeforeClass
	public static void junitBeforeClass_pmf()
	{
		logger.info("junitBeforeClass_pmf: Setting up PersistenceManagerFactory.");
		pmf = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");
	}

	@AfterClass
	public static void junitAfterClass_pmf()
	{
		logger.info("junitAfterClass_pmf: Shutting down PersistenceManagerFactory.");
		if (pmf != null) {
			pmf.close();
			pmf = null;
		}
	}

	@Before
	public void junitBefore_tx()
	{
		pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
	}

	@After
	public void junitAfter_tx()
	{
		pm.currentTransaction().commit();
	}

}
