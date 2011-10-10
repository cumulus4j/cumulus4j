/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.keymanager;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicLong;

import org.cumulus4j.keymanager.back.shared.IdentifierUtil;
import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.KeyNotFoundException;
import org.cumulus4j.keystore.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Manager for {@link Session}s.
 * </p>
 * <p>
 * There is one <code>SessionManager</code> for each {@link AppServer}. It provides the functionality to
 * open and close sessions, expire them automatically after a certain time etc.
 * </p>
 * <p>
 * This is not API! Use the classes and interfaces provided by <code>org.cumulus4j.keymanager.api</code> instead.
 * </p>
 *
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

		public ExpireSessionTimerTask(SessionManager sessionManager)
		{
			if (sessionManager == null)
				throw new IllegalArgumentException("sessionManager == null");

			this.sessionManagerRef = new WeakReference<SessionManager>(sessionManager);
		}

		@Override
		public void run()
		{
			try {
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
					session.destroy();
				}

				if (logger.isDebugEnabled()) {
					synchronized (sessionManager) {
						logger.debug("run: {} sessions left.", sessionManager.cryptoSessionID2Session.size());
					}
				}
			} catch (Throwable x) {
				// The TimerThread is cancelled, if a task throws an exception. Furthermore, they are not logged at all.
				// Since we do not want the TimerThread to die, we catch everything (Throwable - not only Exception) and log
				// it here. IMHO there's nothing better we can do. Marco :-)
				logger.error("run: " + x, x);
			}
		}
	}

	private String cryptoSessionIDPrefix;
	private KeyStore keyStore;

	private Map<String, List<Session>> userName2SessionList = new HashMap<String, List<Session>>();
	private Map<String, Session> cryptoSessionID2Session = new HashMap<String, Session>();

	public SessionManager(KeyStore keyStore)
	{
		logger.info("Creating instance of SessionManager.");
		this.keyStore = keyStore;
		this.cryptoSessionIDPrefix = IdentifierUtil.createRandomID();
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

	private static final void doNothing() { }

	protected synchronized void onReacquireSession(Session session)
	{
		if (session == null)
			throw new IllegalArgumentException("session == null");

		if (cryptoSessionID2Session.get(session.getCryptoSessionID()) != session)
			throw new IllegalStateException("The session with cryptoSessionID=\"" + session.getCryptoSessionID() + "\" is not known. Dead reference already expired and destroyed?");

		if (session.getExpiry().before(new Date()))
			throw new IllegalStateException("The session with cryptoSessionID=\"" + session.getCryptoSessionID() + "\" is already expired. It is still known, but cannot be reacquired anymore!");

		session.updateLastUse(EXPIRY_AGE_MSEC);
	}

	/**
	 * Create a new unlocked session or open (unlock) a cached &amp; currently locked session.
	 *
	 * @return the {@link Session}.
	 * @throws AuthenticationException if the login fails
	 */
	public synchronized Session acquireSession(String userName, char[] password) throws AuthenticationException
	{
		try {
			keyStore.getKey(userName, password, Long.MAX_VALUE);
		} catch (KeyNotFoundException e) {
			// very likely, the key does not exist - this is expected and OK!
			doNothing(); // Remove warning from PMD report: http://cumulus4j.org/latest-dev/pmd.html
		}

		List<Session> sessionList = userName2SessionList.get(userName);
		if (sessionList == null) {
			sessionList = new LinkedList<Session>();
			userName2SessionList.put(userName, sessionList);
		}

		Session session = null;
		List<Session> sessionsToClose = null;
		for (Session s : sessionList) {
			// We make sure we never re-use an expired session, even if it hasn't been closed by the timer yet.
			if (s.getExpiry().before(new Date())) {
				if (sessionsToClose == null)
					sessionsToClose = new LinkedList<Session>();

				sessionsToClose.add(s);
				continue;
			}

			if (s.isReleased()) {
				session = s;
				break;
			}
		}

		if (sessionsToClose != null) {
			for (Session s : sessionsToClose)
				s.destroy();
		}

		if (session == null) {
			session = new Session(this, userName, password);
			sessionList.add(session);
			cryptoSessionID2Session.put(session.getCryptoSessionID(), session);

			// TODO notify listeners - maybe always notify listeners (i.e. when an existing session is refreshed, too)?!
		}

		session.setReleased(false);
		session.updateLastUse(EXPIRY_AGE_MSEC);

		return session;
	}

	protected synchronized void onDestroySession(Session session)
	{
		if (session == null)
			throw new IllegalArgumentException("session == null");

		// TODO notify listeners
		List<Session> sessionList = userName2SessionList.get(session.getUserName());
		if (sessionList == null)
			logger.warn("onDestroySession: userName2SessionList.get(\"{}\") returned null!", session.getUserName());
		else {
			for (Iterator<Session> it = sessionList.iterator(); it.hasNext();) {
				Session s = it.next();
				if (s == session) {
					it.remove();
					break;
				}
			}
		}

		cryptoSessionID2Session.remove(session.getCryptoSessionID());

		if (sessionList == null || sessionList.isEmpty()) {
			userName2SessionList.remove(session.getUserName());
			keyStore.clearCache(session.getUserName());
		}
	}

//	public synchronized Session getSessionForUserName(String userName)
//	{
//		Session session = userName2Session.get(userName);
//		return session;
//	}

	public synchronized Session getSessionForCryptoSessionID(String cryptoSessionID)
	{
		Session session = cryptoSessionID2Session.get(cryptoSessionID);
		return session;
	}

	public synchronized void onReleaseSession(Session session)
	{
		if (session == null)
			throw new IllegalArgumentException("session == null");

		session.setReleased(true);
	}
}
