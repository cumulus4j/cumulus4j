package org.cumulus4j.howto.test;

import java.security.SecureRandom;

import org.cumulus4j.keymanager.api.DefaultKeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.junit.Test;

public class Cumulus4jWithKeyServerTest extends Cumulus4jWithLocalKeystoreTest {

	private static final String URL_KEY_SERVER = "http://localhost:8080";
	private static final String URL_KEY_MANAGER_FRONT_WEBAPP = URL_KEY_SERVER
			+ "/org.cumulus4j.keymanager.front.webapp-1.1.0-SNAPSHOT";

	private static final String KEY_SERVER_USER = "devil";
	private static final char[] KEY_SERVER_PASSWORD = "testtesttest"
			.toCharArray();

	private static SecureRandom random = new SecureRandom();

	// @Ignore
	@Override
	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {
		String keyStoreID = "test-"
				+ Long.toString(System.currentTimeMillis(), 36) + '-'
				+ Long.toString(random.nextLong(), 36);

		KeyManagerAPIConfiguration configuration = new KeyManagerAPIConfiguration();
		configuration.setAuthUserName(KEY_SERVER_USER);
		configuration.setAuthPassword(KEY_SERVER_PASSWORD);
		configuration.setKeyStoreID(keyStoreID);
		configuration.setKeyManagerBaseURL(URL_KEY_MANAGER_FRONT_WEBAPP);

		KeyManagerAPI keyManagerAPI = new DefaultKeyManagerAPI();
		keyManagerAPI.setConfiguration(configuration);

		org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam param = new org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam();
		param.setKeyActivityPeriodMSec(3600L * 1000L);
		param.setKeyStorePeriodMSec(24L * 3600L * 1000L);
		keyManagerAPI.initDateDependentKeyStrategy(param);

		org.cumulus4j.keymanager.api.CryptoSession cryptoSession = keyManagerAPI
				.getCryptoSession(URL_KEY_MANAGER_BACK_WEBAPP);

		// It does not matter here in this test, but in real code, WE MUST
		// ALWAYS lock() after we did unlock()!!!
		// Hence we do it here, too, in case someone copies the code ;-)
		// Marco :-)
		String cryptoSessionID = cryptoSession.acquire();
		try {

			invokeTest(cryptoSessionID);

		} finally {
			cryptoSession.release();
		}
	}
}
