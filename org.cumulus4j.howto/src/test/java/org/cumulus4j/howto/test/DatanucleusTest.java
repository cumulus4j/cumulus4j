package org.cumulus4j.howto.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;

public class DatanucleusTest {
	private static final Logger logger = LoggerFactory
			.getLogger(DatanucleusTest.class);

	private static final String URL_APP_SERVER = "http://localhost:8585";
	private static final String URL_INTEGRATIONTEST_CONTEXT = URL_APP_SERVER
			+ "/org.cumulus4j.howto";
	private static final String URL_INTEGRATIONTEST_WEBAPP = URL_INTEGRATIONTEST_CONTEXT
			+ "/App";
	private static final String URL_TEST = URL_INTEGRATIONTEST_WEBAPP + "/DatanucleusService";;

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

		logger.info("Running DatanucleusTest");
		System.out.println("Running DatanucleusTest");
		invokeTestWithinServer();

	}

	private static long transferStreamData(InputStream in, OutputStream out)
			throws java.io.IOException {
		return transferStreamData(in, out, 0, -1);
	}

	private static long transferStreamData(InputStream in, OutputStream out,
			long inputOffset, long inputLen) throws java.io.IOException {
		byte[] buf = new byte[4096];

		int bytesRead;
		int transferred = 0;

		// skip offset
		if (inputOffset > 0)
			if (in.skip(inputOffset) != inputOffset)
				throw new IOException("Input skip failed (offset "
						+ inputOffset + ")");

		while (true) {
			if (inputLen >= 0)
				bytesRead = in.read(buf, 0,
						(int) Math.min(buf.length, inputLen - transferred));
			else
				bytesRead = in.read(buf);

			if (bytesRead <= 0)
				break;

			out.write(buf, 0, bytesRead);

			transferred += bytesRead;

			if (inputLen >= 0 && transferred >= inputLen)
				break;
		}
		out.flush();

		return transferred;
	}
}
