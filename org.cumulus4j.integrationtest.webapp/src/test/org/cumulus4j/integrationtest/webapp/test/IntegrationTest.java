package org.cumulus4j.integrationtest.webapp.test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.AppServer;
import org.cumulus4j.keymanager.AppServerManager;
import org.cumulus4j.keymanager.Session;
import org.cumulus4j.keystore.KeyStore;
import org.junit.Assert;
import org.junit.Test;
import org.nightlabs.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;

public class IntegrationTest
{
	private static final Logger logger = LoggerFactory.getLogger(IntegrationTest.class);

	private static final String URL_APP_SERVER = "http://localhost:8585";
	private static final String URL_INTEGRATIONTEST_WEBAPP = URL_APP_SERVER + "/org.cumulus4j.integrationtest.webapp";
	private static final String URL_KEY_MANAGER_BACK_WEBAPP = URL_INTEGRATIONTEST_WEBAPP + "/org.cumulus4j.keymanager.back.webapp";
	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP + "/Test";

	private static final String KEY_STORE_USER = "marco";
	private static final char[] KEY_STORE_PASSWORD = "abcdefg-very+secret".toCharArray();

	@Test
	public void test1() throws Exception
	{
		File keyStoreFile = File.createTempFile("test-", ".keystore");
		try {
			KeyStore keyStore = new KeyStore(keyStoreFile);
			keyStore.createUser(null, null, KEY_STORE_USER, KEY_STORE_PASSWORD);
			AppServerManager appServerManager = new AppServerManager(keyStore);
			AppServer appServer = new AppServer(appServerManager, "appServer1", new URL(URL_KEY_MANAGER_BACK_WEBAPP));
			appServerManager.putAppServer(appServer);
			Session session = appServer.getSessionManager().openSession(KEY_STORE_USER, KEY_STORE_PASSWORD);
			session.setLocked(false);

//			Thread.sleep(10000); // TODO remove this - currently here only for making it easier to read the log.

			Client client = new Client();
			String url = URL_TEST + "?cryptoSessionID=" + URLEncoder.encode(session.getCryptoSessionID(), IOUtil.CHARSET_NAME_UTF_8);
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
		} finally {
			keyStoreFile.delete();
		}
	}

}
