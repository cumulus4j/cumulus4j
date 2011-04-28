package org.cumulus4j.keystore;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class EncryptedKey
{
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	public EncryptedKey(
			byte[] data, byte[] salt, String algorithm, byte[] keyEncryptionIV, String keyEncryptionAlgorithm, short hashSize, String hashAlgorithm
	)
	{
		if (data == null)
			throw new IllegalArgumentException("data must not be null!");

		if (salt == null)
			salt = EMPTY_BYTE_ARRAY;

		if (algorithm == null)
			throw new IllegalArgumentException("algorithm must not be null!");

		if (keyEncryptionAlgorithm == null)
			throw new IllegalArgumentException("keyEncryptionAlgorithm must not be null!");

		if (hashSize < 0)
			throw new IllegalArgumentException("hashSize < 0");

		if (hashAlgorithm == null)
			hashAlgorithm = "";

		if (hashAlgorithm.isEmpty() && hashSize > 0)
			throw new IllegalArgumentException("hashAlgorithm must not be null and not be empty, if hashSize > 0!");

		if (!hashAlgorithm.isEmpty() && hashSize == 0)
			throw new IllegalArgumentException("hashAlgorithm must be null or empty, if hashSize == 0!");

		this.data = data;
		this.salt = salt;
		this.algorithm = algorithm;
		this.keyEncryptionIV = keyEncryptionIV;
		this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
		this.hashSize = hashSize;
		this.hashAlgorithm = hashAlgorithm;
	}

	private byte[] data;

	public byte[] getData() {
		return data;
	}

	private byte[] salt;

	public byte[] getSalt() {
		return salt;
	}

	private String algorithm;

	public String getAlgorithm() {
		return algorithm;
	}

	byte[] keyEncryptionIV;

	public byte[] getKeyEncryptionIV() {
		return keyEncryptionIV;
	}

	private String keyEncryptionAlgorithm;

	public String getKeyEncryptionAlgorithm() {
		return keyEncryptionAlgorithm;
	}

	private short hashSize;

	public short getHashSize() {
		return hashSize;
	}

	private String hashAlgorithm;

	public String getHashAlgorithm() {
		return hashAlgorithm;
	}
}
