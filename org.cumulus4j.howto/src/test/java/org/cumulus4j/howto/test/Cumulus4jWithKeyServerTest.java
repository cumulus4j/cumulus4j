//package org.cumulus4j.howto.test;
//
//import java.io.ByteArrayOutputStream;
//import java.io.File;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.URLEncoder;
//import java.security.SecureRandom;
//
//import javax.ws.rs.core.MediaType;
//
//import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
//import org.cumulus4j.keymanager.api.DefaultKeyManagerAPI;
//import org.cumulus4j.keymanager.api.KeyManagerAPI;
//import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
//import org.junit.Assert;
//import org.junit.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.UniformInterfaceException;
//
//public class Cumulus4jWithKeyServerTest {
//	private static final Logger logger = LoggerFactory
//			.getLogger(Cumulus4jWithKeyServerTest.class);
//
//	private static final String URL_APP_SERVER = "http://localhost:8585";
//	private static final String URL_INTEGRATIONTEST_CONTEXT = URL_APP_SERVER
//			+ "/org.cumulus4j.howto";
//	private static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_INTEGRATIONTEST_CONTEXT
//			+ "/org.cumulus4j.keymanager.back.webapp";
//	private static final String URL_INTEGRATIONTEST_WEBAPP = URL_INTEGRATIONTEST_CONTEXT
//			+ "/App";
//	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP + "/Test";
//
//	private static final String KEY_STORE_USER = "marco";
//	private static final char[] KEY_STORE_PASSWORD = "abcdefg-very+secret"
//			.toCharArray();
//
//	private static SecureRandom random = new SecureRandom();
//
//	private void invokeTestWithinServer(String cryptoSessionID)
//	// private void invokeTestWithinServer()
//			throws Exception {
//		Client client = new Client();
//		String url = URL_TEST + "?cryptoSessionID="
//				+ URLEncoder.encode(cryptoSessionID, "UTF-8");
//		// String url = URL_TEST;
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
//
//	@Test
//	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {
//		File keyStoreDir = new File(new File(
//				System.getProperty("java.io.tmpdir")),
//				"cumulus4j-integration-test-key-stores");
//
//		KeyManagerAPIConfiguration configuration = new KeyManagerAPIConfiguration();
//		configuration.setAuthUserName(KEY_STORE_USER);
//		configuration.setAuthPassword(KEY_STORE_PASSWORD);
//		configuration.setKeyStoreID("test-"
//				+ Long.toString(System.currentTimeMillis(), 36) + '-'
//				+ Long.toString(random.nextLong(), 36));
//		configuration.setKeyManagerBaseURL(keyStoreDir.toURI().toString());
//
//		try {
//			KeyManagerAPI keyManagerAPI = new DefaultKeyManagerAPI();
//			keyManagerAPI.setConfiguration(configuration);
//
//			DateDependentKeyStrategyInitParam param = new DateDependentKeyStrategyInitParam();
//			param.setKeyActivityPeriodMSec(3600L * 1000L);
//			param.setKeyStorePeriodMSec(24L * 3600L * 1000L);
//			keyManagerAPI.initDateDependentKeyStrategy(param);
//
//			org.cumulus4j.keymanager.api.CryptoSession cryptoSession = keyManagerAPI
//					.getCryptoSession(URL_KEY_MANAGER_BACK_WEBAPP);
//
//			// It does not matter here in this test, but in real code, WE MUST
//			// ALWAYS release() after we did acquire()!!!
//			// Hence we do it here, too, in order to be a good example and in
//			// case someone copies the code ;-)
//			// Marco :-)
//			String cryptoSessionID = cryptoSession.acquire();
//			try {
//
//				invokeTestWithinServer(cryptoSessionID);
//				// invokeTestWithinServer();
//			} finally {
//				cryptoSession.release();
//			}
//
//		} finally {
//			File keyStoreFile = new File(keyStoreDir,
//					configuration.getKeyStoreID() + ".keystore");
//			if (!keyStoreFile.exists()) {
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//
//				logger.warn("*** The key-store-file does not exist: "
//						+ keyStoreFile.getAbsolutePath());
//
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//				logger.warn("**************************************************************************");
//			} else {
//				keyStoreFile.delete();
//				if (keyStoreFile.exists())
//					;
//				logger.warn("The key-store-file could not be deleted: "
//						+ keyStoreFile.getAbsolutePath());
//			}
//		}
//	}
//
//	public static long transferStreamData(InputStream in, OutputStream out)
//			throws java.io.IOException {
//		return transferStreamData(in, out, 0, -1);
//	}
//
//	public static long transferStreamData(InputStream in, OutputStream out,
//			long inputOffset, long inputLen) throws java.io.IOException {
//		byte[] buf = new byte[4096];
//
//		int bytesRead;
//		int transferred = 0;
//
//		// skip offset
//		if (inputOffset > 0)
//			if (in.skip(inputOffset) != inputOffset)
//				throw new IOException("Input skip failed (offset "
//						+ inputOffset + ")");
//
//		while (true) {
//			if (inputLen >= 0)
//				bytesRead = in.read(buf, 0,
//						(int) Math.min(buf.length, inputLen - transferred));
//			else
//				bytesRead = in.read(buf);
//
//			if (bytesRead <= 0)
//				break;
//
//			out.write(buf, 0, bytesRead);
//
//			transferred += bytesRead;
//
//			if (inputLen >= 0 && transferred >= inputLen)
//				break;
//		}
//		out.flush();
//
//		return transferred;
//	}
//}
