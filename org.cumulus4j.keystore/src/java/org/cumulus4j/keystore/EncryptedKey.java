package org.cumulus4j.keystore;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class EncryptedKey
{
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	public EncryptedKey(
			byte[] data, byte[] salt, String algorithm, byte[] keyEncryptionIV, String keyEncryptionAlgorithm, short checksumSize, String checksumAlgorithm
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

		if (checksumSize <= 0)
			throw new IllegalArgumentException("checksumSize <= 0");

		if (checksumAlgorithm == null || checksumAlgorithm.isEmpty())
			throw new IllegalArgumentException("checksumAlgorithm must not be null and not be empty!");

		this.data = data;
		this.salt = salt;
		this.algorithm = algorithm;
		this.keyEncryptionIV = keyEncryptionIV;
		this.keyEncryptionAlgorithm = keyEncryptionAlgorithm;
		this.checksumSize = checksumSize;
		this.checksumAlgorithm = checksumAlgorithm;
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

	private short checksumSize;

	public short getChecksumSize() {
		return checksumSize;
	}

	private String checksumAlgorithm;

	public String getChecksumAlgorithm() {
		return checksumAlgorithm;
	}
}
