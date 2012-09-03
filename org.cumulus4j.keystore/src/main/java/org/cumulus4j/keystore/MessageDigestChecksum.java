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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.Checksum;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class MessageDigestChecksum implements Checksum
{
	public static class SHA1 extends MessageDigestChecksum
	{
		public SHA1() {
			super("SHA1");
		}
	}

	public static class MD5 extends MessageDigestChecksum
	{
		public MD5() {
			super("MD5");
		}
	}

	private MessageDigest messageDigest;

	public MessageDigestChecksum(String algorithm) {
		try {
			messageDigest = MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void update(int b) {
		checksum = null;
		messageDigest.update((byte)b);
	}

	@Override
	public void update(byte[] b, int off, int len) {
		checksum = null;
		messageDigest.update(b, off, len);
	}

	private byte[] checksum = null;

	public byte[] getChecksum()
	{
		if (checksum == null)
			checksum = messageDigest.digest();

		return checksum;
	}

	@Override
	public long getValue()
	{
		byte[] value = getChecksum();
		long result = 0;
		int shift = 0;
		// We don't need to overlap the first and the last bytes, because
		// the hashes distribute pretty well - thus we can simply & safely
		// ignore the rest. Marco.
//		for (int i = 0; i < value.length; ++i) {
		for (int i = 0; i < 8; ++i) {
			result ^= ((long)(value[i]) << (shift * 8));

			if (++shift > 8)
				shift = 0;
		}
		return result;
	}

	@Override
	public void reset()
	{
		checksum = null;
		messageDigest.reset();
	}

//	private byte[] value = new byte[8];
//	private int index = 0;
//
//	@Override
//	public final void update(int b) {
//		value[index] ^= (b & 0xff);
//		if (++index >= value.length)
//			index = 0;
//	}
//
//	@Override
//	public final void update(byte[] b, int off, int len) {
//		for (int i = 0; i < len; ++i) {
//			value[index] ^= (b[i] & 0xff);
//			if (++index >= value.length)
//				index = 0;
//		}
//	}
//
//	@Override
//	public final long getValue() {
//		long result = 0;
//		for (int i = 0; i < value.length; ++i)
//			result |= ((long)value[i] << (i * 8));
//
//		return result;
//	}
//
//	@Override
//	public final void reset() {
//		Arrays.fill(value, (byte)0);
//		index = 0;
//	}
}