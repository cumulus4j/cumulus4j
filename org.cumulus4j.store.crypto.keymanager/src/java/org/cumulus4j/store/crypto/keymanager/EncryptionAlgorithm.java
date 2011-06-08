package org.cumulus4j.store.crypto.keymanager;

/**
 * <p>
 * Encryption algorithms currently supported by Cumulus4j's persistent data storage.
 * </p>
 * <p>
 * For more details (including when you want to extend Cumulus4j) look at
 * <a href="http://download.java.net/jdk7/docs/technotes/guides/security/SunProviders.html#SunJCEProvider">SunJCEProvider</a>.
 * </p>
 * <p>
 * <b>Important:</b> When extending this class, make sure that you do not change the
 * order of the existing enum values, because the algorithm used is encoded via its
 * {@link Enum#ordinal() ordinal value} in the first byte of the encrypted
 * byte array.
 * </p>
 * <p>
 * Note, that Cumulus4j does <b>not</b> support
 * <a href="http://en.wikipedia.org/wiki/Block_cipher_modes_of_operation#Electronic_codebook_.28ECB.29">ECB</a> and never will!
 * This is because ECB is extremely unsafe.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public enum EncryptionAlgorithm
{
	// Question: Shall we support all of them or restrict to optimal solutions? e.g. CFB only with NoPadding?!
	// Answer: We only support what makes sense, because to support all would blow the the byte, already now.
	// We thus slowly add new elements, if really necessary (e.g. if someone asks in the forum).
	AES_CBC_PKCS5Padding,
	AES_CBC_ISO10126Padding,

	AES_CTR_NoPadding,
	AES_CTS_NoPadding,

	AES_CFB_NoPadding,
	AES_OFB_NoPadding,

	Blowfish_CBC_PKCS5Padding,
	Blowfish_CBC_ISO10126Padding,

	Blowfish_CTR_NoPadding,
	Blowfish_CTS_NoPadding,

	Blowfish_CFB_NoPadding,
	Blowfish_OFB_NoPadding,

	Twofish_CBC_PKCS5Padding,
	Twofish_CBC_ISO10126Padding,

	Twofish_CTR_NoPadding,
	Twofish_CTS_NoPadding,

	Twofish_CFB_NoPadding,
	Twofish_OFB_NoPadding
	;

	{
		if (ordinal() > 127)
			throw new IllegalStateException("The encryption-algorithm is encoded in the first byte of the ciphertext-byte-array. Thus more than 255 values are not possible and we currently reserve the highest bit for later extensions.");
	}

	private final String transformation = this.name().replace('_', '/');

	public final String getTransformation() {
		return transformation;
	}

	/**
	 * Get the <code>EncryptionAlgorithm</code> identified by its {@link Enum#ordinal() ordinal value}
	 * encoded in a byte. This method handles the given byte as if it was UNSIGNED, i.e. there is a limit
	 * of 256 values for this enum.
	 * @param b the byte the <code>EncryptionAlgorithm</code>'s {@link Enum#ordinal() ordinal value}.
	 * @return the <code>EncryptionAlgorithm</code> identified by the given {@link Enum#ordinal() ordinal value}.
	 * @see #toByte()
	 */
	public static EncryptionAlgorithm valueOf(byte b)
	{
		int encryptionAlgoID = b & 0xff; // the '& 0xff' is necessary to use the whole UNSIGNED range of a byte, i.e. 0...255.
		if (encryptionAlgoID > EncryptionAlgorithm.values().length - 1)
			throw new IllegalArgumentException("encryptionAlgoID == " + encryptionAlgoID + " (byte value " + b + ") is unknown!");

		return EncryptionAlgorithm.values()[encryptionAlgoID];
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
