package org.cumulus4j.keymanager.api;

import java.io.IOException;

/**
 * <p>
 * Entry point for the key management API.
 * </p><p>
 * Use <code>new DefaultKeyManagerAPI()</code> to get an instance, which you should keep (e.g. in a static shared
 * instance or some other context). Except for this one reference to {@link DefaultKeyManagerAPI} (i.e. an implementation class),
 * you should only reference the interfaces of this API project!
 * </p><p>
 * An application server using Cumulus4j is only able to read or write data, when the key manager grants access to
 * keys. In order to control this access, crypto-sessions are used (not to be confused with a servlet's session):
 * An application server can only request a key from a key manager, when the crypto-session exists and is unlocked.
 * Usually, a client will first unlock the session, then send a request to the app server and when the app server responded,
 * lock the session, again. Thus most of the time, a key manager will reject access to keys, even while a connection
 * between app server and key manager exists.
 * </p><p>
 * This entire API (all classes in <code>org.cumulus4j.keymanager.api</code>) is thread-safe. You can - and should - share
 * one <code>KeyManagerAPI</code> instance across multiple threads.
 * </p><p>
 * Note, that you must {@link #setConfiguration(KeyManagerAPIConfiguration) configure} the <code>KeyManagerAPI</code>, before
 * you can use it.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface KeyManagerAPI
{
	/**
	 * <p>
	 * Set the configuration for this {@link KeyManagerAPI} instance.
	 * </p>
	 * <p>
	 * Before a KeyManagerAPI instance can actually be used, it first needs to be configured. The configuration
	 * passed to this method will be {@link KeyManagerAPIConfiguration#markReadOnly() marked read-only}.
	 * </p>
	 * @param configuration the configuration (which will be {@link KeyManagerAPIConfiguration#markReadOnly() marked read-only}
	 * by this operation). Must not be <code>null</code>.
	 * @throws IllegalArgumentException if the configuration is <code>null</code> or incomplete (e.g. {@link KeyManagerAPIConfiguration#getKeyStoreID() configuration.keyStoreID} being <code>null</code>).
	 * @throws KeyManagerAPIInstantiationException if the actual implementation cannot be instantiated.
	 */
	void setConfiguration(KeyManagerAPIConfiguration configuration) throws IllegalArgumentException, KeyManagerAPIInstantiationException;

	/**
	 * Get the current configuration of this {@link KeyManagerAPI}. If {@link #setConfiguration(KeyManagerAPIConfiguration)} was not
	 * yet called, this is <code>null</code>.
	 * @return the {@link KeyManagerAPIConfiguration} (or <code>null</code>, if not yet configured).
	 */
	KeyManagerAPIConfiguration getConfiguration();

	/**
	 * Initialise a new key-store with the {@link org.cumulus4j.keystore.DateDependentKeyStrategy}.
	 * @param param the settings controlling the details of how to initialise it. Must not be <code>null</code>.
	 * @return
	 * @throws KeyStoreNotEmptyException
	 * @throws IOException
	 */
	DateDependentKeyStrategyInitResult initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param) throws KeyStoreNotEmptyException, IOException;

	/**
	 * Create a new user or change an existing user's password. If the password of the {@link KeyManagerAPIConfiguration#getAuthUserName() current user}
	 * is modified, this instance of KeyManagerAPI will be updated with a new configuration, automatically. Other instances of <code>KeyManagerAPI</code>
	 * - even in the same JVM - are not updated, though.
	 * @param userName the name of the new user.
	 * @param password the password of the new user.
	 * @throws AuthenticationException if the {@link KeyManagerAPIConfiguration#setAuthUserName(String) authUserName} or the {@link KeyManagerAPIConfiguration#setAuthPassword(char[]) authPassword} is incorrect.
	 * @throws IOException if the communication with the key-store (either local key-store-file or remote key-server) fails.
	 */
	void putUser(String userName, char[] password)
	throws AuthenticationException, IOException;

	/**
	 * Delete a user. If the specified user does not exist, this method is a no-op. Note, that the current user can delete himself.
	 * In this case, a 2nd call to this method would cause an <code>AuthenticationException</code>.
	 * @param userName the name of the user to be deleted.
	 * @throws AuthenticationException if the {@link KeyManagerAPIConfiguration#setAuthUserName(String) authUserName} or the {@link KeyManagerAPIConfiguration#setAuthPassword(char[]) authPassword} is incorrect.
	 * @throws CannotDeleteLastUserException if you attempted to delete the last user (which would render the key-store to be totally
	 * unreadable).
	 * @throws IOException if the communication with the key-store (either local key-store-file or remote key-server) fails.
	 */
	void deleteUser(String userName)
	throws AuthenticationException, CannotDeleteLastUserException, IOException;

	/**
	 * <p>
	 * Get a session for a certain application server.
	 * </p>
	 *
	 * @param appServerBaseURL the base-url of the app-server-key-manager-channel (must not be <code>null</code>). This is the part of the URL before the "/KeyManagerChannel" -
	 * e.g. if the REST URL of the KeyManagerChannel-service is
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keymanager.back.webapp/KeyManagerChannel", then this must be
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keymanager.back.webapp".
	 * @return the session; never <code>null</code>.
	 * @throws AuthenticationException if the {@link KeyManagerAPIConfiguration#setAuthUserName(String) authUserName} or the {@link KeyManagerAPIConfiguration#setAuthPassword(char[]) authPassword} is incorrect.
	 * @throws IOException if the communication with the key-store (either local key-store-file or remote key-server) fails.
	 */
	CryptoSession getCryptoSession(String appServerBaseURL) throws AuthenticationException, IOException;

}
