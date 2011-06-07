package org.cumulus4j.crypto.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

public class ChecksumCalculator
{
	private MessageDigest checksum_md_sha1;
	private MessageDigest checksum_md_md5;
	private CRC32 checksum_crc32;

	public synchronized byte[] checksum(byte[] data, ChecksumAlgorithm algorithm)
	{
		return checksum(data, 0, data.length, algorithm);
	}

	public synchronized byte[] checksum(byte[] data, int dataOffset, int dataLength, ChecksumAlgorithm algorithm)
	{
		switch (algorithm) {
			case NONE:
				return new byte[0];
			case CRC32:
				if (checksum_crc32 == null)
					checksum_crc32 = new CRC32();
				else
					checksum_crc32.reset();

				checksum_crc32.update(data, dataOffset, dataLength);
				long crc32Value = checksum_crc32.getValue();
				byte[] result = new byte[4]; // CRC 32 has only 32 bits (= 4 bytes), hence the name
				for (int i = 0; i < result.length; ++i)
					result[i] = (byte)(crc32Value >>> (i * 8));

				return result;
			case MD5:
				if (checksum_md_md5 == null) {
					try {
						checksum_md_md5 = MessageDigest.getInstance("MD5");
					} catch (NoSuchAlgorithmException e) {
						throw new RuntimeException(e);
					}
				}
				checksum_md_md5.update(data, dataOffset, dataLength);
				return checksum_md_md5.digest();
			case SHA1:
				if (checksum_md_sha1 == null) {
					try {
						checksum_md_sha1 = MessageDigest.getInstance("SHA1");
					} catch (NoSuchAlgorithmException e) {
						throw new RuntimeException(e);
					}
				}
				checksum_md_sha1.update(data, dataOffset, dataLength);
				return checksum_md_sha1.digest();
			default:
				throw new UnsupportedOperationException("Unsupported hash algorithm: " + algorithm);
		}
	}

	public synchronized int checksum(
			byte[] data, int dataOffset, int dataLength,
			ChecksumAlgorithm algorithm,
			byte[] out, int outOffset
	)
	{
		byte[] checksum = checksum(data, dataOffset, dataLength, algorithm);
		System.arraycopy(checksum, 0, out, outOffset, checksum.length);
		return checksum.length;
	}
}
