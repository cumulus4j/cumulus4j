package org.cumulus4j.keymanager.api;

import java.io.IOException;

import org.cumulus4j.keymanager.api.internal.AbstractKeyManagerAPI;
import org.cumulus4j.keymanager.api.internal.local.LocalKeyManagerAPI;
import org.cumulus4j.keymanager.api.internal.remote.RemoteKeyManagerAPI;

/**
 * <p>
 * Implementation of {@link KeyManagerAPI} that delegates to a specific implementation.
 * </p><p>
 * Instantiate an instance of this class and then use the <code>KeyManagerAPI</code> interface
 * everywhere to reference it.
 * </p><p>
 * When using dependency injection, there should be no reference
 * to this class at all except for the configuration of the dependency injection.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class DefaultKeyManagerAPI extends AbstractKeyManagerAPI
{
	private volatile KeyManagerAPI delegate;

	@Override
	public void setConfiguration(KeyManagerAPIConfiguration configuration) throws IllegalArgumentException, KeyManagerAPIInstantiationException
	{
		// We do *not* call super.setConfiguration(...), because the delegates might replace the configuration and
		// thus this instance should not have a copy of it at all! Instead we override getConfiguration() as well.

		if (configuration == null)
			throw new IllegalArgumentException("configuration == null");

		configuration.markReadOnly();

		// In case, we already had a delegate before, we null it now (so we don't end up with some half-initialised stuff.
		this.delegate = null;

		String keyManagerBaseURL = configuration.getKeyManagerBaseURL();

		KeyManagerAPI delegate;
		if (keyManagerBaseURL == null || keyManagerBaseURL.startsWith(FILE_URL_PREFIX)) {
			try {
				delegate = new LocalKeyManagerAPI();
			} catch (KeyManagerAPIInstantiationException x) {
				throw x;
			} catch (Throwable t) {
				throw new KeyManagerAPIInstantiationException("The LocalKeyManagerAPI could not be instantiated! If you really want to use a local key-store, make sure all required libs are deployed. If you want to use a key-server instead of a local key-store, you must specify different arguments. " + t, t);
			}
		}
		else {
			try {
				delegate = new RemoteKeyManagerAPI();
			} catch (KeyManagerAPIInstantiationException x) {
				throw x;
			} catch (Throwable t) {
				throw new KeyManagerAPIInstantiationException("The RemoteKeyManagerAPI could not be instantiated! If you really want to use a key-server, make sure all required libs are deployed. If you want to use a local key-store instead of a key-server, you must specify different arguments. " + t, t);
			}
		}

		delegate.setConfiguration(configuration);
		this.delegate = delegate;
	}

	@Override
	public KeyManagerAPIConfiguration getConfiguration()
	{
		KeyManagerAPI delegate = this.delegate;
		if (delegate == null)
			return null;
		else
			return delegate.getConfiguration();
	}

	private KeyManagerAPI getDelegate()
	{
		KeyManagerAPI delegate = this.delegate;
		if (delegate == null)
			throw new IllegalStateException("setConfiguration(...) was not yet called!");

		return delegate;
	}

	@Override
	public DateDependentKeyStrategyInitResult initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param) throws KeyStoreNotEmptyException, IOException
	{
		return getDelegate().initDateDependentKeyStrategy(param);
	}

	@Override
	public void putUser(String userName, char[] password) throws AuthenticationException, IOException
	{
		getDelegate().putUser(userName, password);
	}

	@Override
	public void deleteUser(String userName) throws AuthenticationException, CannotDeleteLastUserException, IOException
	{
		getDelegate().deleteUser(userName);
	}

	@Override
	public CryptoSession getCryptoSession(String appServerBaseURL) throws AuthenticationException, IOException
	{
		return getDelegate().getCryptoSession(appServerBaseURL);
	}

}
