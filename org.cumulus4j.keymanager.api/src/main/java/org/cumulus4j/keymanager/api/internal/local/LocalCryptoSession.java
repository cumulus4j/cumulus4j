package org.cumulus4j.keymanager.api.internal.local;

import java.io.IOException;

import org.cumulus4j.keymanager.AppServer;
import org.cumulus4j.keymanager.api.AuthenticationException;
import org.cumulus4j.keymanager.api.CryptoSession;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class LocalCryptoSession implements CryptoSession
{
	private LocalKeyManagerAPI localKeyManagerAPI;

	private AppServer appServer;

	public LocalCryptoSession(LocalKeyManagerAPI localKeyManagerAPI, AppServer appServer)
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

//	@Override
//	public String getCryptoSessionID() throws AuthenticationException, IOException
//	{
//		org.cumulus4j.keymanager.Session rs = realSession;
//		if (rs == null)
//			return null;
//		else
//			return rs.getCryptoSessionID();
////		try {
////			return appServer.getSessionManager().openSession(localKeyManagerAPI.getAuthUserName(), localKeyManagerAPI.getAuthPassword()).getCryptoSessionID();
////		} catch (org.cumulus4j.keystore.AuthenticationException e) {
////			throw new AuthenticationException(e);
////		}
//	}

	private int unlockCounter = 0;

	private org.cumulus4j.keymanager.Session realSession = null;

	@Override
	public synchronized String acquire() throws AuthenticationException, IOException
	{
		++unlockCounter;
		if (realSession == null) {
			try {
				realSession = appServer.getSessionManager().acquireSession(localKeyManagerAPI.getAuthUserName(), localKeyManagerAPI.getAuthPassword());
			} catch (org.cumulus4j.keystore.AuthenticationException e) {
				throw new AuthenticationException(e);
			}
		}
		else {
			realSession.reacquire();
		}
		return realSession.getCryptoSessionID();
	}

	@Override
	public synchronized void release() throws AuthenticationException, IOException
	{
		int counter = --unlockCounter;

		if (counter < 0)
			throw new IllegalStateException("lock() called more often than unlock()!!!");

		if (counter > 0)
			return;

		if (realSession == null)
			throw new IllegalStateException("unlockCounter inconsistent with realSession! realSession is null!");
		realSession.release();
		realSession = null;
	}

//	@Override
//	public void close() {
//		org.cumulus4j.keymanager.Session underlyingSession = appServer.getSessionManager().getSessionForUserName(localKeyManagerAPI.getAuthUserName());
//		if (underlyingSession != null)
//			underlyingSession.close();
//	}

}
