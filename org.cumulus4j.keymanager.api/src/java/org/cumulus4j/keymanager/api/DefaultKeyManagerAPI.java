package org.cumulus4j.keymanager.api;

import org.cumulus4j.keymanager.api.internal.AbstractKeyManagerAPI;
import org.cumulus4j.keymanager.api.internal.local.LocalKeyManagerAPI;
import org.cumulus4j.keymanager.api.internal.remote.RemoteKeyManagerAPI;

public class DefaultKeyManagerAPI extends AbstractKeyManagerAPI
{
	private KeyManagerAPI delegate;

	@Override
	public void setAuthUserName(final String authUserName)
	{
		super.setAuthUserName(authUserName);

		KeyManagerAPI delegate = this.delegate;
		if (delegate != null)
			delegate.setAuthUserName(authUserName);
	}

	@Override
	public void setAuthPassword(final char[] authPassword)
	{
		super.setAuthPassword(authPassword);

		KeyManagerAPI delegate = this.delegate;
		if (delegate != null)
			delegate.setAuthPassword(authPassword);
	}

	@Override
	public void setKeyStoreID(final String keyStoreID)
	{
		super.setKeyStoreID(keyStoreID);

		KeyManagerAPI delegate = this.delegate;
		if (delegate != null)
			delegate.setKeyStoreID(keyStoreID);
	}

	@Override
	public void setKeyManagerBaseURL(final String keyManagerBaseURL)
	{
		if (equals(keyManagerBaseURL, this.getKeyManagerBaseURL()))
			return;

		super.setKeyManagerBaseURL(keyManagerBaseURL);
		this.delegate = null; // enforce re-initialisation of delegate
		initDelegate();
	}

	private KeyManagerAPI initDelegate()
	{
		KeyManagerAPI delegate = this.delegate;
		if (delegate != null)
			return delegate;

		String keyManagerBaseURL = getKeyManagerBaseURL();

		if (keyManagerBaseURL == null || keyManagerBaseURL.startsWith(FILE_URL_PREFIX))
			delegate = new LocalKeyManagerAPI();
		else
			delegate = new RemoteKeyManagerAPI();

		delegate.setAuthUserName(getAuthUserName());
		delegate.setAuthPassword(getAuthPassword());
		delegate.setKeyStoreID(getKeyStoreID());
		delegate.setKeyManagerBaseURL(getKeyManagerBaseURL());

		this.delegate = delegate;
		return delegate;
	}

	@Override
	public void initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param)
	{
		KeyManagerAPI delegate = initDelegate(); // in case it was not yet initialised (keyManagerBaseURL being null is legal)
		delegate.initDateDependentKeyStrategy(param);
	}

	@Override
	public Session getSession(String appServerBaseURL)
	{
		KeyManagerAPI delegate = initDelegate(); // in case it was not yet initialised (keyManagerBaseURL being null is legal)
		return delegate.getSession(appServerBaseURL);
	}

}
