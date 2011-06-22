package org.cumulus4j.keymanager.api.internal.remote;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.api.Session;
import org.cumulus4j.keymanager.api.internal.AbstractKeyManagerAPI;
import org.cumulus4j.keymanager.front.shared.OpenSessionResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class RemoteKeyManagerAPI extends AbstractKeyManagerAPI
{

	public RemoteKeyManagerAPI() {
		// We test here, whether the OpenSessionResponse and some other classes are accessible. If they are not, it means the remote stuff is not deployed
		// and it should not be possible to instantiate a RemoteKeyManagerAPI.
		OpenSessionResponse.class.getConstructors();
	}

	@Override
	public void initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param)
	{
		org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitParam ksInitParam = new org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitParam();
		ksInitParam.setKeyActivityPeriodMSec(param.getKeyActivityPeriodMSec());
		ksInitParam.setKeyStorePeriodMSec(param.getKeyStorePeriodMSec());

		Client clientForKeyServer = new Client();
		clientForKeyServer.addFilter(
				new HTTPBasicAuthFilter(getAuthUserName(), new String(getAuthPassword()))
		);

		clientForKeyServer.resource(appendFinalSlash(getKeyManagerBaseURL()) + "DateDependentKeyStrategy/init")
		.type(MediaType.APPLICATION_XML_TYPE)
		.post(ksInitParam);

	}

	private static String appendFinalSlash(String url)
	{
		if (url.endsWith("/"))
			return url;
		else
			return url + '/';
	}

	@Override
	public Session getSession(String appServerBaseURL) {
		// TODO Auto-generated method stub
		return null;
	}

}
