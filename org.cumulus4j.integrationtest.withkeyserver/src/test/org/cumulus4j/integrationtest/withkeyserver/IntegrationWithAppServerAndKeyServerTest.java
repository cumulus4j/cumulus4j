package org.cumulus4j.integrationtest.withkeyserver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

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

	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_APP_SERVER = URL_KEY_MANAGER_FRONT_WEBAPP + "/AppServer";
//	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_USER = URL_KEY_MANAGER_FRONT_WEBAPP + "/User";
	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_DATE_DEPENDENT_KEY_STRATEGY = URL_KEY_MANAGER_FRONT_WEBAPP + "/DateDependentKeyStrategy";
	private static final String URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_SESSION = URL_KEY_MANAGER_FRONT_WEBAPP + "/Session";

	private static final String KEY_SERVER_USER = "devil";
	private static final char[] KEY_SERVER_PASSWORD = "testtesttest".toCharArray();

	@Test
	public void testThreeComputerScenario()
	throws Exception
	{
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
		clientForKeyServer.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_DATE_DEPENDENT_KEY_STRATEGY + "/init")
		.type(MediaType.APPLICATION_XML_TYPE)
		.post(ksInitParam);


		AppServer appServer = new AppServer();
		appServer.setAppServerID("appServer1");
		appServer.setAppServerBaseURL(new URL(URL_KEY_MANAGER_BACK_WEBAPP));

		clientForKeyServer.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_APP_SERVER)
		.type(MediaType.APPLICATION_XML_TYPE)
		.put(appServer);


		OpenSessionResponse openSessionResponse = clientForKeyServer
		.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_SESSION + '/' + appServer.getAppServerID() + "/open")
		.accept(MediaType.APPLICATION_XML_TYPE)
		.post(OpenSessionResponse.class);

		String cryptoSessionID = openSessionResponse.getCryptoSessionID();

		clientForKeyServer
		.resource(URL_KEY_MANAGER_FRONT_WEBAPP_SERVICE_SESSION + '/' + appServer.getAppServerID() + '/' + cryptoSessionID + "/unlock")
		.post();


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
}
