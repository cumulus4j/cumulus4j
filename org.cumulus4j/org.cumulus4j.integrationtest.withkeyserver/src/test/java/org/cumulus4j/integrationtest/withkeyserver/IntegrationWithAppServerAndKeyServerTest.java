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
import org.cumulus4j.keymanager.front.shared.AcquireCryptoSessionResponse;
import org.cumulus4j.keymanager.front.shared.AppServer;
import org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.front.shared.PutAppServerResponse;
import org.cumulus4j.testutil.IOUtil;
import org.junit.Assert;
import org.junit.Test;
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
	private static final String URL_INTEGRATIONTEST_CONTEXT = URL_APP_SERVER + "/org.cumulus4j.integrationtest.webapp";
	private static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_INTEGRATIONTEST_CONTEXT + "/org.cumulus4j.keymanager.back.webapp";
	private static final String URL_INTEGRATIONTEST_WEBAPP = URL_INTEGRATIONTEST_CONTEXT + "/App";
	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP + "/Test";

	private static final String URL_KEY_SERVER = "http://localhost:8686";
	private static final String URL_KEY_MANAGER_FRONT_WEBAPP = URL_KEY_SERVER + "/org.cumulus4j.keymanager.front.webapp";

	private static final String KEY_STORE_ID_VAR = "${keyStoreID}";

	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_APP_SERVER = URL_KEY_MANAGER_FRONT_WEBAPP + "/AppServer/" + KEY_STORE_ID_VAR;
//	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_USER = URL_KEY_MANAGER_FRONT_WEBAPP + "/User";
	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_DATE_DEPENDENT_KEY_STRATEGY = URL_KEY_MANAGER_FRONT_WEBAPP + "/DateDependentKeyStrategy/" + KEY_STORE_ID_VAR;
	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_SESSION = URL_KEY_MANAGER_FRONT_WEBAPP + "/CryptoSession/" + KEY_STORE_ID_VAR;

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
		appServer.setAppServerBaseURL(URL_KEY_MANAGER_BACK_WEBAPP);

		PutAppServerResponse putAppServerResponse = clientForKeyServer.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_APP_SERVER.replaceAll(Pattern.quote(KEY_STORE_ID_VAR), keyStoreID))
		.type(MediaType.APPLICATION_XML_TYPE)
		.put(PutAppServerResponse.class, appServer);

		appServer.setAppServerID(putAppServerResponse.getAppServerID());


		AcquireCryptoSessionResponse acquireCryptoSessionResponse = clientForKeyServer
		.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_SESSION.replaceAll(Pattern.quote(KEY_STORE_ID_VAR), keyStoreID) + '/' + appServer.getAppServerID() + "/acquire")
		.accept(MediaType.APPLICATION_XML_TYPE)
		.post(AcquireCryptoSessionResponse.class);

		String cryptoSessionID = acquireCryptoSessionResponse.getCryptoSessionID();

		AcquireCryptoSessionResponse acquireSessionResponse2 = clientForKeyServer
		.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_SESSION.replaceAll(Pattern.quote(KEY_STORE_ID_VAR), keyStoreID) + '/' + appServer.getAppServerID() + '/' + cryptoSessionID + "/reacquire")
		.post(AcquireCryptoSessionResponse.class);

		Assert.assertEquals(acquireCryptoSessionResponse.getCryptoSessionID(), acquireSessionResponse2.getCryptoSessionID());

		invokeTestWithinServer(cryptoSessionID, true, false);

		clientForKeyServer
		.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_SESSION.replaceAll(Pattern.quote(KEY_STORE_ID_VAR), keyStoreID) + '/' + appServer.getAppServerID() + '/' + cryptoSessionID + "/release")
		.post();
	}

	private void invokeTestWithinServer(String cryptoSessionID, boolean clean, boolean readBeforeWrite)
	throws Exception
	{
		Client client = new Client();
		String url = URL_TEST
				+ "?cryptoSessionID=" + URLEncoder.encode(cryptoSessionID, IOUtil.CHARSET_NAME_UTF_8)
				+ "&clean=" + clean
				+ "&readBeforeWrite=" + readBeforeWrite;
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

		org.cumulus4j.keymanager.api.CryptoSession cryptoSession = keyManagerAPI.getCryptoSession(URL_KEY_MANAGER_BACK_WEBAPP);

		// It does not matter here in this test, but in real code, WE MUST ALWAYS release() after we did acquire()!!!
		// Hence we do it here, too, in case someone copies the code ;-)
		// Marco :-)
		String cryptoSessionID = cryptoSession.acquire();
		try {

			invokeTestWithinServer(cryptoSessionID, true, false);

		} finally {
			cryptoSession.release();
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

		org.cumulus4j.keymanager.api.CryptoSession cryptoSession = keyManagerAPI.getCryptoSession(URL_KEY_MANAGER_BACK_WEBAPP);

		// It does not matter here in this test, but in real code, WE MUST ALWAYS release() after we did acquire()!!!
		// Hence we do it here, too, in case someone copies the code ;-)
		// Marco :-)
		String cryptoSessionID = cryptoSession.acquire();
		try {

			invokeTestWithinServer(cryptoSessionID, true, false);

		} finally {
			cryptoSession.release();
		}

		// The behaviour is different, depending on whether the datastore already exists (and has data) or not.
		// Hence, we test it twice; this time with clean = false (and once for each possible value of readBeforeWrite).
		cryptoSessionID = cryptoSession.acquire();
		try {

			invokeTestWithinServer(cryptoSessionID, false, false);

		} finally {
			cryptoSession.release();
		}

		cryptoSessionID = cryptoSession.acquire();
		try {

			invokeTestWithinServer(cryptoSessionID, false, true);

		} finally {
			cryptoSession.release();
		}
	}
}
