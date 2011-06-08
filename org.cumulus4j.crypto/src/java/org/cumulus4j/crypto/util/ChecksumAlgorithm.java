package org.cumulus4j.crypto.util;

import java.util.zip.CRC32;


/**
 * <p>
 * Checksum algorithms currently supported by Cumulus4j's persistent data storage.
 * </p>
 * <p>
 * Checksums are used to find out, if (1) persistent data was tampered with by an attacker
 * or was corrupted for other reasons and (2) whether it was correctly decrypted, i.e. the
 * correct key (and initialisation vector) was used.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public enum ChecksumAlgorithm
{
	/**
	 * Do not use a checksum. <b>This is not recommended!</b>
	 */
	NONE,

	/**
	 * Store a {@link CRC32}-checksum (encrypted) together with the data and
	 * verify this checksum after decrypting the data.
	 */
	CRC32,

	MD5,

	SHA1
	;

	{
		if (ordinal() > 255)
			throw new IllegalStateException("Too many values! Due to the encoding of its ordinal value in a byte, this enum must not contain more than 256 values!");
	}

	/**
	 * Get the <code>ChecksumAlgorithm</code> identified by its {@link Enum#ordinal() ordinal value}
	 * encoded in a byte. This method handles the given byte as if it was UNSIGNED, i.e. there is a limit
	 * of 256 values for this enum.
	 * @param b the byte the <code>ChecksumAlgorithm</code>'s {@link Enum#ordinal() ordinal value}.
	 * @return the <code>ChecksumAlgorithm</code> identified by the given {@link Enum#ordinal() ordinal value}.
	 * @see #toByte()
	 */
	public static ChecksumAlgorithm valueOf(byte b)
	{
		int checksumAlgoID = b & 0xff; // the '& 0xff' is necessary to use the whole UNSIGNED range of a byte, i.e. 0...255.
		if (checksumAlgoID > ChecksumAlgorithm.values().length - 1)
			throw new IllegalArgumentException("checksumAlgoID == " + checksumAlgoID + " (byte value " + b + ") is unknown!");

		return ChecksumAlgorithm.values()[checksumAlgoID];
	}

	/**
	 * Get the {@link Enum#ordinal() ordinal value} as a byte.
	 * @return {@link Enum#ordinal() ordinal value} as a byte.
	 * @see #valueOf(byte)
	 */
	public byte toByte()
	{
		return (byte)ordinal();
	}

}
