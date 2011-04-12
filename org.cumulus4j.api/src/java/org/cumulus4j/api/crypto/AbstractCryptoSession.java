package org.cumulus4j.api.crypto;

import java.util.Date;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractCryptoSession implements CryptoSession
{
	private Date creationTimestamp = new Date();
	private CryptoManager cryptoManager;
	private String keyManagerSessionID;

	@Override
	public CryptoManager getCryptoManager() {
		return cryptoManager;
	}
	@Override
	public void setCryptoManager(CryptoManager cryptoManager)
	{
		if (cryptoManager == null)
			throw new IllegalArgumentException("cryptoManager == null");

		if (this.cryptoManager == cryptoManager)
			return;

		if (this.cryptoManager != null)
			throw new IllegalStateException("this.keyManager already assigned! Cannot modify!");

		this.cryptoManager = cryptoManager;
	}

	@Override
	public String getCryptoSessionID()
	{
		return keyManagerSessionID;
	}
	@Override
	public void setCryptoSessionID(String keyManagerSessionID)
	{
		if (keyManagerSessionID == null)
			throw new IllegalArgumentException("keyManagerSessionID == null");

		if (keyManagerSessionID.equals(this.keyManagerSessionID))
			return;

		if (this.keyManagerSessionID != null)
			throw new IllegalStateException("this.keyManagerSessionID already assigned! Cannot modify!");

		this.keyManagerSessionID = keyManagerSessionID;
	}

	@Override
	public Date getCreationTimestamp()
	{
		return creationTimestamp;
	}
}
