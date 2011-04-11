package org.cumulus4j.api.keymanagement;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractKeyManager implements KeyManager
{
	private String keyManagerID;

	private Map<String, KeyManagerSession> id2session = new HashMap<String, KeyManagerSession>();

	@Override
	public String getKeyManagerID() {
		return keyManagerID;
	}

	@Override
	public void setKeyManagerID(String keyManagerID)
	{
		if (keyManagerID == null)
			throw new IllegalArgumentException("keyManagerID == null");

		if (keyManagerID.equals(this.keyManagerID))
			return;

		if (this.keyManagerID != null)
			throw new IllegalStateException("this.keyManagerID is already assigned and cannot be modified!");

		this.keyManagerID = keyManagerID;
	}

	protected abstract KeyManagerSession createSession();

	public KeyManagerSession getKeyManagerSession(String keyManagerSessionID)
	{
		synchronized (id2session) {
			KeyManagerSession session = id2session.get(keyManagerSessionID);
			if (session == null) {
				session = createSession();
				if (session == null)
					throw new IllegalStateException("Implementation error! " + this.getClass().getName() + ".createSession() returned null!");

				session.setKeyManager(this);
				session.setKeyManagerSessionID(keyManagerSessionID);

				id2session.put(keyManagerSessionID, session);
			}
			return session;
		}
	}
}
