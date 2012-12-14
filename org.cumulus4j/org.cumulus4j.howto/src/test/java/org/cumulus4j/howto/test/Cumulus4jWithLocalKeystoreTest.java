package org.cumulus4j.howto.test;

import java.io.File;
import java.net.URLEncoder;
import java.security.SecureRandom;

import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.api.DefaultKeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cumulus4jWithLocalKeystoreTest extends BaseTest {

	private static final Logger logger = LoggerFactory
			.getLogger(Cumulus4jWithLocalKeystoreTest.class);

	protected static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_HOWTO_CONTEXT
			+ "/org.cumulus4j.keymanager.back.webapp";

	private static final String URL_TEST = URL_HOWTO_WEBAPP
			+ "/KeyStoreService";
	
	// User name and password for for the key store
	private static final String KEY_STORE_USER = "marco";
	private static final char[] KEY_STORE_PASSWORD = "abcdefg-very+secret"
			.toCharArray();

	private static SecureRandom random = new SecureRandom();

	protected void invokeTest(String cryptoSessionID) throws Exception {
		invokeTestWithinServer(URL_TEST + "?cryptoSessionID="
				+ URLEncoder.encode(cryptoSessionID, "UTF-8"));
	}

	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {
		
		// The file which contains the local key store
		File keyStoreDir = new File(new File(
				System.getProperty("java.io.tmpdir")),
				"cumulus4j-integration-test-key-stores");

		// Configuration of name, password and id of the key manager.
		KeyManagerAPIConfiguration configuration = new KeyManagerAPIConfiguration();
		configuration.setAuthUserName(KEY_STORE_USER);
		configuration.setAuthPassword(KEY_STORE_PASSWORD);
		configuration.setKeyStoreID("test-"
				+ Long.toString(System.currentTimeMillis(), 36) + '-'
				+ Long.toString(random.nextLong(), 36));
		
		// Configuration of the key manager to use a local key store at
		// the given directory directory.
		configuration.setKeyManagerBaseURL(keyStoreDir.toURI().toString());

		try {
			
			// Set configuration of for a local key store.
			KeyManagerAPI keyManagerAPI = new DefaultKeyManagerAPI();
			keyManagerAPI.setConfiguration(configuration);

			DateDependentKeyStrategyInitParam param = new DateDependentKeyStrategyInitParam();
			param.setKeyActivityPeriodMSec(3600L * 1000L);
			param.setKeyStorePeriodMSec(24L * 3600L * 1000L);
			keyManagerAPI.initDateDependentKeyStrategy(param);

			// Get the crypto session of the local key store for the call of the 
			// test service.
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

		} finally {
			// Since this is only a test we cleanup the system by deleting 
			// the temporary key store file. 
			File keyStoreFile = new File(keyStoreDir,
					configuration.getKeyStoreID() + ".keystore");
			if (!keyStoreFile.exists()) {
				logger.warn("**************************************************************************");

				logger.warn("*** The key-store-file does not exist: "
						+ keyStoreFile.getAbsolutePath());

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
