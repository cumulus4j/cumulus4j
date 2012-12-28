package org.cumulus4j.store.test.framework;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractJDOTransactionalTestClearingDatabase extends AbstractJDOTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractJDOTransactionalTestClearingDatabase.class);

	public static boolean clearDatabaseEnabled = true;

	@BeforeClass
	public static void clearDatabase() throws Exception {
		if (!clearDatabaseEnabled) {
			logger.info("clearDatabase: Clearing database is DISABLED! Skipping.");
			return;
		}
		logger.info("clearDatabase: Clearing database (dropping all tables).");
		CleanupUtil.dropAllTables();
	}

}
