package org.cumulus4j.api.keymanagement;

import java.util.Date;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractKeyManagerSession implements KeyManagerSession
{
	private Date creationTimestamp = new Date();
	private KeyManager keyManager;
	private String keyManagerSessionID;

	@Override
	public KeyManager getKeyManager() {
		return keyManager;
	}
	@Override
	public void setKeyManager(KeyManager keyManager)
	{
		if (keyManager == null)
			throw new IllegalArgumentException("keyManager == null");

		if (this.keyManager == keyManager)
			return;

		if (this.keyManager != null)
			throw new IllegalStateException("this.keyManager already assigned! Cannot modify!");

		this.keyManager = keyManager;
	}

	@Override
	public String getKeyManagerSessionID()
	{
		return keyManagerSessionID;
	}
	@Override
	public void setKeyManagerSessionID(String keyManagerSessionID)
	{
		if (keyManagerSessionID == null)
			throw new IllegalArgumentException("keyManagerSessionID == null");

		if (keyManagerSessionID.equals(this.keyManagerSessionID))
			return;

		if (this.keyManagerSessionID != null)
			throw new IllegalStateException("this.keyManagerSessionID already assigned! Cannot modify!");

		this.keyManagerSessionID = keyManagerSessionID;
	}

	public Date getCreationTimestamp()
	{
		return creationTimestamp;
	}
}
