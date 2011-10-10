package org.cumulus4j.keymanager.api;

import java.io.IOException;

/**
 * <p>
 * Crypto-session in which key transfers can be performed.
 * </p><p>
 * Use {@link KeyManagerAPI#getSession(String)} to get a <code>Session</code> instance.
 * This instance is a proxy which can be kept and never expires (though the underlying real session will expire if
 * not used for some time). If the underlying real
 * session expired, a new underlying session with a new {@link #getCryptoSessionID() sessionID}
 * will be created and bound to this <code>Session</code> instance.
 * </p><p>
 * Sessions are thread-safe.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface Session
{
	/**
	 * Get the identifier of the application server. This denotes a logical application server, which
	 * can be composed of many physical machines (in a cluster/cloud).
	 * @return the application server's ID; never <code>null</code>.
	 */
	String getAppServerID();

	/**
	 * Get the base-url of the app-server-key-manager-channel. This is the part of the URL before the "/KeyManagerChannel" -
	 * e.g. if the REST URL of the KeyManagerChannel-service is
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keymanager.back.webapp/KeyManagerChannel", then this must be
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keymanager.back.webapp".
	 * @return the base-URL before the "/KeyManagerChannel".
	 */
	String getAppServerBaseURL();

//	/**
//	 * <p>
//	 * Get the ID of the underlying real session or <code>null</code>, if there is none.
//	 * </p><p>
//	 * Multiple calls to this method might return different values! When this method is called while there
//	 * is no underlying session {@link #acquire() acquired}, <code>null</code> is returned. The session-ID
//	 * returned by this method is valid
//	 * only for a certain time (within an acquire-release-block) and should therefore not be kept in memory outside of
//	 * an acquire-release-block!
//	 * </p><p>
//	 * An underlying session is only existing, if {@link #acquire()} has been called (and {@link #release()} not yet).
//	 * </p>
//	 * @return the crypto-session-ID to be passed via the application's main communication channel
//	 * from the client to the app server.
//	 * @throws AuthenticationException if the authentication fails. This might happen for example, when
//	 * a session was created and then the password was modified by another instance of {@link KeyManagerAPI}.
//	 * Calling {@link KeyManagerAPI#putUser(String, char[])} automatically updates the authentication information
//	 * of the current <code>KeyManagerAPI</code> if the current user's password was changed. But if the password
//	 * is changed by another instance, this instance is locked out due to its outdated password.
//	 * @throws IOException if communication with the key-store failed. This might be a socket error between
//	 * client and remote key server or it might be a problem when reading/writing data in the local file system.
//	 */
//	String getCryptoSessionID() throws AuthenticationException, IOException;

	/**
	 * <p>
	 * Acquire an unlocked underlying real session.
	 * </p><p>
	 * The application server is only able to request keys from the key manager, while a crypto-session is
	 * acquired. It thus needs to be acquired, first, before it can be used for key transfers.
	 * </p><p>
	 * <b>Important:</b> It is essential that you call {@link #release()} once for every time you called <code>acquire()</code>.
	 * You should therefore use a try-finally-block like this:
	 * </p>
	 * <pre>
	 * String cryptoSessionID = session.acquire();
	 * try {
	 *
	 * 	// Do some operation that requires key access. For example
	 * 	// call an EJB method or perform a SOAP/REST request which
	 * 	// will make your app server read/write data.
	 *
	 * } finally {
	 * 	session.release();
	 * }
	 * </pre>
	 * <p>
	 * If multiple threads use the same session (recommended!), the underlying real session will be unlocked
	 * when the first thread requires it and it will be locked again when the last thread calls <code>lock()</code>.
	 * However, locking does not need to happen immediately. Instead it can be deferred a few seconds, in case a
	 * new <code>unlock()</code> would happen quickly again. This
	 * strategy is usually used with a remote key server (when latency makes unlocking/locking a pretty expensive
	 * operation).
	 * </p>
	 * @return the cryptoSessionID to be used within the acquire-release-block for key-management. This ID must be
	 * passed to your application server in order to allow it perform database operations.
	 * @throws AuthenticationException if the authentication fails. This might happen for example, when
	 * a session was created and then the password was modified by another instance of {@link KeyManagerAPI}.
	 * Calling {@link KeyManagerAPI#putUser(String, char[])} automatically updates the authentication information
	 * of the current <code>KeyManagerAPI</code> if the current user's password was changed. But if the password
	 * is changed by another instance, this instance is locked out due to its outdated password.
	 * @throws IOException if communication with the key-store failed. This might be a socket error between
	 * client and remote key server or it might be a problem when reading/writing data in the local file system.
	 * @see #release()
	 */
	String acquire() throws AuthenticationException, IOException;

	/**
	 * <p>
	 * Lock the session, after it was previously {@link #acquire() unlocked}.
	 * </p><p>
	 * For every call to {@link #acquire()}, there must be exactly one call to {@link #release()}. You should
	 * therefore use a try-finally-block!
	 * </p><p>
	 * See {@link #acquire()} for further details.
	 * </p>
	 *
	 * @throws AuthenticationException if the authentication fails. This might happen for example, when
	 * a session was created and then the password was modified by another instance of {@link KeyManagerAPI}.
	 * Calling {@link KeyManagerAPI#putUser(String, char[])} automatically updates the authentication information
	 * of the current <code>KeyManagerAPI</code> if the current user's password was changed. But if the password
	 * is changed by another instance, this instance is locked out due to its outdated password.
	 * @throws IOException if communication with the key-store failed. This might be a socket error between
	 * client and remote key server or it might be a problem when reading/writing data in the local file system.
	 * @see #acquire()
	 */
	void release() throws AuthenticationException, IOException;
}
