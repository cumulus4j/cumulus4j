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
package org.cumulus4j.benchmark;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.security.SecureRandom;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.benchmark.framework.PropertyHandler;
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

public class IntegrationWithAppServerOnlyTest {
	private static final Logger logger = LoggerFactory
	.getLogger(IntegrationWithAppServerOnlyTest.class);

	private static final String URL_APP_SERVER = "http://localhost:8585";
	private static final String URL_INTEGRATIONTEST_WEBAPP = URL_APP_SERVER
	+ "/org.cumulus4j.benchmark";
	private static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_INTEGRATIONTEST_WEBAPP
	+ "/org.cumulus4j.keymanager.back.webapp";
	private static final String URL_PERSON = URL_INTEGRATIONTEST_WEBAPP + "/Person";

	private static final String KEY_STORE_USER = "test";
	private static final char[] KEY_STORE_PASSWORD = "abcdefg-very+secret"
		.toCharArray();

//	private static final String URL_KEY_SERVER = "http://localhost:8686";
//	private static final String URL_KEY_MANAGER_FRONT_WEBAPP = URL_KEY_SERVER + "/org.cumulus4j.keymanager.front.webapp";
	
//	private static final String KEY_SERVER_USER = "devil";
//	private static final char[] KEY_SERVER_PASSWORD = "testtesttest".toCharArray();
	
	private static SecureRandom random = new SecureRandom();
	
	private String invokeOnServer(Client client, String cryptoSessionID, String methodName) throws Exception{
		
		String url = URL_PERSON + "/" + methodName + "?cryptoSessionID="
		+ URLEncoder.encode(cryptoSessionID, IOUtil.CHARSET_NAME_UTF_8);
			
		String result;
		
		try {
			result = client.resource(url).accept(MediaType.TEXT_PLAIN).get(String.class);
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
				throw new IOException("Error-code="
						+ x.getResponse().getStatus() + " error-message="
						+ message, x);
		}

		if (result == null)
			Assert.fail("The POST request on URL " + url
					+ " did not return any result!");
		
		return result;
	}
	
	private String invokeOnServer2(Client client, String cryptoSessionID, String methodName, String arg) throws Exception{
		
		String url = URL_PERSON + "/" + methodName + "?cryptoSessionID="
		+ URLEncoder.encode(cryptoSessionID, IOUtil.CHARSET_NAME_UTF_8) + "&arg=" + arg;
			
		String result;
		
		try {
			result = client.resource(url).accept(MediaType.TEXT_PLAIN).get(String.class);
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
				throw new IOException("Error-code="
						+ x.getResponse().getStatus() + " error-message="
						+ message, x);
		}

		if (result == null)
			Assert.fail("The POST request on URL " + url
					+ " did not return any result!");
		
		return result;
	}

	private void invokeTestWithinServer(String cryptoSessionID)
	throws Exception {

		Client client = new Client();
//		
//		while(!invokeOnServer(client, cryptoSessionID, "nextConfiguration").equals("Cumulus4j disabled")){
//			invokeOnServer(client, cryptoSessionID, "warmup");
//			invokeOnServer(client, cryptoSessionID, "persistPersons");
//			invokeOnServer(client, cryptoSessionID, "readAllPersons");
//		}
		
//		int warmupCount = Integer.parseInt(invokeOnServer(client, cryptoSessionID, "getWarmupCount"));
		int benchmarkCount = Integer.parseInt(invokeOnServer(client, cryptoSessionID, "getBenchmarkCount"));
		
//		invokeOnServer(client, cryptoSessionID, "warmup");
//		
//		int stops = benchmarkCount -(benchmarkCount % 100);

		invokeOnServer(client, cryptoSessionID, "warmup");
//		invokeOnServer(client, cryptoSessionID, "persistPersons");
		
//		for(int i = 0; i < PropertyHandler.TEST_OBJECTS;i++)
//			invokeOnServer(client, cryptoSessionID, "persistPerson");
		
		invokeOnServer(client, cryptoSessionID, "readAllPersons");
		
		logger.info(invokeOnServer2(client, cryptoSessionID, "readSomePersons", "arg"));
		
		logger.info(invokeOnServer(client, cryptoSessionID, "getResults"));

//		IReportTracker<String> consoleReport = new ConsoleReportTracker();
//
//		while(!invokeOnServer(client, cryptoSessionID, "getConfiguration").equals("Current properties:\n" + BaseService.CUMULUS4J_NOT_ACTIVATED)){
////			consoleReport.addReport(invokeOnServer(client, cryptoSessionID, "getConfiguration"));
//			consoleReport.newStory(invokeOnServer(client, cryptoSessionID, "getConfiguration"));
//			consoleReport.addReport(invokeOnServer(client, cryptoSessionID, "warmup"));
//			consoleReport.addReport(invokeOnServer(client, cryptoSessionID, "persistPersons"));
//			consoleReport.addReport(invokeOnServer(client, cryptoSessionID, "readAllPersons"));
////			consoleReport.newStory();
//			invokeOnServer(client, cryptoSessionID, "nextConfiguration");
//		}
//		consoleReport.newStory(invokeOnServer(client, cryptoSessionID, "getConfiguration"));
////		consoleReport.addReport(invokeOnServer(client, cryptoSessionID, "getConfiguration"));
//		consoleReport.addReport(invokeOnServer(client, cryptoSessionID, "warmup"));
//		consoleReport.addReport(invokeOnServer(client, cryptoSessionID, "persistPersons"));
//		consoleReport.addReport(invokeOnServer(client, cryptoSessionID, "readAllPersons"));
//			
//		logger.info(consoleReport.getFullReport());
//		
	}

	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {
		// We do not want to put test-key-store-files into the ~/.cumulus4j
		// folder, thus setting this to the temp dir.
		File keyStoreDir = new File(IOUtil.getTempDir(),
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

			org.cumulus4j.keymanager.api.Session session = keyManagerAPI
			.getSession(URL_KEY_MANAGER_BACK_WEBAPP);

			// It does not matter here in this test, but in real code, WE MUST
			// ALWAYS lock() after we did unlock()!!!
			// Hence we do it here, too, in case someone copies the code ;-)
			// Marco :-)
			session.unlock();

			try {
				invokeTestWithinServer(session.getCryptoSessionID());
			} finally {
				session.lock();
			}

		} finally {
			File keyStoreFile = new File(keyStoreDir, configuration
					.getKeyStoreID()
					+ ".keystore");
			if (!keyStoreFile.exists()) {
				logger
				.warn("**************************************************************************");
				logger.warn("*** The key-store-file does not exist: "
						+ keyStoreFile.getAbsolutePath());
				logger
				.warn("**************************************************************************");
			} else {
				keyStoreFile.delete();
				if (keyStoreFile.exists())
					logger.warn("The key-store-file could not be deleted: "
							+ keyStoreFile.getAbsolutePath());
			}
		}
	}
		
//	@Test
//	public void testThreeComputerScenarioWithUnifiedAPI()
//	throws Exception
//	{
//		String keyStoreID = "test-" + Long.toString(System.currentTimeMillis(), 36) + '-' + Long.toString(random.nextLong(), 36);
//
//		KeyManagerAPIConfiguration configuration = new KeyManagerAPIConfiguration();
//		configuration.setAuthUserName(KEY_SERVER_USER);
//		configuration.setAuthPassword(KEY_SERVER_PASSWORD);
//		configuration.setKeyStoreID(keyStoreID);
//		configuration.setKeyManagerBaseURL(URL_KEY_MANAGER_FRONT_WEBAPP);
//
//		KeyManagerAPI keyManagerAPI = new DefaultKeyManagerAPI();
//		keyManagerAPI.setConfiguration(configuration);
//
//		org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam param = new org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam();
//		param.setKeyActivityPeriodMSec(3600L * 1000L);
//		param.setKeyStorePeriodMSec(24L * 3600L * 1000L);
//		keyManagerAPI.initDateDependentKeyStrategy(param);
//
//		org.cumulus4j.keymanager.api.Session session = keyManagerAPI.getSession(URL_KEY_MANAGER_BACK_WEBAPP);
//
//		// It does not matter here in this test, but in real code, WE MUST ALWAYS lock() after we did unlock()!!!
//		// Hence we do it here, too, in case someone copies the code ;-)
//		// Marco :-)
//		session.unlock();
//		try {
//
//			invokeTestWithinServer(session.getCryptoSessionID());
//
//		} finally {
//			session.lock();
//		}
//	}
}
