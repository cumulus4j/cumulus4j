package org.cumulus4j.howto.test;

import org.junit.Test;

public class DatanucleusTest extends BaseTest {

	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP
			+ "/DatanucleusService";

	private void invokeTest() throws Exception {

		invokeTestWithinServer(URL_TEST);
	}

	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {

		invokeTest();
	}
}
