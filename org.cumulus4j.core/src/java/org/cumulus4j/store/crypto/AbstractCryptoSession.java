package org.cumulus4j.store.crypto;

import java.util.Date;

/**
 * Abstract base-class for implementing {@link CryptoSession}s.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractCryptoSession implements CryptoSession
{
	private Date creationTimestamp = new Date();
	private volatile Date lastUsageTimestamp = creationTimestamp;
	private CryptoManager cryptoManager;
	private String cryptoSessionID;

	private volatile boolean closed;

	@Override
	public CryptoManager getCryptoManager() {
		return cryptoManager;
	}

	@Override
	public void setCryptoManager(CryptoManager cryptoManager)
	{
		if (cryptoManager == null)
			throw new IllegalArgumentException("cryptoManager == null");

		if (cryptoManager == this.cryptoManager)
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

	/**
	 * {@inheritDoc}
	 * <p>
	 * Implementors should use {@link #assertNotClosed()} in their subclasses to check
	 * whether the operation is still allowed.
	 * </p>
	 */
	@Override
	public boolean isClosed() {
		return closed;
	}

	/**
	 * Throws an {@link IllegalStateException}, if this session is already closed.
	 */
	protected void assertNotClosed()
	{
		if (isClosed())
			throw new IllegalStateException("This session (cryptoSessionID=\"" + cryptoSessionID + "\") is already closed!");
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * When overriding this method, you should first call <code>super.close();</code> and
	 * then perform your own closing operations.
	 * </p>
	 */
	@Override
	public void close() {
		closed = true;
		cryptoManager.onCloseCryptoSession(this);
	}
}
