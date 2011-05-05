package org.cumulus4j.keystore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class KeyStoreUtil
{
	private KeyStoreUtil() { }

	public static void writeByteArrayWithLengthHeader(DataOutputStream out, byte[] source)
	throws IOException
	{
		out.writeInt(source.length);
		out.write(source);
	}

	public static byte[] readByteArrayWithLengthHeader(DataInputStream in)
	throws IOException
	{
		int length = in.readInt();
		byte[] dest = new byte[length];
		readByteArrayCompletely(in, dest);
		return dest;
	}

	public static void readByteArrayCompletely(InputStream in, byte[] dest)
	throws IOException
	{
		int off = 0;
		while (off < dest.length) {
			int read = in.read(dest, off, dest.length - off);
			if (read < 0)
				throw new IOException("Unexpected early end of stream!");

			off += read;
		}
	}

	/**
	 * This method encodes a byte array into a human readable hex string. For each byte,
	 * two hex digits are produced. They are concatted without any separators.
	 * <p>
	 * This is a convenience method for <code>encodeHexStr(buf, 0, buf.length)</code>
	 * <p>
	 * This is copied from project <code>org.nightlabs.util</code>. If we need  more from this
	 * lib, we should add a dependency onto it.
	 *
	 * @param buf The byte array to translate into human readable text.
	 * @return a human readable string like "fa3d70" for a byte array with 3 bytes and these values.
	 * @see #encodeHexStr(byte[], int, int)
	 * @see #decodeHexStr(String)
	 */
	public static String encodeHexStr(byte[] buf)
	{
		return encodeHexStr(buf, 0, buf.length);
	}

	/**
	 * Encode a byte array into a human readable hex string. For each byte,
	 * two hex digits are produced. They are concatted without any separators.
	 * <p>
	 * This is copied from project <code>org.nightlabs.util</code>. If we need  more from this
	 * lib, we should add a dependency onto it.
	 *
	 * @param buf The byte array to translate into human readable text.
	 * @param pos The start position (0-based).
	 * @param len The number of bytes that shall be processed beginning at the position specified by <code>pos</code>.
	 * @return a human readable string like "fa3d70" for a byte array with 3 bytes and these values.
	 * @see #encodeHexStr(byte[])
	 * @see #decodeHexStr(String)
	 */
	public static String encodeHexStr(byte[] buf, int pos, int len)
	{
		 StringBuffer hex = new StringBuffer();
		 while (len-- > 0) {
				byte ch = buf[pos++];
				int d = (ch >> 4) & 0xf;
				hex.append((char)(d >= 10 ? 'a' - 10 + d : '0' + d));
				d = ch & 0xf;
				hex.append((char)(d >= 10 ? 'a' - 10 + d : '0' + d));
		 }
		 return hex.toString();
	}
}
