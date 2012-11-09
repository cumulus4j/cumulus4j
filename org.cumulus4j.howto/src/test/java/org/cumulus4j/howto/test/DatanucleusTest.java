package org.cumulus4j.howto.test;

import org.junit.Test;

public class DatanucleusTest extends BaseTest {

	// The address of the service which uses DataNucleus without Cumulus4j
	private static final String URL_TEST = URL_HOWTO_WEBAPP
			+ "/DatanucleusService";

	// Calling the DataNucleus reference service.  
	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {

		invokeTestWithinServer(URL_TEST);
	}
}
