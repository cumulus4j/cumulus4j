package org.cumulus4j.api.crypto;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractCryptoManager implements CryptoManager
{
	private String keyManagerID;

	private Map<String, CryptoSession> id2session = new HashMap<String, CryptoSession>();

	@Override
	public String getCryptoManagerID() {
		return keyManagerID;
	}

	@Override
	public void setCryptoManagerID(String cryptoManagerID)
	{
		if (cryptoManagerID == null)
			throw new IllegalArgumentException("keyManagerID == null");

		if (cryptoManagerID.equals(this.keyManagerID))
			return;

		if (this.keyManagerID != null)
			throw new IllegalStateException("this.keyManagerID is already assigned and cannot be modified!");

		this.keyManagerID = cryptoManagerID;
	}

	protected abstract CryptoSession createCryptoSession();

	public CryptoSession acquireCryptoSession(String cryptoSessionID)
	{
		CryptoSession session = null;

		int tryCounter = 0;
		do {
			synchronized (id2session) {
				session = id2session.get(cryptoSessionID);
				if (session == null) {
					session = createCryptoSession();
					if (session == null)
						throw new IllegalStateException("Implementation error! " + this.getClass().getName() + ".createSession() returned null!");

					session.setCryptoManager(this);
					session.setCryptoSessionID(cryptoSessionID);

					id2session.put(cryptoSessionID, session);
				}
			}

			if (!session.onAcquire()) {
				// The session is already closed. Throw it away.
				// This can only happen, if it is right now being closed => try it simply again.
				// Marco.
				session.close(); // Make 100% sure, it is removed from the i2session-map.
				session = null;

				if (++tryCounter > 3)
					throw new IllegalStateException("Could not obtain a usable Session!");
			}
		} while (session == null);

		return session;
	}

	@Override
	public void onReleaseCryptoSession(AbstractCryptoSession abstractCryptoSession) {

	}

	@Override
	public void onCloseCryptoSession(CryptoSession cryptoSession)
	{
		synchronized (id2session) {
			id2session.remove(cryptoSession.getCryptoSessionID());
		}
	}
}
