package org.cumulus4j.keyserver.front.webapp;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.KeyNotFoundException;
import org.cumulus4j.keystore.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class SessionManager
{
	private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

	private static final long EXPIRY_AGE_MSEC = 3L * 60L * 1000L; // TODO make configurable

	private static Timer expireSessionTimer = new Timer();

	private TimerTask expireSessionTimerTask = new ExpireSessionTimerTask(this);

	private static class ExpireSessionTimerTask extends TimerTask
	{
		private static final Logger logger = LoggerFactory.getLogger(ExpireSessionTimerTask.class);

		private WeakReference<SessionManager> sessionManagerRef;

		public ExpireSessionTimerTask(SessionManager sessionManager) {
			this.sessionManagerRef = new WeakReference<SessionManager>(sessionManager);
		}

		@Override
		public void run()
		{
			SessionManager sessionManager = sessionManagerRef.get();
			if (sessionManager == null) {
				logger.info("run: SessionManager has been garbage-collected. Removing this ExpireSessionTimerTask.");
				this.cancel();
				return;
			}

			Date now = new Date();

			LinkedList<Session> sessionsToExpire = new LinkedList<Session>();
			synchronized (sessionManager) {
				for (Session session : sessionManager.cryptoSessionID2Session.values()) {
					if (session.getExpiry().before(now))
						sessionsToExpire.add(session);
				}
			}

			for (Session session : sessionsToExpire) {
				logger.info("run: Expiring session: userName='{}' cryptoSessionID='{}'.", session.getUserName(), session.getCryptoSessionID());
				session.close();
			}

			if (logger.isDebugEnabled()) {
				synchronized (sessionManager) {
					logger.debug("run: {} sessions left.", sessionManager.cryptoSessionID2Session.size());
				}
			}
		}
	}

	private String cryptoSessionIDPrefix;
	private KeyStore keyStore;

	private Map<String, Session> userName2Session = new HashMap<String, Session>();
	private Map<String, Session> cryptoSessionID2Session = new HashMap<String, Session>();

	public SessionManager(KeyStore keyStore)
	{
		logger.info("Creating instance of SessionManager.");
		this.keyStore = keyStore;
		this.cryptoSessionIDPrefix = IdentifierUtil.createRandomID(26); // about the same uniqueness as a UUID
		expireSessionTimer.schedule(expireSessionTimerTask, 60000, 60000); // TODO make this configurable
	}

	private AtomicLong lastCryptoSessionSerial = new AtomicLong();

	protected long nextCryptoSessionSerial()
	{
		return lastCryptoSessionSerial.incrementAndGet();
	}

	public String getCryptoSessionIDPrefix() {
		return cryptoSessionIDPrefix;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	/**
	 * Open a new or refresh an existing session.
	 *
	 * @return the {@link Session}.
	 * @throws AuthenticationException if the login fails
	 */
	public synchronized Session openSession(String userName, char[] password) throws AuthenticationException
	{
		try {
			keyStore.getKey(userName, password, Long.MAX_VALUE);
		} catch (KeyNotFoundException e) {
			// very likely, the key does not exist - this is expected and OK!
		}

		Session session = userName2Session.get(userName);

		// We make sure we never re-use an expired session, even if it hasn't been closed by the timer yet.
		if (session != null && session.getExpiry().before(new Date())) {
			session.close();
			session = null;
		}

		if (session == null) {
			session = new Session(this, userName, password);
			userName2Session.put(userName, session);
			cryptoSessionID2Session.put(session.getCryptoSessionID(), session);

			// TODO notify listeners - maybe always notify listeners (i.e. when an existing session is refreshed, too)?!
		}

		session.updateLastUse(EXPIRY_AGE_MSEC);

		return session;
	}

	protected synchronized void onCloseSession(Session session)
	{
		// TODO notify listeners

		userName2Session.remove(session.getUserName());
		cryptoSessionID2Session.remove(session.getCryptoSessionID());
		keyStore.clearCache(session.getUserName());
	}

	public synchronized Session getSessionForUserName(String userName)
	{
		Session session = userName2Session.get(userName);
		return session;
	}

	public synchronized Session getSessionForCryptoSessionID(String cryptoSessionID)
	{
		Session session = cryptoSessionID2Session.get(cryptoSessionID);
		return session;
	}
}
