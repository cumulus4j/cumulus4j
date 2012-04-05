package org.polepos.client;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.SecureRandom;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.api.CryptoSession;
import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.api.DefaultKeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.nightlabs.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;


public class ClientTest {

	private static Logger logger = LoggerFactory.getLogger(ClientTest.class);

	private static final String URL_APP_SERVER = "http://localhost:8787";
//	private static final String URL_APP_SERVER = "http://localhost:8080";
	private static final String URL_INTEGRATIONTEST_CONTEXT = URL_APP_SERVER + "/org.cumulus4j.poleposition";
	private static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_INTEGRATIONTEST_CONTEXT + "/org.cumulus4j.keymanager.back.webapp";
	private static final String URL_INTEGRATIONTEST_WEBAPP = URL_INTEGRATIONTEST_CONTEXT + "/App";
	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP + "/Test";

	private static final String KEY_STORE_USER = "jan";
	private static final char[] KEY_STORE_PASSWORD = "abcdefg-very+secret".toCharArray();

	private static SecureRandom random = new SecureRandom();

	private void invokeTestWithinServer(String cryptoSessionID)
	throws Exception
	{
		Client client = new Client();
		String url = URL_TEST + "?cryptoSessionID=" + URLEncoder.encode(cryptoSessionID, IOUtil.CHARSET_NAME_UTF_8);
		String result;
		try {
			result = client.resource(url).accept(MediaType.TEXT_PLAIN).post(String.class);
		} catch (UniformInterfaceException x) {
			String message = null;
			try {
				InputStream in = x.getResponse().getEntityInputStream();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				IOUtil.transferStreamData(in, out);
				in.close();
				message = new String(out.toByteArray(), IOUtil.CHARSET_UTF_8);
			} catch (Exception e) {
				logger.error("Reading error message failed: " + e, e);
			}
			if (message == null)
				throw x;
			else
				throw new IOException("Error-code=" + x.getResponse().getStatus() + " error-message=" + message, x);
		}

		if (result == null)
			Assert.fail("The POST request on URL " + url + " did not return any result!");

		if (!result.startsWith("OK:"))
			Assert.fail("The POST request on URL " + url + " did not return the expected result! Instead it returned: " + result);
	}

	private static class RefreshCryptoSessionThread extends Thread
	{
		private CryptoSession cryptoSession;
		private Throwable error;

		public RefreshCryptoSessionThread(CryptoSession cryptoSession)
		{
			this.cryptoSession = cryptoSession;
		}

		private volatile boolean interruptForced = false;

		@Override
		public void interrupt() {
			interruptForced = true;
			super.interrupt();
		}

		@Override
		public boolean isInterrupted() {
			return interruptForced || super.isInterrupted();
		}

		@Override
		public void run() {
			try {
				while (!isInterrupted()) {
					cryptoSession.acquire(); // does re-acquire, if already acquired - which is always the case in our scenario (except for the start when this thread runs in parallel and thus this might be the very first call to acquire())
					cryptoSession.release(); // is essentially necessary to keep counters correct (every acquire() must be followed by a release()!!!)
					Thread.sleep(10000);
				}
			} catch (Throwable e) {
				logger.error("RefreshCryptoSessionThread.run:" + e, e);
				this.error = e;
			}
		}

		public Throwable getError() {
			return error;
		}
	}

	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception
	{
		// We do not want to put test-key-store-files into the ~/.cumulus4j folder, thus setting this to the temp dir.
		File keyStoreDir = new File(IOUtil.getTempDir(), "cumulus4j-benchmark-key-stores");

		logger.debug("Key store file directory: " + keyStoreDir.getAbsolutePath());

		KeyManagerAPIConfiguration configuration = new KeyManagerAPIConfiguration();
		configuration.setAuthUserName(KEY_STORE_USER);
		configuration.setAuthPassword(KEY_STORE_PASSWORD);
		configuration.setKeyStoreID("test-" + Long.toString(System.currentTimeMillis(), 36) + '-' + Long.toString(random.nextLong(), 36));
		configuration.setKeyManagerBaseURL(keyStoreDir.toURI().toString());

		try {
			KeyManagerAPI keyManagerAPI = new DefaultKeyManagerAPI();
			keyManagerAPI.setConfiguration(configuration);

			DateDependentKeyStrategyInitParam param = new DateDependentKeyStrategyInitParam();
			param.setKeyActivityPeriodMSec(3600L * 1000L);
			param.setKeyStorePeriodMSec(24L * 3600L * 1000L);
			keyManagerAPI.initDateDependentKeyStrategy(param);

			org.cumulus4j.keymanager.api.CryptoSession cryptoSession = keyManagerAPI.getCryptoSession(URL_KEY_MANAGER_BACK_WEBAPP);

			RefreshCryptoSessionThread refreshCryptoSessionThread = new RefreshCryptoSessionThread(cryptoSession);
			refreshCryptoSessionThread.start();
			try {

				// It does not matter here in this test, but in real code, WE MUST ALWAYS release() after we did acquire()!!!
				// Hence we do it here, too, in order to be a good example and in case someone copies the code ;-)
				// Marco :-)
				String cryptoSessionID = cryptoSession.acquire();
				try {
					invokeTestWithinServer(cryptoSessionID);
				} finally {
					cryptoSession.release();
				}

			} finally {
				refreshCryptoSessionThread.interrupt();
			}

			refreshCryptoSessionThread.join(10000L);
			if (refreshCryptoSessionThread.getError() != null)
				throw new RuntimeException("Asynchronous refreshing of CryptoSession encountered an error: " + refreshCryptoSessionThread.getError(), refreshCryptoSessionThread.getError());

		} finally {
			File keyStoreFile = new File(keyStoreDir, configuration.getKeyStoreID() + ".keystore");
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

				logger.warn("*** The key-store-file does not exist: " + keyStoreFile.getAbsolutePath());

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
			}
			else {
				keyStoreFile.delete();
				if (keyStoreFile.exists())
					logger.warn("The key store file could not be deleted: " + keyStoreFile.getAbsolutePath());
				else
					logger.info("The key store file has been deleted: " + keyStoreFile.getAbsolutePath());
			}
		}
	}
}
