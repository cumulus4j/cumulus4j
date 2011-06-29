/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.integrationtest.webapp.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.SecureRandom;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.AppServer;
import org.cumulus4j.keymanager.AppServerManager;
import org.cumulus4j.keymanager.Session;
import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.api.DefaultKeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.cumulus4j.keystore.DateDependentKeyStrategy;
import org.cumulus4j.keystore.KeyStore;
import org.junit.Assert;
import org.junit.Test;
import org.nightlabs.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;

public class IntegrationWithAppServerOnlyTest
{
	private static final Logger logger = LoggerFactory.getLogger(IntegrationWithAppServerOnlyTest.class);

	private static final String URL_APP_SERVER = "http://localhost:8585";
//	private static final String URL_APP_SERVER = "http://localhost:8080";
	private static final String URL_INTEGRATIONTEST_WEBAPP = URL_APP_SERVER + "/org.cumulus4j.integrationtest.webapp";
	private static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_INTEGRATIONTEST_WEBAPP + "/org.cumulus4j.keymanager.back.webapp";
	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP + "/Test";

	private static final String KEY_STORE_USER = "marco";
	private static final char[] KEY_STORE_PASSWORD = "abcdefg-very+secret".toCharArray();

	private static SecureRandom random = new SecureRandom();

	/**
	 * Test for the 2-computer-deployment-scenario. DO NOT USE THIS AS AN EXAMPLE FOR YOUR OWN CODE!!!
	 * You should instead use the API (this code here is called "low-level" for a reason!) as shown below
	 * in {@link #testTwoComputerScenarioWithUnifiedAPI()}.
	 *
	 * @throws Exception if sth. goes wrong.
	 */
	@Test
	public void testTwoComputerScenarioLowLevel() throws Exception
	{
		File keyStoreFile = File.createTempFile("test-", ".keystore");
		try {
			KeyStore keyStore = new KeyStore(keyStoreFile);
			// User creation is done during the keyStrategy.init(...)
//			keyStore.createUser(null, null, KEY_STORE_USER, KEY_STORE_PASSWORD);
			DateDependentKeyStrategy keyStrategy = new DateDependentKeyStrategy(keyStore);
			keyStrategy.init(KEY_STORE_USER, KEY_STORE_PASSWORD, 3600L * 1000L, 24L * 3600L * 1000L);
			AppServerManager appServerManager = new AppServerManager(keyStore);
			AppServer appServer = new AppServer(appServerManager, "appServer1", URL_KEY_MANAGER_BACK_WEBAPP);
			appServerManager.putAppServer(appServer);
			Session session = appServer.getSessionManager().openSession(KEY_STORE_USER, KEY_STORE_PASSWORD);
			session.setLocked(false);

			invokeTestWithinServer(session.getCryptoSessionID());
		} finally {
			keyStoreFile.delete();
		}
	}

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

	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception
	{
		// We do not want to put test-key-store-files into the ~/.cumulus4j folder, thus setting this to the temp dir.
		File keyStoreDir = new File(IOUtil.getTempDir(), "cumulus4j-integration-test-key-stores");

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

			org.cumulus4j.keymanager.api.Session session = keyManagerAPI.getSession(URL_KEY_MANAGER_BACK_WEBAPP);

			// It does not matter here in this test, but in real code, WE MUST ALWAYS lock() after we did unlock()!!!
			// Hence we do it here, too, in case someone copies the code ;-)
			// Marco :-)
			session.unlock();
			try {

				invokeTestWithinServer(session.getCryptoSessionID());

			} finally {
				session.lock();
			}

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
					logger.warn("The key-store-file could not be deleted: " + keyStoreFile.getAbsolutePath());
			}
		}
	}

	@Test
	public void testUnifiedAPIWithLocalKeyStoreThoroughly() throws Exception
	{
		// We do not want to put test-key-store-files into the ~/.cumulus4j folder, thus setting this to the temp dir.
		File keyStoreDir = new File(IOUtil.getTempDir(), "cumulus4j-integration-test-key-stores");

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

			keyManagerAPI.putUser(KEY_STORE_USER, "anotherVerySecretPassword".toCharArray());

			keyManagerAPI.putUser("user2", "password2".toCharArray());
			keyManagerAPI.putUser("user3", "password3".toCharArray());

			configuration = new KeyManagerAPIConfiguration(configuration);
			configuration.setAuthUserName("user3");
			configuration.setAuthPassword("password3".toCharArray());

			org.cumulus4j.keymanager.api.Session session = keyManagerAPI.getSession(URL_KEY_MANAGER_BACK_WEBAPP);

			// It does not matter here in this test, but in real code, WE MUST ALWAYS lock() after we did unlock()!!!
			// Hence we do it here, too, in case someone copies the code ;-)
			// Marco :-)
			session.unlock();
			try {

				invokeTestWithinServer(session.getCryptoSessionID());

			} finally {
				session.lock();
			}

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
					logger.warn("The key-store-file could not be deleted: " + keyStoreFile.getAbsolutePath());
			}
		}
	}
}
