package org.cumulus4j.howto.test;

import java.security.SecureRandom;

import org.cumulus4j.keymanager.api.DefaultKeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.junit.Test;

public class Cumulus4jWithKeyServerTest extends Cumulus4jWithLocalKeystoreTest{
//	private static final Logger logger = LoggerFactory
//			.getLogger(Cumulus4jWithKeyServerTest.class);
//
//	private static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_INTEGRATIONTEST_CONTEXT
//			+ "/org.cumulus4j.keymanager.back.webapp";
//
//	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP
//			+ "/KeyStoreService";

	private static final String URL_KEY_SERVER = "http://localhost:8080";
	private static final String URL_KEY_MANAGER_FRONT_WEBAPP = URL_KEY_SERVER + "/org.cumulus4j.keymanager.front.webapp-1.1.0-SNAPSHOT";


	private static final String KEY_SERVER_USER = "devil";
	private static final char[] KEY_SERVER_PASSWORD = "testtesttest".toCharArray();

	private static SecureRandom random = new SecureRandom();

//	@Override
//	protected void invokeTestWithinServer(String cryptoSessionID)
//			throws Exception {
//		Client client = new Client();
//		String url = URL_TEST + "?cryptoSessionID="
//				+ URLEncoder.encode(cryptoSessionID, "UTF-8");
//		String result;
//		try {
//			result = client.resource(url).accept(MediaType.TEXT_PLAIN)
//					.post(String.class);
//		} catch (UniformInterfaceException x) {
//			String message = null;
//			try {
//				InputStream in = x.getResponse().getEntityInputStream();
//				ByteArrayOutputStream out = new ByteArrayOutputStream();
//				transferStreamData(in, out);
//				in.close();
//				message = new String(out.toByteArray(), "UTF-8");
//			} catch (Exception e) {
//				logger.error("Reading error message failed: " + e, e);
//			}
//			if (message == null)
//				throw x;
//			else
//				throw new IOException("Error-code="
//						+ x.getResponse().getStatus() + " error-message="
//						+ message, x);
//		}
//
//		if (result == null)
//			Assert.fail("The POST request on URL " + url
//					+ " did not return any result!");
//
//		if (!result.startsWith("OK:"))
//			Assert.fail("The POST request on URL "
//					+ url
//					+ " did not return the expected result! Instead it returned: "
//					+ result);
//	}

	@Override
	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {
		String keyStoreID = "test-" + Long.toString(System.currentTimeMillis(), 36) + '-' + Long.toString(random.nextLong(), 36);

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

		org.cumulus4j.keymanager.api.CryptoSession cryptoSession = keyManagerAPI.getCryptoSession(URL_KEY_MANAGER_BACK_WEBAPP);

		// It does not matter here in this test, but in real code, WE MUST ALWAYS lock() after we did unlock()!!!
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
