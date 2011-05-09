package org.cumulus4j.core.crypto;

/**
 * Object representing encrypted information (aka <a href="http://en.wikipedia.org/wiki/Ciphertext">ciphertext</a>).
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Ciphertext
{
	private long keyID = -1;

	/**
	 * Get the identifier of the key used to encrypt the {@link #getData() data}.
	 *
	 * @return the key identifier.
	 */
	public long getKeyID() {
		return keyID;
	}
	/**
	 * Set the identifier of the key used to encrypt the {@link #getData() data}.
	 * @param keyID the key identifier.
	 */
	public void setKeyID(long keyID) {
		this.keyID = keyID;
	}

	private byte[] data;

	/**
	 * Get the encrypted information.
	 * @return the encrypted information or <code>null</code>.
	 */
	public byte[] getData() {
		return data;
	}
	/**
	 * Set the encrypted information.
	 * @param data the encrypted information or <code>null</code>.
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
}
