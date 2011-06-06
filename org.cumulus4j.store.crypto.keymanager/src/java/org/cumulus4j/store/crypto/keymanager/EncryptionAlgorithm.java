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
	// TODO shall we support all of them or restrict to optimal solutions? e.g. CFB only with NoPadding?!
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

	private EncryptionAlgorithm() {
		if (ordinal() > 127)
			throw new IllegalStateException("The encryption-algorithm is encoded in the first byte of the ciphertext-byte-array. Thus more than 255 values are not possible and we currently reserve the highest bit for later extensions.");
	}

	private final String transformation = this.name().replace('_', '/');

	public final String getTransformation() {
		return transformation;
	}
}
