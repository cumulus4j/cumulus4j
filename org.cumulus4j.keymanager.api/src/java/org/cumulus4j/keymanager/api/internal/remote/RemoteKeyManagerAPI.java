package org.cumulus4j.keymanager.api.internal.remote;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.cumulus4j.keymanager.api.AuthenticationException;
import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.api.KeyManagerAPIInstantiationException;
import org.cumulus4j.keymanager.api.KeyStoreNotEmptyException;
import org.cumulus4j.keymanager.api.Session;
import org.cumulus4j.keymanager.api.internal.AbstractKeyManagerAPI;
import org.cumulus4j.keymanager.front.shared.AppServer;
import org.cumulus4j.keymanager.front.shared.Error;
import org.cumulus4j.keymanager.front.shared.OpenSessionResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

public class RemoteKeyManagerAPI extends AbstractKeyManagerAPI
{
	private Map<String, AppServer> appServerBaseURL2appServer = Collections.synchronizedMap(new HashMap<String, AppServer>());

	public RemoteKeyManagerAPI()
	throws KeyManagerAPIInstantiationException
	{
		// We test here, whether the OpenSessionResponse and some other classes are accessible. If they are not, it means the remote stuff is not deployed
		// and it should not be possible to instantiate a RemoteKeyManagerAPI.
		try {
			OpenSessionResponse.class.getConstructors();
		} catch (NoClassDefFoundError x) {
			throw new KeyManagerAPIInstantiationException("The RemoteKeyManagerAPI could not be instantiated! If you really want to use a key-server, make sure all required libs are deployed. If you want to use a local key-store instead of a key-server, you must specify different arguments. It seems, the module 'org.cumulus4j.keymanager.front.shared' is missing! " + x, x);
		}

		try {
			com.sun.jersey.core.header.AcceptableMediaType.class.getConstructors();
		} catch (NoClassDefFoundError x) {
			throw new KeyManagerAPIInstantiationException("The RemoteKeyManagerAPI could not be instantiated! If you really want to use a key-server, make sure all required libs are deployed. If you want to use a local key-store instead of a key-server, you must specify different arguments. It seems, the module 'jersey-core' is missing! " + x, x);
		}

		try {
			Client.class.getConstructors();
		} catch (NoClassDefFoundError x) {
			throw new KeyManagerAPIInstantiationException("The RemoteKeyManagerAPI could not be instantiated! If you really want to use a key-server, make sure all required libs are deployed. If you want to use a local key-store instead of a key-server, you must specify different arguments. It seems, the module 'jersey-client' is missing! " + x, x);
		}
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
	public void initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param) throws KeyStoreNotEmptyException, IOException
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
	public Session getSession(String appServerBaseURL) throws IOException, AuthenticationException
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

	protected static void throwUniformInterfaceExceptionAsAuthenticationException(UniformInterfaceException x)
	throws AuthenticationException
	{
		if (x.getResponse().getStatus() != Status.FORBIDDEN.getStatusCode())
			return;

		x.getResponse().bufferEntity();
		if (x.getResponse().hasEntity())
		{
			try {
				Error error = x.getResponse().getEntity(Error.class);
				if (
						AuthenticationException.class.getName().equals(error.getType()) ||
						org.cumulus4j.keystore.AuthenticationException.class.getName().equals(error.getType())
				)
					throw new AuthenticationException(error.getMessage());
			} catch (ClientHandlerException e) {
				//parsing the result failed => returning it as a String
				String message = getClientResponseEntityAsString(x.getResponse());
				throw new AuthenticationException("Server replied with error code " + x.getResponse().getStatus() + " and message: " + message);
			}
		}

		throw new AuthenticationException("Server replied with error code " + x.getResponse().getStatus() + "!");
	}

	private static String getClientResponseEntityAsString(ClientResponse response)
	{
		Reader reader = new InputStreamReader(response.getEntityInputStream(), Charset.forName("UTF-8"));
		StringBuilder sb = new StringBuilder();
		char[] cb = new char[1024];
		int bytesRead;
		try {
			while (0 <= (bytesRead = reader.read(cb))) {
				sb.append(cb, 0, bytesRead);
			}
		} catch (IOException x) { // this comes from the Reader API and should be safe to ignore, because we buffer the entity and should thus not encounter any socket-read-error here.
			throw new RuntimeException(x);
		}
		return sb.toString();
	}

	protected static void throwUniformInterfaceExceptionAsIOException(UniformInterfaceException x)
	throws IOException
	{
		x.getResponse().bufferEntity();
		if (x.getResponse().hasEntity()) {
			try {
				Error error = x.getResponse().getEntity(Error.class);
				throw new IOException(error.getMessage());
			} catch (ClientHandlerException e) {
				//parsing the result failed => returning it as a String
				String message = getClientResponseEntityAsString(x.getResponse());
				throw new IOException("Server replied with error code " + x.getResponse().getStatus() + " and message: " + message);
			}
		}

		if (x.getResponse().getStatus() >= 400)
			throw new IOException("Server replied with error code " + x.getResponse().getStatus() + "!");
	}

}
