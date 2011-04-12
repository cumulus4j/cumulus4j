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

	protected abstract CryptoSession createSession();

	public CryptoSession getCryptoSession(String cryptoSessionID)
	{
		synchronized (id2session) {
			CryptoSession session = id2session.get(cryptoSessionID);
			if (session == null) {
				session = createSession();
				if (session == null)
					throw new IllegalStateException("Implementation error! " + this.getClass().getName() + ".createSession() returned null!");

				session.setCryptoManager(this);
				session.setCryptoSessionID(cryptoSessionID);

				id2session.put(cryptoSessionID, session);
			}
			return session;
		}
	}
}
