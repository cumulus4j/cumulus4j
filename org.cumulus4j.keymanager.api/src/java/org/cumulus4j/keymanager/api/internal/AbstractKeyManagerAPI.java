package org.cumulus4j.keymanager.api.internal;

import org.cumulus4j.keymanager.api.KeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.cumulus4j.keymanager.api.KeyManagerAPIInstantiationException;

public abstract class AbstractKeyManagerAPI
implements KeyManagerAPI
{
	protected static final String FILE_URL_PREFIX = "file:";

	private volatile KeyManagerAPIConfiguration configuration;

	/**
	 * Get the configuration. If there is no configuration, yet, an
	 * {@link IllegalStateException} is thrown.
	 * @return the configuration, never <code>null</code>.
	 * @throws IllegalStateException if there is no configuration, yet, i.e. setConfiguration(...) was not yet called.
	 */
	public KeyManagerAPIConfiguration getConf()
	throws IllegalStateException
	{
		KeyManagerAPIConfiguration configuration = getConfiguration();
		if (configuration == null)
			throw new IllegalStateException("There is no configuration, yet! Call setConfiguration(...) first!");

		return configuration;
	}

	/**
	 * Convenience method delegating to {@link KeyManagerAPIConfiguration#getAuthUserName()}.
	 * @return the authUserName.
	 * @throws IllegalStateException if there is no configuration, yet, i.e. setConfiguration(...) was not yet called.
	 */
	public String getAuthUserName() throws IllegalStateException {
		return getConf().getAuthUserName();
	}

	/**
	 * Convenience method delegating to {@link KeyManagerAPIConfiguration#getAuthPassword()}.
	 * @return the authPassword.
	 * @throws IllegalStateException if there is no configuration, yet, i.e. setConfiguration(...) was not yet called.
	 */
	public char[] getAuthPassword() throws IllegalStateException {
		return getConf().getAuthPassword();
	}

	/**
	 * Convenience method delegating to {@link KeyManagerAPIConfiguration#getKeyStoreID()}.
	 * @return the keyStoreID.
	 * @throws IllegalStateException if there is no configuration, yet, i.e. setConfiguration(...) was not yet called.
	 */
	public String getKeyStoreID() throws IllegalStateException {
		return getConf().getKeyStoreID();
	}

	/**
	 * Convenience method delegating to {@link KeyManagerAPIConfiguration#getKeyManagerBaseURL()}.
	 * @return the keyManagerBaseURL.
	 * @throws IllegalStateException if there is no configuration, yet, i.e. setConfiguration(...) was not yet called.
	 */
	public String getKeyManagerBaseURL() throws IllegalStateException {
		return getConf().getKeyManagerBaseURL();
	}

	@Override
	public void setConfiguration(KeyManagerAPIConfiguration configuration) throws IllegalArgumentException, KeyManagerAPIInstantiationException
	{
		if (configuration == null)
			throw new IllegalArgumentException("configuration == null");

		// Mark it read-only to prevent any configuration change besides calling this method again.
		configuration.markReadOnly();

		// The authUserName and authPassword is not necessarily required for all operations.
//		if (configuration.getAuthUserName() == null)
//			throw new IllegalArgumentException("configuration.authUserName == null");
//
//		if (configuration.getAuthPassword() == null)
//			throw new IllegalArgumentException("configuration.authPassword == null");

		if (configuration.getKeyStoreID() == null)
			throw new IllegalArgumentException("configuration.keyStoreID == null");

		this.configuration = configuration;
	}

	@Override
	public KeyManagerAPIConfiguration getConfiguration() {
		return configuration;
	}

	protected static boolean equals(Object o1, Object o2)
	{
		return o1 == o2 || (o1 != null && o1.equals(o2));
	}
}
