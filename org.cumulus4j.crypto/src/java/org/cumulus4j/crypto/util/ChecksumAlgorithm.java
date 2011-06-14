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
	 * <p>
	 * Do not use any checksum.
	 * </p>
	 * <p>
	 * <b>This is not recommended in most situations!</b> It neither detects accidental
	 * data corruption nor intentional alteration of data nor whether the correct key+IV
	 * was used at all. Only use it in combination with a cipher transformation
	 * that is known to already include a
	 * <a href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a>!
	 * Better don't use it at all, if you're unsure!
	 * </p>
	 */
	NONE,

	/**
	 * Store a {@link CRC32}-checksum (encrypted) together with the data and
	 * verify this checksum after decrypting the data.
	 * <p>
	 * <b>This is not recommended!</b> Even
	 * though it detects accidental data corruption as well as whether the correct key+IV was used,
	 * it does not detect intentional alteration of data.
	 * </p>
	 * <p>
	 * See <a href="http://en.wikipedia.org/wiki/CRC32">CRC32</a>
	 * and <a href="http://en.wikipedia.org/wiki/Message_authentication_code">Message authentication code</a> in Wikipedia.
	 * </p>
	 */
	CRC32,

	/**
	 * Store a <a href="http://en.wikipedia.org/wiki/MD5">MD5</a> hash (encrypted) together with the data
	 * and verify this checksum after decrypting the data.
	 * <p>
	 * <b>This is not recommended!</b> Even
	 * though it is far more secure than {@link #NONE} and {@link #CRC32}, MD5 is known to have many
	 * vulnerabilities.
	 * </p>
	 */
	MD5,

	/**
	 * Store an <a href="http://en.wikipedia.org/wiki/SHA1">SHA1</a> hash (encrypted) together with the data
	 * and verify this checksum after decrypting the data.
	 */
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
