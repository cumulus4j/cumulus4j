package org.cumulus4j.keymanager.api;

import java.io.IOException;

import org.cumulus4j.keymanager.api.internal.AbstractKeyManagerAPI;
import org.cumulus4j.keymanager.api.internal.local.LocalKeyManagerAPI;
import org.cumulus4j.keymanager.api.internal.remote.RemoteKeyManagerAPI;

public class DefaultKeyManagerAPI extends AbstractKeyManagerAPI
{
	private KeyManagerAPI delegate;

	@Override
	public void setAuthUserName(final String authUserName)
	{
		assertNotInitialised();
		super.setAuthUserName(authUserName);

		KeyManagerAPI delegate = this.delegate;
		if (delegate != null)
			delegate.setAuthUserName(authUserName);
	}

	@Override
	public void setAuthPassword(final char[] authPassword)
	{
		assertNotInitialised();
		super.setAuthPassword(authPassword);

		KeyManagerAPI delegate = this.delegate;
		if (delegate != null)
			delegate.setAuthPassword(authPassword);
	}

	@Override
	public void setKeyStoreID(final String keyStoreID)
	{
		assertNotInitialised();
		super.setKeyStoreID(keyStoreID);

		KeyManagerAPI delegate = this.delegate;
		if (delegate != null)
			delegate.setKeyStoreID(keyStoreID);
	}

	@Override
	public void setKeyManagerBaseURL(final String keyManagerBaseURL)
	{
		assertNotInitialised();

		if (equals(keyManagerBaseURL, this.getKeyManagerBaseURL()))
			return;

		super.setKeyManagerBaseURL(keyManagerBaseURL);
//		this.delegate = null; // enforce re-initialisation of delegate // not necessary with new init policy, anymore
	}

	@Override
	public void init() throws KeyManagerAPIInstantiationException
	{
		if (initialised)
			return;

		initDelegate();

		super.init(); // we call this after initDelegate(), because we want the initialised state to stay false, if the delegate initialisation failed.
	}

	private KeyManagerAPI getDelegate()
	{
		assertInitialised();
		return this.delegate;
	}

	private KeyManagerAPI initDelegate() throws KeyManagerAPIInstantiationException
	{
		KeyManagerAPI delegate = this.delegate;
		if (delegate != null)
			return delegate;

		String keyManagerBaseURL = getKeyManagerBaseURL();

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

		delegate.setAuthUserName(getAuthUserName());
		delegate.setAuthPassword(getAuthPassword());
		delegate.setKeyStoreID(getKeyStoreID());
		delegate.setKeyManagerBaseURL(getKeyManagerBaseURL());

		this.delegate = delegate;
		return delegate;
	}

	@Override
	public void initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param) throws KeyStoreNotEmptyException, IOException
	{
		KeyManagerAPI delegate = getDelegate();
		delegate.initDateDependentKeyStrategy(param);
	}

	@Override
	public Session getSession(String appServerBaseURL) throws IOException, AuthenticationException
	{
		KeyManagerAPI delegate = getDelegate();
		return delegate.getSession(appServerBaseURL);
	}

}
