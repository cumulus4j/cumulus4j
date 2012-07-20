package org.cumulus4j.store.test.framework;

import org.junit.BeforeClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractJDOTransactionalTestClearingDatabase extends AbstractJDOTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(AbstractJDOTransactionalTestClearingDatabase.class);

	@BeforeClass
	public static void clearDatabase() throws Exception {
		if (State.getTestRunIndex() == 0) {
			logger.info("clearDatabase: Clearing database (dropping all tables).");
			CleanupUtil.dropAllTables();
		}
		else
			logger.info("clearDatabase: testRunIndex={} => Skip!", State.getTestRunIndex());
	}

}
