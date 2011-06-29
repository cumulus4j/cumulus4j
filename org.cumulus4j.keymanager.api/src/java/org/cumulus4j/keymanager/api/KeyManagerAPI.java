package org.cumulus4j.keymanager.api;

import java.io.IOException;

/**
 * <p>
 * Entry point for the key management API.
 * </p>
 * <p>
 * Use <code>new DefaultKeyManagerAPI()</code> to get an instance, which you should keep (e.g. in a static shared
 * instance or some other context). Except for this one reference to {@link DefaultKeyManagerAPI},
 * you should only reference the interfaces of this API project!
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

	void initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param) throws KeyStoreNotEmptyException, IOException;

	/**
	 * Create a new user or change an existing user's password.
	 * @param userName the name of the new user.
	 * @param password the password of the new user.
	 * @throws AuthenticationException if the {@link #setAuthUserName(String) authUserName} or the {@link #setAuthPassword(char[]) authPassword} is incorrect.
	 * @throws IOException if the communication with the key-store (either local key-store-file or remote key-server) fails.
	 */
	void putUser(String userName, char[] password)
	throws AuthenticationException, IOException;

	Session getSession(String appServerBaseURL) throws AuthenticationException, IOException;

}
