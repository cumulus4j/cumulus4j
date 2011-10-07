package org.cumulus4j.keymanager.api.internal.local;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import org.cumulus4j.keymanager.AppServer;
import org.cumulus4j.keymanager.api.AuthenticationException;
import org.cumulus4j.keymanager.api.Session;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class LocalSession implements Session
{
	private LocalKeyManagerAPI localKeyManagerAPI;

	private AppServer appServer;

	public LocalSession(LocalKeyManagerAPI localKeyManagerAPI, AppServer appServer)
	{
		if (localKeyManagerAPI == null)
			throw new IllegalArgumentException("localKeyManagerAPI == null");

		this.localKeyManagerAPI = localKeyManagerAPI;
		this.appServer = appServer;
	}

	@Override
	public String getAppServerID() {
		return appServer.getAppServerID();
	}

	@Override
	public String getAppServerBaseURL() {
		return appServer.getAppServerBaseURL();
	}

	@Override
	public String getCryptoSessionID() throws AuthenticationException, IOException {
		try {
			return appServer.getSessionManager().openSession(localKeyManagerAPI.getAuthUserName(), localKeyManagerAPI.getAuthPassword()).getCryptoSessionID();
		} catch (org.cumulus4j.keystore.AuthenticationException e) {
			throw new AuthenticationException(e);
		}
	}

	private AtomicInteger unlockCounter = new AtomicInteger();

	@Override
	public void lock() throws AuthenticationException, IOException
	{
		int counter = unlockCounter.decrementAndGet();

		if (counter < 0)
			throw new IllegalStateException("lock() called more often than unlock()!!!");

		if (counter > 0)
			return;

		try {
			appServer.getSessionManager().openSession(localKeyManagerAPI.getAuthUserName(), localKeyManagerAPI.getAuthPassword()).setLocked(true);
		} catch (org.cumulus4j.keystore.AuthenticationException e) {
			throw new AuthenticationException(e);
		}
	}

	@Override
	public void unlock() throws AuthenticationException, IOException
	{
		unlockCounter.incrementAndGet();

		// We do *NOT* check the result of unlockCounter.incrementAndGet(), because we 1st can't
		// ensure without synchronisation that the session will be unlocked when this method returns
		// and 2nd, we want openSession(...) to be called in order to update the last-usage-timestamp.
		// Marco :-)

		try {
			appServer.getSessionManager().openSession(localKeyManagerAPI.getAuthUserName(), localKeyManagerAPI.getAuthPassword()).setLocked(false);
		} catch (org.cumulus4j.keystore.AuthenticationException e) {
			throw new AuthenticationException(e);
		}
	}

	@Override
	public void close() {
		org.cumulus4j.keymanager.Session underlyingSession = appServer.getSessionManager().getSessionForUserName(localKeyManagerAPI.getAuthUserName());
		if (underlyingSession != null)
			underlyingSession.close();
	}

}
