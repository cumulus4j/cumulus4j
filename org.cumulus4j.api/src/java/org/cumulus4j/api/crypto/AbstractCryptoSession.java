package org.cumulus4j.api.crypto;

import java.util.Date;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractCryptoSession implements CryptoSession
{
	private Date creationTimestamp = new Date();
	private Date lastUsageTimestamp = new Date();
	private CryptoManager cryptoManager;
	private String cryptoSessionID;

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
			throw new IllegalStateException("this.cryptoManager already assigned! Cannot modify!");

		this.cryptoManager = cryptoManager;
	}

	@Override
	public String getCryptoSessionID()
	{
		return cryptoSessionID;
	}

	@Override
	public void setCryptoSessionID(String cryptoSessionID)
	{
		if (cryptoSessionID == null)
			throw new IllegalArgumentException("cryptoSessionID == null");

		if (cryptoSessionID.equals(this.cryptoSessionID))
			return;

		if (this.cryptoSessionID != null)
			throw new IllegalStateException("this.cryptoSessionID already assigned! Cannot modify!");

		this.cryptoSessionID = cryptoSessionID;
	}

	@Override
	public Date getCreationTimestamp()
	{
		return creationTimestamp;
	}

	@Override
	public Date getLastUsageTimestamp() {
		return lastUsageTimestamp;
	}

	@Override
	public void updateLastUsageTimestamp() {
		lastUsageTimestamp = new Date();
	}
}
