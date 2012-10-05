package org.cumulus4j.howto.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;

public class Cumulus4jWithDummyKeyManagerTest extends BaseTest{

	private static final Logger logger = LoggerFactory
			.getLogger(Cumulus4jWithDummyKeyManagerTest.class);

	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP + "/DummyKeyManagerService";
//	protected static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP + "/DatanucleusService";

	private void invokeTestWithinServer()
			throws Exception {
		Client client = new Client();

		String url = URL_TEST;
		String result;
		try {
			result = client.resource(url).accept(MediaType.TEXT_PLAIN)
					.post(String.class);
		} catch (UniformInterfaceException x) {
			String message = null;
			try {
				InputStream in = x.getResponse().getEntityInputStream();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				transferStreamData(in, out);
				in.close();
				message = new String(out.toByteArray(), "UTF-8");
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

		if (!result.startsWith("OK:"))
			Assert.fail("The POST request on URL "
					+ url
					+ " did not return the expected result! Instead it returned: "
					+ result);
	}

	@Test
	public void testTwoComputerScenarioWithUnifiedAPI() throws Exception {
		invokeTestWithinServer();
	}
}
