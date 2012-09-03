/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.keystore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * <p>
 * Utility class to read and write data (used by the {@link KeyStore}).
 * </p>
 * <p>
 * <b>Important:</b> This class is not part of the API! It might change without further notice!
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class KeyStoreUtil
{
	private KeyStoreUtil() { }

	/**
	 * <p>
	 * Write the byte array <code>source</code> with a length header to <code>out</code>.
	 * </p>
	 * <p>
	 * This method first writes 2 bytes via {@link DataOutputStream#writeShort(int)} indicating
	 * the length of the byte array, i.e. how many bytes are following. Then this
	 * method writes the given byte array.
	 * </p>
	 * <p>
	 * Data written by this method can be read using {@link #readByteArrayWithShortLengthHeader(DataInputStream)}.
	 * </p>
	 * <p>
	 * In contrast to {@link #writeByteArrayWithIntegerLengthHeader(DataOutputStream, byte[])}, the maximum
	 * length of the byte-array <code>source</code> must not exceed
	 * </p>
	 *
	 * @param out the {@link DataOutputStream} to write data to.
	 * @param source the byte array from which to read the data.
	 * @throws IllegalArgumentException if one of the given arguments is <code>null</code> or if <code>source</code>
	 * exceeds the maximum length.
	 * @throws IOException if writing the data to the output failed.
	 * @see #readByteArrayWithShortLengthHeader(DataInputStream)
	 * @see #writeByteArrayWithIntegerLengthHeader(DataOutputStream, byte[])
	 */
	public static void writeByteArrayWithShortLengthHeader(DataOutputStream out, byte[] source)
	throws IllegalArgumentException, IOException
	{
		if (out == null)
			throw new IllegalArgumentException("out == null");

		if (source == null)
			throw new IllegalArgumentException("source == null");

		if (source.length > 0xffff)
			throw new IllegalArgumentException("source too long! Cannot encode length " + source.length + " in 2 bytes!");

		out.writeShort(source.length);
		out.write(source);
	}

	/**
	 * <p>
	 * Write the byte array <code>source</code> with a length header to <code>out</code>.
	 * </p>
	 * <p>
	 * This method first writes 4 bytes via {@link DataOutputStream#writeInt(int)} indicating
	 * the length of the byte array, i.e. how many bytes are following. Then this
	 * method writes the given byte array.
	 * </p>
	 * <p>
	 * Data written by this method can be read using {@link #readByteArrayWithIntegerLengthHeader(DataInputStream)}.
	 * </p>
	 *
	 * @param out the {@link DataOutputStream} to write data to.
	 * @param source the byte array from which to read the data.
	 * @throws IllegalArgumentException if one of the given arguments is <code>null</code>.
	 * @throws IOException if writing the data to the output failed.
	 * @see #readByteArrayWithIntegerLengthHeader(DataInputStream)
	 * @see #writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])
	 */
	public static void writeByteArrayWithIntegerLengthHeader(DataOutputStream out, byte[] source)
	throws IllegalArgumentException, IOException
	{
		if (out == null)
			throw new IllegalArgumentException("out == null");

		if (source == null)
			throw new IllegalArgumentException("source == null");

		out.writeInt(source.length);
		out.write(source);
	}

	/**
	 * <p>
	 * Read data from <code>in</code> and return it as a byte array.
	 * </p>
	 * <p>
	 * This method first reads 2 bytes which it interpretes as length (i.e. number of bytes) of the actual
	 * data to follow. It then reads as many bytes as the length indicates into a newly created byte array.
	 * </p>
	 * <p>
	 * Data previously written by {@link #writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])} can
	 * be read by this method.
	 * </p>
	 *
	 * @param in the {@link DataInputStream} from which to read.
	 * @return the byte array containing the actual data.
	 * @throws IllegalArgumentException if one of the given arguments is <code>null</code>.
	 * @throws IOException if reading the data from the input failed.
	 * @see #writeByteArrayWithShortLengthHeader(DataOutputStream, byte[])
	 * @see #readByteArrayWithIntegerLengthHeader(DataInputStream)
	 */
	public static byte[] readByteArrayWithShortLengthHeader(DataInputStream in)
	throws IllegalArgumentException, IOException
	{
		if (in == null)
			throw new IllegalArgumentException("in == null");

		int length = in.readShort() & 0xffff;
		byte[] dest = new byte[length];
		readByteArrayCompletely(in, dest);
		return dest;
	}

	/**
	 * <p>
	 * Read data from <code>in</code> and return it as a byte array.
	 * </p>
	 * <p>
	 * This method first reads 4 bytes which it interpretes as length (i.e. number of bytes) of the actual
	 * data to follow. It then reads as many bytes as the length indicates into a newly created byte array.
	 * </p>
	 * <p>
	 * Data previously written by {@link #writeByteArrayWithIntegerLengthHeader(DataOutputStream, byte[])} can
	 * be read by this method.
	 * </p>
	 *
	 * @param in the {@link DataInputStream} from which to read.
	 * @return the byte array containing the actual data.
	 * @throws IllegalArgumentException if one of the given arguments is <code>null</code>.
	 * @throws IOException if reading the data from the input failed.
	 * @see #writeByteArrayWithIntegerLengthHeader(DataOutputStream, byte[])
	 * @see #readByteArrayWithShortLengthHeader(DataInputStream)
	 */
	public static byte[] readByteArrayWithIntegerLengthHeader(DataInputStream in)
	throws IllegalArgumentException, IOException
	{
		if (in == null)
			throw new IllegalArgumentException("in == null");

		int length = in.readInt();
		byte[] dest = new byte[length];
		readByteArrayCompletely(in, dest);
		return dest;
	}

	/**
	 * <p>
	 * Read from <code>in</code> into <code>dest</code>, until the number of bytes
	 * read matches the length of the byte array <code>dest</code>.
	 * </p>
	 * <p>
	 * This method is to be preferred over a simple {@link InputStream#read(byte[])}, because
	 * it already returns when some bytes are read (not necessarily all that are expected). This
	 * method in contrast runs in a loop until really all expected bytes have been read successfully.
	 * If this is not possible, an {@link IOException} is thrown instead (i.e. either read all or nothing).
	 * </p>
	 *
	 * @param in the {@link InputStream} to read from.
	 * @param dest the destination byte array to write into.
	 * @throws IllegalArgumentException if one of the given arguments is <code>null</code>.
	 * @throws IOException if reading data failed; for example the <code>InputStream</code> does not
	 * contain enough bytes (e.g. an underlying file is smaller than expected).
	 */
	public static void readByteArrayCompletely(InputStream in, byte[] dest)
	throws IllegalArgumentException, IOException
	{
		if (in == null)
			throw new IllegalArgumentException("in == null");

		if (dest == null)
			throw new IllegalArgumentException("dest == null");

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
	 * two hex digits are produced. They are concatenated without any separators.
	 * <p>
	 * This is a convenience method for {@link #encodeHexStr(byte[], int, int)}.
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
