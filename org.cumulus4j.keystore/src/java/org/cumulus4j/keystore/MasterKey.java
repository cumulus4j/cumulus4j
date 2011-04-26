package org.cumulus4j.keystore;

import java.security.Key;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class MasterKey
{
	public MasterKey(Key key)
	{
		if (key == null)
			throw new IllegalArgumentException("key == null");

		this.key = key;
	}

	private Key key;

	public Key getKey() {
		return key;
	}

	public void clear()
	{
		byte[] data = key.getEncoded();
		if (data != null) {
			for (int i = 0; i < data.length; i++)
				data[i] = 0;
		}
	}
}
