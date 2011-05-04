package org.cumulus4j.integrationtest.webapp.test;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;

public class IntegrationTest
{
	private static final String URL_INTEGRATIONTEST_WEBAPP = "http://localhost:8585/org.cumulus4j.integrationtest.webapp";
	private static final String URL_DUMMY = URL_INTEGRATIONTEST_WEBAPP + "/Dummy";

	@Test
	public void test1()
	{
		Client client = new Client();
		String url = URL_DUMMY + "/test";
		String result;
		try {
			result = client.resource(url).accept(MediaType.TEXT_PLAIN).post(String.class);
		} catch (UniformInterfaceException x) {
//			InputStream in = x.getResponse().getEntityInputStream();
//			TODO read in and produce a helpful error that is easily readable in jenkins.
			throw x;
		}

		if (result == null)
			Assert.fail("The POST request on URL " + url + " did not return any result!");

		if (!result.startsWith("OK:"))
			Assert.fail("The POST request on URL " + url + " did not return the expected result! Instead it returned: " + result);
	}

}
