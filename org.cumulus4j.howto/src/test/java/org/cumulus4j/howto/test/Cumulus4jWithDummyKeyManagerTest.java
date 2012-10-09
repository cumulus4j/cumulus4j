package org.cumulus4j.howto.test;

import org.junit.Test;

public class Cumulus4jWithDummyKeyManagerTest extends BaseTest {

	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP
			+ "/DummyKeyManagerService";

	private void invokeTest() throws Exception {

		invokeTestWithinServer(URL_TEST);
	}

//	@Ignore
	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {

		invokeTest();
	}
}
