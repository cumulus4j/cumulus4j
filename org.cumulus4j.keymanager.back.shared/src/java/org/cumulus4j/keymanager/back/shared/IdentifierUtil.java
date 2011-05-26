package org.cumulus4j.keymanager.back.shared;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.UUID;

/**
 * Utility class for identifiers used within Cumulus4j.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class IdentifierUtil
{
	private static SecureRandom random = new SecureRandom();

	private static double log(double base, double value)
	{
		return Math.log10(value) / Math.log10(base);
	}

	/**
	 * <p>
	 * Create a random <code>String</code> identifier with a sufficiently unique length.
	 * </p>
	 * <p>
	 * This method calls {@link #createRandomID(int)} with a <code>length</code> of 25.
	 * </p>
	 * <p>
	 * The <code>length</code> of 25 is chosen, because it produces an identifier
	 * which has about the same uniqueness as {@link UUID#randomUUID()}. This is because
	 * the String has 36 ^ 25 (approximately equals 2 ^ 129) possible values while a UUID
	 * has 2 ^ 128 possible values and both identifiers are created using the same
	 * method ({@link SecureRandom#nextBytes(byte[])}).
	 * </p>
	 * @return a random <code>String</code>.
	 * @see #createRandomID(int)
	 */
	public static String createRandomID()
	{
		return createRandomID(25);
	}

	/**
	 * <p>
	 * Create a random <code>String</code> identifier with a specified length.
	 * </p>
	 * <p>
	 * The generated identifier will contain
	 * only the characters '0'...'9' and 'a'...'z' and will have the specified <code>length</code>.
	 * This method uses a {@link SecureRandom} (just like {@link UUID#randomUUID()}). With a length
	 * of 25, the identifier will have about the same uniqueness as a <code>UUID</code> - see
	 * {@link #createRandomID()}.
	 * </p>
	 *
	 * @param length the number of <code>char</code>s in the result.
	 * @return a random <code>String</code> with the given <code>length</code>.
	 * @see #createRandomID()
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
//		// Check to see whether a length of 25 is approximately as unique as a UUID.
//		double possibleValues = Math.pow(36, 25);
//		double possibleValuesExponentTo2 = log(2, possibleValues);
//		System.out.println(possibleValues);
//		System.out.println(possibleValuesExponentTo2);
//		System.out.println(Math.pow(2, possibleValuesExponentTo2));
//	}

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
