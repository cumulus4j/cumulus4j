package org.cumulus4j.keyserver.front.webapp;

import java.math.BigInteger;
import java.security.SecureRandom;

public class IdentifierUtil
{
	private static SecureRandom random = new SecureRandom();

	public static String createRandomID()
	{
		byte[] val = new byte[17];
		random.nextBytes(val);
		val[0] = (byte)(val[0] & 0x7F); // ensure a positive value
		BigInteger bi = new BigInteger(val);
		String result = bi.toString(36).substring(1); // cut the first character, because its range is limited (never reaches 'z')

		if (result.length() < 26) { // prepend with '0' to reach a fixed length.
			StringBuilder sb = new StringBuilder(26);
			for (int i = result.length(); i < 26; ++i)
				sb.append('0');

			sb.append(result);
			result = sb.toString();
		}

		if (result.length() != 26)
			throw new IllegalStateException("Why is result.length != 26 chars?!");

		return result;
	}

}
