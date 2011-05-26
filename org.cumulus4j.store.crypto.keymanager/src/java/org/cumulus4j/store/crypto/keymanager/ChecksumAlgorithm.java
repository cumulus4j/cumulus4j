package org.cumulus4j.store.crypto.keymanager;

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
	 * Do not use a checksum. This is not recommended!
	 */
	none,

	/**
	 * Store a {@link CRC32}-checksum (encrypted) together with the data and
	 * verify this checksum after decrypting the data.
	 */
	crc32
}
