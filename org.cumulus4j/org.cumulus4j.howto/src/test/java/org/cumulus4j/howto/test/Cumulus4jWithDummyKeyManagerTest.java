package org.cumulus4j.howto.test;

import org.junit.Test;

public class Cumulus4jWithDummyKeyManagerTest extends BaseTest {

	// The address of the test service with a dummy key manager. 
	private static final String URL_TEST = URL_HOWTO_WEBAPP
			+ "/DummyKeyManagerService";

	@Test
	// For testing the dummy key manager we have to make no additional changes 
	// on the client side
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {

		invokeTestWithinServer(URL_TEST);
	}
}
