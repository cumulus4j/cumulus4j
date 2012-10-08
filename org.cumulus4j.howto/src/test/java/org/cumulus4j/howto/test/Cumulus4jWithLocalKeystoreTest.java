package org.cumulus4j.howto.test;

import java.io.File;
import java.net.URLEncoder;
import java.security.SecureRandom;

import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.api.DefaultKeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cumulus4jWithLocalKeystoreTest extends BaseTest {

	private static final Logger logger = LoggerFactory
			.getLogger(Cumulus4jWithLocalKeystoreTest.class);

	protected static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_INTEGRATIONTEST_CONTEXT
			+ "/org.cumulus4j.keymanager.back.webapp";

	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP
			+ "/KeyStoreService";

	private static final String KEY_STORE_USER = "marco";
	private static final char[] KEY_STORE_PASSWORD = "abcdefg-very+secret"
			.toCharArray();

	private static SecureRandom random = new SecureRandom();

	protected void invokeTest(String cryptoSessionID) throws Exception {
		invokeTestWithinServer(URL_TEST + "?cryptoSessionID="
				+ URLEncoder.encode(cryptoSessionID, "UTF-8"));
	}

	@Ignore
	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {
		File keyStoreDir = new File(new File(
				System.getProperty("java.io.tmpdir")),
				"cumulus4j-integration-test-key-stores");

		KeyManagerAPIConfiguration configuration = new KeyManagerAPIConfiguration();
		configuration.setAuthUserName(KEY_STORE_USER);
		configuration.setAuthPassword(KEY_STORE_PASSWORD);
		configuration.setKeyStoreID("test-"
				+ Long.toString(System.currentTimeMillis(), 36) + '-'
				+ Long.toString(random.nextLong(), 36));
		configuration.setKeyManagerBaseURL(keyStoreDir.toURI().toString());

		try {
			KeyManagerAPI keyManagerAPI = new DefaultKeyManagerAPI();
			keyManagerAPI.setConfiguration(configuration);

			DateDependentKeyStrategyInitParam param = new DateDependentKeyStrategyInitParam();
			param.setKeyActivityPeriodMSec(3600L * 1000L);
			param.setKeyStorePeriodMSec(24L * 3600L * 1000L);
			keyManagerAPI.initDateDependentKeyStrategy(param);

			org.cumulus4j.keymanager.api.CryptoSession cryptoSession = keyManagerAPI
					.getCryptoSession(URL_KEY_MANAGER_BACK_WEBAPP);

			String cryptoSessionID = cryptoSession.acquire();
			try {

				invokeTest(cryptoSessionID);
			} finally {
				cryptoSession.release();
			}

		} finally {
			File keyStoreFile = new File(keyStoreDir,
					configuration.getKeyStoreID() + ".keystore");
			if (!keyStoreFile.exists()) {
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");

				logger.warn("*** The key-store-file does not exist: "
						+ keyStoreFile.getAbsolutePath());

				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
				logger.warn("**************************************************************************");
			} else {
				keyStoreFile.delete();
				if (keyStoreFile.exists())
					;
				logger.warn("The key-store-file could not be deleted: "
						+ keyStoreFile.getAbsolutePath());
			}
		}
	}
}
