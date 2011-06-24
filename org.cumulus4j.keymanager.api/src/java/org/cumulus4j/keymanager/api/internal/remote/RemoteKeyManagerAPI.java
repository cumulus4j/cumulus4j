package org.cumulus4j.keymanager.api.internal.remote;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.api.Session;
import org.cumulus4j.keymanager.api.internal.AbstractKeyManagerAPI;
import org.cumulus4j.keymanager.front.shared.AppServer;
import org.cumulus4j.keymanager.front.shared.OpenSessionResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class RemoteKeyManagerAPI extends AbstractKeyManagerAPI
{
	private Map<String, AppServer> appServerBaseURL2appServer = Collections.synchronizedMap(new HashMap<String, AppServer>());

	public RemoteKeyManagerAPI() {
		// We test here, whether the OpenSessionResponse and some other classes are accessible. If they are not, it means the remote stuff is not deployed
		// and it should not be possible to instantiate a RemoteKeyManagerAPI.
		OpenSessionResponse.class.getConstructors();
	}

	private Client client;

	protected synchronized Client getClient()
	{
		// A client is thread-safe except for configuration changes (but we don't change the configuration of the returned client anymore).
		if (client == null) {
			Client client = new Client();
			client.addFilter(
					new HTTPBasicAuthFilter(getAuthUserName(), new String(getAuthPassword()))
			);
			this.client = client;
		}
		return client;
	}

	@Override
	public void initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param)
	{
		org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitParam ksInitParam = new org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitParam();
		ksInitParam.setKeyActivityPeriodMSec(param.getKeyActivityPeriodMSec());
		ksInitParam.setKeyStorePeriodMSec(param.getKeyStorePeriodMSec());

		getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "DateDependentKeyStrategy/" + getKeyStoreID() + "/init")
		.type(MediaType.APPLICATION_XML_TYPE)
		.post(ksInitParam);

		// TODO try-catch-block and introduce nice exceptions into this API - and possibly test them!!!
	}

	@Override
	public void setKeyManagerBaseURL(String keyManagerBaseURL) {
		super.setKeyManagerBaseURL(keyManagerBaseURL);
		appServerBaseURL2appServer.clear();
	}

	@Override
	public void setKeyStoreID(String keyStoreID) {
		super.setKeyStoreID(keyStoreID);
		appServerBaseURL2appServer.clear();
	}

	protected static final String appendFinalSlash(String url)
	{
		if (url.endsWith("/"))
			return url;
		else
			return url + '/';
	}

	@Override
	public Session getSession(String appServerBaseURL)
	{
		AppServer appServer = appServerBaseURL2appServer.get(appServerBaseURL);
		if (appServer == null) {
			// Even if multiple threads run into this clause, the key-server will return
			// the same appServerID for all of them.
			appServer = new AppServer();
			appServer.setAppServerBaseURL(appServerBaseURL);

			String appServerID = getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "AppServer/" + getKeyStoreID())
			.accept(MediaType.TEXT_PLAIN_TYPE)
			.type(MediaType.APPLICATION_XML_TYPE)
			.put(String.class, appServer);

			if (appServerID == null)
				throw new IllegalStateException("Key server returned null instead of an appServerID when putting an AppServer instance!"); // TODO nice exceptions for this API!

			appServer.setAppServerID(appServerID);

			appServerBaseURL2appServer.put(appServerBaseURL, appServer);
		}

		RemoteSession session = new RemoteSession(this, appServer);

		// Try to open the session already now, so that we know already here, whether this works
		// (this getter will internally trigger the REST request).
		session.getCryptoSessionID();

		return session;
		// TODO try-catch-block and introduce nice exceptions into this API
	}

}
