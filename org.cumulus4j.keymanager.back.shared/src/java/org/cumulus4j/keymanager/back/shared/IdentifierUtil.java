package org.cumulus4j.keymanager.back.shared;

import java.math.BigInteger;
import java.security.SecureRandom;

public class IdentifierUtil
{
	private static SecureRandom random = new SecureRandom();

//	public static String createRandomID()
//	{
//		byte[] val = new byte[17];
//		random.nextBytes(val);
//		val[0] = (byte)(val[0] & 0x7F); // ensure a positive value
//		BigInteger bi = new BigInteger(val);
//		String result = bi.toString(36).substring(1); // cut the first character, because its range is limited (never reaches 'z')
//
//		if (result.length() < 26) { // prepend with '0' to reach a fixed length.
//			StringBuilder sb = new StringBuilder(26);
//			for (int i = result.length(); i < 26; ++i)
//				sb.append('0');
//
//			sb.append(result);
//			result = sb.toString();
//		}
//
//		if (result.length() != 26)
//			throw new IllegalStateException("Why is result.length != 26 chars?!");
//
//		return result;
//	}

	private static double log(double base, double value)
	{
		return Math.log10(value) / Math.log10(base);
	}

	/**
	 * Create a random <code>String</code> identifier. The generated identifier will contain
	 * only the characters '0'...'9' and 'a'...'z' and will have the specified <code>length</code>.
	 * This method uses a {@link SecureRandom}.
	 *
	 * @param length the number of <code>char</code>s in the result.
	 * @return a random <code>String</code> with the given <code>length</code>.
	 */
	public static String createRandomID(int length)
	{
		int byteArrayLength = (int)log(256, Math.pow(36, length)) + 1;

		byte[] val = new byte[byteArrayLength];
		random.nextBytes(val);
		val[0] = (byte)(val[0] & 0x7F); // ensure a positive value
		BigInteger bi = new BigInteger(val);
		String result = bi.toString(36).substring(1); // cut the first character, because its range is limited (never reaches 'z')

		if (result.length() < length) { // prepend with '0' to reach a fixed length.
			StringBuilder sb = new StringBuilder(length);
			for (int i = result.length(); i < length; ++i)
				sb.append('0');

			sb.append(result);
			result = sb.toString();
		}

		if (result.length() > length + 1)
			throw new IllegalStateException("Why is result.length == " + result.length() + " > " + length + "+1 chars?!");

		if (result.length() > length)
			result = result.substring(result.length() - length);

		if (result.length() != length)
			throw new IllegalStateException("Why is result.length != " + length + " chars?!");

		return result;
	}

//	public static void main(String[] args) {
//		long start = System.currentTimeMillis();
//		double a = Math.random();
//		double b = Math.random();
//		double p = Math.pow(a, b);
//		double l = log(a, p);
//		System.out.println("a = " + a);
//		System.out.println("b = " + b);
//		System.out.println("p = " + p);
//		System.out.println("l = " + l);
//		System.out.println("|b-l| = " + Math.abs(b-l));
//
//		for (int i = 0; i < 10000; ++i) {
//			String id = createRandomID(1 + (i % 50));
//			System.out.println(id);
//		}
//		System.out.println("Duration: " + (System.currentTimeMillis() - start) + " msec");
//	}
}
