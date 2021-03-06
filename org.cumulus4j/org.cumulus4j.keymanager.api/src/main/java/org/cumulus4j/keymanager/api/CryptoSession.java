package org.cumulus4j.keymanager.api;

import java.io.IOException;

/**
 * <p>
 * A <code>CryptoSession</code> is a session in which key transfers can be performed.
 * </p><p>
 * Use {@link KeyManagerAPI#getCryptoSession(String)} to get a <code>CryptoSession</code> instance.
 * This instance is a proxy which can be kept and never expires (though the underlying real session will expire if
 * not used for some time). If the underlying real
 * session expired, a new underlying session with a new <code>cryptoSessionID</code>
 * will be created and bound to this <code>CryptoSession</code> instance.
 * </p><p>
 * <code>CryptoSession</code>s are thread-safe.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface CryptoSession
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
	 * If multiple threads use the same <code>CryptoSession</code> (recommended!), the underlying real session will be
	 * acquired (unlocked) when the first thread requires it and it will be locked again when the last thread calls
	 * <code>release()</code>.
	 * However, releasing (locking) does not need to happen immediately. Instead it can be deferred a few seconds, in
	 * case a new <code>acquire()</code> would happen quickly again. This
	 * strategy is usually used with a remote key server (when latency makes acquiring/releasing a pretty expensive
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
	 * Release the session, after it was previously {@link #acquire() acquired}.
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
