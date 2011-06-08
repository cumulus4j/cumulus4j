package org.cumulus4j.keystore;


/**
 * Container holding a {@link #getKeyID() keyID} and a {@link #getKey() key}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class GeneratedKey
{
	private long keyID;
	private byte[] key;

	/**
	 * Constructor for instantiating a <code>GeneratedKey</code> with a <code>keyID</code> and the actual <code>key</code>.
	 *
	 * @param keyID the identifier of the key.
	 * @param key the actual key.
	 */
	public GeneratedKey(long keyID, byte[] key) {
		this.keyID = keyID;
		this.key = key;
	}

	/**
	 * Get the key-identifier.
	 *
	 * @return the key-identifier.
	 */
	public long getKeyID() {
		return keyID;
	}

	/**
	 * Get the actual key.
	 *
	 * @return the key.
	 */
	public byte[] getKey() {
		return key;
	}
}
