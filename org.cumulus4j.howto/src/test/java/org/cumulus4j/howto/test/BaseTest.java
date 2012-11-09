package org.cumulus4j.howto.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.ws.rs.core.MediaType;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;

public abstract class BaseTest {

	private static final Logger logger = LoggerFactory
			.getLogger(BaseTest.class);

	// Address of the application server
	private static final String URL_APP_SERVER = "http://localhost:8585";
	
	// Context of the howto project within the application server
	protected static final String URL_HOWTO_CONTEXT = URL_APP_SERVER
			+ "/org.cumulus4j.howto";
	
	// The web application which holds the howto services.
	protected static final String URL_HOWTO_WEBAPP = URL_HOWTO_CONTEXT
			+ "/App";

	protected static long transferStreamData(InputStream in, OutputStream out)
			throws java.io.IOException {
		return transferStreamData(in, out, 0, -1);
	}

	/*
	 * This method performs a call to the application server.
	 * It does not matter if you are using Cumulus4j or not, the only
	 * thing that changes is the url.
	 */
	protected void invokeTestWithinServer(String url) throws Exception{
		Client client = new Client();
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

	protected static long transferStreamData(InputStream in, OutputStream out,
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
