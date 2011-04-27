package org.cumulus4j.keyserver.front.webapp;

import java.util.HashMap;
import java.util.Map;

import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.LoginException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager
{
	private static final Logger logger = LoggerFactory.getLogger(SessionManager.class);

	private KeyStore keyStore;

	private Map<String, Session> userName2Session = new HashMap<String, Session>();
	private Map<String, Session> cryptoSessionID2Session = new HashMap<String, Session>();

	public SessionManager(KeyStore keyStore)
	{
		logger.info("Creating instance of SessionManager.");
		this.keyStore = keyStore;
	}

	/**
	 * Open a new or refresh an existing session.
	 *
	 * @return the {@link Session}.
	 * @throws LoginException if the login fails
	 */
	public synchronized Session openSession(String userName, char[] password) throws LoginException
	{
		keyStore.getKey(userName, password, Long.MAX_VALUE); // this method will always return null, if the key does not exist - but it will throw a LoginException, if the credentials are invalid.

		Session session = userName2Session.get(userName);

		if (session == null) {
			session = new Session(this, userName, password);
			userName2Session.put(userName, session);
			cryptoSessionID2Session.put(session.getCryptoSessionID(), session);

			// TODO notify listeners
		}
		else
			session.updateLastUse();

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
