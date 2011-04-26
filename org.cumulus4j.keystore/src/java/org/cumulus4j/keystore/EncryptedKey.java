package org.cumulus4j.keystore;

public class EncryptedKey
{
	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	public EncryptedKey(byte[] data, byte[] salt, String algorithm)
	{
		if (data == null)
			throw new IllegalArgumentException("data must not be null!");

		if (salt == null)
			salt = EMPTY_BYTE_ARRAY;

		if (algorithm == null)
			throw new IllegalArgumentException("algorithm must not be null!");

		this.data = data;
		this.salt = salt;
		this.algorithm = algorithm;
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
}
