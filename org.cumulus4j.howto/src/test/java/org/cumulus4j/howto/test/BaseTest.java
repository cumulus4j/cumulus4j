package org.cumulus4j.howto.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class BaseTest {

	private static final String URL_APP_SERVER = "http://localhost:8585";
	private static final String URL_INTEGRATIONTEST_CONTEXT = URL_APP_SERVER
			+ "/org.cumulus4j.howto";
	protected static final String URL_INTEGRATIONTEST_WEBAPP = URL_INTEGRATIONTEST_CONTEXT
			+ "/App";

	protected static long transferStreamData(InputStream in, OutputStream out)
			throws java.io.IOException {
		return transferStreamData(in, out, 0, -1);
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
