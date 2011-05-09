package org.cumulus4j.core.crypto;

/**
 * Object representing unencrypted information (aka <a href="http://en.wikipedia.org/wiki/Plaintext">plaintext</a>).
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Plaintext
{
	private byte[] data;

	/**
	 * Get the unencrypted data.
	 * @return the unencrypted data or <code>null</code>.
	 */
	public byte[] getData() {
		return data;
	}
	/**
	 * Set the unencrypted data.
	 * @param data the unencrypted data or <code>null</code>.
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
}
