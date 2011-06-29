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
package org.cumulus4j.integrationtest.withkeyserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.api.DefaultKeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.cumulus4j.keymanager.front.shared.AppServer;
import org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.front.shared.OpenSessionResponse;
import org.junit.Assert;
import org.junit.Test;
import org.nightlabs.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class IntegrationWithAppServerAndKeyServerTest
{
	private static final Logger logger = LoggerFactory.getLogger(IntegrationWithAppServerAndKeyServerTest.class);

	private static final String URL_APP_SERVER = "http://localhost:8585";
//	private static final String URL_APP_SERVER = "http://localhost:8080";
	private static final String URL_INTEGRATIONTEST_WEBAPP = URL_APP_SERVER + "/org.cumulus4j.integrationtest.webapp";
	private static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_INTEGRATIONTEST_WEBAPP + "/org.cumulus4j.keymanager.back.webapp";
	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP + "/Test";

	private static final String URL_KEY_SERVER = "http://localhost:8686";
	private static final String URL_KEY_MANAGER_FRONT_WEBAPP = URL_KEY_SERVER + "/org.cumulus4j.keymanager.front.webapp";

	private static final String KEY_STORE_ID_VAR = "${keyStoreID}";

	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_APP_SERVER = URL_KEY_MANAGER_FRONT_WEBAPP + "/AppServer/" + KEY_STORE_ID_VAR;
//	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_USER = URL_KEY_MANAGER_FRONT_WEBAPP + "/User";
	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_DATE_DEPENDENT_KEY_STRATEGY = URL_KEY_MANAGER_FRONT_WEBAPP + "/DateDependentKeyStrategy/" + KEY_STORE_ID_VAR;
	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_SESSION = URL_KEY_MANAGER_FRONT_WEBAPP + "/Session/" + KEY_STORE_ID_VAR;

	private static final String KEY_SERVER_USER = "devil";
	private static final char[] KEY_SERVER_PASSWORD = "testtesttest".toCharArray();

	private static SecureRandom random = new SecureRandom();

	/**
	 * Test for the 3-computer-deployment-scenario. DO NOT USE THIS AS AN EXAMPLE FOR YOUR OWN CODE!!!
	 * You should instead use the API (this code here is called "low-level" for a reason!) as shown below
	 * in {@link #testThreeComputerScenarioWithUnifiedAPI()}.
	 *
	 * @throws Exception if sth. goes wrong.
	 */
	@Test
	public void testThreeComputerScenarioLowLevel()
	throws Exception
	{
		String keyStoreID = "test-" + Long.toString(System.currentTimeMillis(), 36) + '-' + Long.toString(random.nextLong(), 36);

		Client clientForKeyServer = new Client();
		clientForKeyServer.addFilter(
				new HTTPBasicAuthFilter(KEY_SERVER_USER, new String(KEY_SERVER_PASSWORD))
		);


//		UserWithPassword userWithPassword = new UserWithPassword();
//		userWithPassword.setUserName(KEY_SERVER_USER);
//		userWithPassword.setPassword(KEY_SERVER_PASSWORD);

//		clientForKeyServer.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_USER)
//		.type(MediaType.APPLICATION_XML_TYPE)
//		.put(userWithPassword);

		DateDependentKeyStrategyInitParam ksInitParam = new DateDependentKeyStrategyInitParam();
		ksInitParam.setKeyActivityPeriodMSec(3600L * 1000L);
		ksInitParam.setKeyStorePeriodMSec(24L * 3600L * 1000L);
		clientForKeyServer.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_DATE_DEPENDENT_KEY_STRATEGY.replaceAll(Pattern.quote(KEY_STORE_ID_VAR), keyStoreID) + "/init")
		.type(MediaType.APPLICATION_XML_TYPE)
		.post(ksInitParam);


		AppServer appServer = new AppServer();
		appServer.setAppServerID("appServer1");
		appServer.setAppServerBaseURL(URL_KEY_MANAGER_BACK_WEBAPP);

		clientForKeyServer.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_APP_SERVER.replaceAll(Pattern.quote(KEY_STORE_ID_VAR), keyStoreID))
		.type(MediaType.APPLICATION_XML_TYPE)
		.put(appServer);


		OpenSessionResponse openSessionResponse = clientForKeyServer
		.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_SESSION.replaceAll(Pattern.quote(KEY_STORE_ID_VAR), keyStoreID) + '/' + appServer.getAppServerID() + "/open")
		.accept(MediaType.APPLICATION_XML_TYPE)
		.post(OpenSessionResponse.class);

		String cryptoSessionID = openSessionResponse.getCryptoSessionID();

		clientForKeyServer
		.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_SESSION.replaceAll(Pattern.quote(KEY_STORE_ID_VAR), keyStoreID) + '/' + appServer.getAppServerID() + '/' + cryptoSessionID + "/unlock")
		.post();

		invokeTestWithinServer(cryptoSessionID);
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
	public void testThreeComputerScenarioWithUnifiedAPI()
	throws Exception
	{
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
	}

	@Test
	public void testUnifiedAPIWithRemoteKeyServerThoroughly() throws Exception
	{
		KeyManagerAPIConfiguration configuration = new KeyManagerAPIConfiguration();
		configuration.setAuthUserName(KEY_SERVER_USER);
		configuration.setAuthPassword(KEY_SERVER_PASSWORD);
		configuration.setKeyStoreID("test-" + Long.toString(System.currentTimeMillis(), 36) + '-' + Long.toString(random.nextLong(), 36));
		configuration.setKeyManagerBaseURL(URL_KEY_MANAGER_FRONT_WEBAPP);

		KeyManagerAPI keyManagerAPI = new DefaultKeyManagerAPI();
		keyManagerAPI.setConfiguration(configuration);

		org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam param = new org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam();
		param.setKeyActivityPeriodMSec(3600L * 1000L);
		param.setKeyStorePeriodMSec(24L * 3600L * 1000L);
		keyManagerAPI.initDateDependentKeyStrategy(param);

		keyManagerAPI.putUser(KEY_SERVER_USER, "anotherVerySecretPassword".toCharArray());

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
	}
}
