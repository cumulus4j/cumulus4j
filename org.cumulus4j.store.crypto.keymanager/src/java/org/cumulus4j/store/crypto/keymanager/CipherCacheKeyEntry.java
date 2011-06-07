package org.cumulus4j.store.crypto.keymanager;

import java.util.Arrays;
import java.util.Date;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class CipherCacheKeyEntry
{
	public CipherCacheKeyEntry(long keyID, byte[] keyData)
	{
		if (keyID < 0)
			throw new IllegalArgumentException("keyID < 0");

		if (keyData == null)
			throw new IllegalArgumentException("keyData == null");

		this.keyID = keyID;
		this.keyData = keyData.clone(); // necessary, because we overwrite the keyData in the finalize() method.
	}

	private long keyID = -1;

	private byte[] keyData;

	private Date lastUse = new Date();

	public long getKeyID() {
		return keyID;
	}
	public byte[] getKeyData() {
		return keyData;
	}

	public Date getLastUse() {
		return lastUse;
	}

	@Override
	protected void finalize() throws Throwable
	{
		Arrays.fill(keyData, (byte)0);
		super.finalize();
	}
}
