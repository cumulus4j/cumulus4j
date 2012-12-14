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
import org.cumulus4j.keymanager.api.CannotDeleteLastUserException;
import org.cumulus4j.keymanager.api.CryptoSession;
import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitParam;
import org.cumulus4j.keymanager.api.DateDependentKeyStrategyInitResult;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.cumulus4j.keymanager.api.KeyManagerAPIInstantiationException;
import org.cumulus4j.keymanager.api.KeyStoreNotEmptyException;
import org.cumulus4j.keymanager.api.internal.AbstractKeyManagerAPI;
import org.cumulus4j.keymanager.front.shared.AcquireCryptoSessionResponse;
import org.cumulus4j.keymanager.front.shared.AppServer;
import org.cumulus4j.keymanager.front.shared.Error;
import org.cumulus4j.keymanager.front.shared.PutAppServerResponse;
import org.cumulus4j.keymanager.front.shared.UserWithPassword;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class RemoteKeyManagerAPI extends AbstractKeyManagerAPI
{
	private Map<String, AppServer> appServerBaseURL2appServer = Collections.synchronizedMap(new HashMap<String, AppServer>());

	public RemoteKeyManagerAPI()
	throws KeyManagerAPIInstantiationException
	{
		// We test here, whether the AcquireCryptoSessionResponse and some other classes are accessible. If they are not, it means the remote stuff is not deployed
		// and it should not be possible to instantiate a RemoteKeyManagerAPI.
		try {
			AcquireCryptoSessionResponse.class.getConstructors();
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

	@Override
	public void setConfiguration(KeyManagerAPIConfiguration configuration) throws IllegalArgumentException, KeyManagerAPIInstantiationException
	{
		super.setConfiguration(configuration);
		appServerBaseURL2appServer.clear();
	}

	protected static final String appendFinalSlash(String url)
	{
		if (url.endsWith("/"))
			return url;
		else
			return url + '/';
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
	public DateDependentKeyStrategyInitResult initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param) throws KeyStoreNotEmptyException, IOException
	{
		org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitParam ksInitParam = new org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitParam();
		ksInitParam.setKeyActivityPeriodMSec(param.getKeyActivityPeriodMSec());
		ksInitParam.setKeyStorePeriodMSec(param.getKeyStorePeriodMSec());

		org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitResult r;
		try {
			r = getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "DateDependentKeyStrategy/" + getKeyStoreID() + "/init")
			.type(MediaType.APPLICATION_XML_TYPE)
			.post(org.cumulus4j.keymanager.front.shared.DateDependentKeyStrategyInitResult.class, ksInitParam);
		} catch (UniformInterfaceException x) {
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsKeyStoreNotEmptyException(x);
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
			throw new IOException(x);
		}

		DateDependentKeyStrategyInitResult result = new DateDependentKeyStrategyInitResult();
		result.setGeneratedKeyCount(r.getGeneratedKeyCount());
		return result;
	}

	@Override
	public void putUser(String userName, char[] password) throws AuthenticationException, IOException
	{
		try {
			UserWithPassword userWithPassword = new UserWithPassword();
			userWithPassword.setUserName(userName);
			userWithPassword.setPassword(password.toString());

			getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "User/" + getKeyStoreID())
			.type(MediaType.APPLICATION_XML_TYPE)
			.put(userWithPassword);
		} catch (UniformInterfaceException x) {
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsAuthenticationException(x);
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
			throw new IOException(x);
//		} catch (IOException x) {
//			throw x;
		}

		// If we changed the current user's password, we automatically re-configure this API instance.
		KeyManagerAPIConfiguration conf = getConf();
		if (conf.getAuthUserName() != null && conf.getAuthUserName().equals(userName)) {
			KeyManagerAPIConfiguration newConf = new KeyManagerAPIConfiguration(conf);
			newConf.setAuthPassword(password);
			try {
				setConfiguration(newConf);
			} catch (KeyManagerAPIInstantiationException e) {
				throw new RuntimeException(e); // Shouldn't happen, because we copied the old configuration.
			}
		}
	}

	@Override
	public void deleteUser(String userName) throws AuthenticationException, CannotDeleteLastUserException, IOException
	{
		try {
			getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "User/" + getKeyStoreID() + '/' + userName)
			.type(MediaType.APPLICATION_XML_TYPE)
			.delete();
		} catch (UniformInterfaceException x) {
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsAuthenticationException(x);
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
			throw new IOException(x);
//		} catch (IOException x) {
//			throw x;
		}
	}

	@Override
	public CryptoSession getCryptoSession(String appServerBaseURL) throws IOException, AuthenticationException
	{
		try {
			AppServer appServer = appServerBaseURL2appServer.get(appServerBaseURL);
			if (appServer == null) {
				// Even if multiple threads run into this clause, the key-server will return
				// the same appServerID for all of them.
				appServer = new AppServer();
				appServer.setAppServerBaseURL(appServerBaseURL);

				PutAppServerResponse putAppServerResponse = getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "AppServer/" + getKeyStoreID())
				.accept(MediaType.APPLICATION_XML_TYPE)
				.type(MediaType.APPLICATION_XML_TYPE)
				.put(PutAppServerResponse.class, appServer);

				if (putAppServerResponse == null)
					throw new IOException("Key server returned null instead of a PutAppServerResponse when putting an AppServer instance!");

				if (putAppServerResponse.getAppServerID() == null)
					throw new IOException("Key server returned a PutAppServerResponse with property appServerID being null!");

				appServer.setAppServerID(putAppServerResponse.getAppServerID());
				appServerBaseURL2appServer.put(appServerBaseURL, appServer);
			}

			RemoteCryptoSession session = new RemoteCryptoSession(this, appServer);

//			// Try to open the session already now, so that we know already here, whether this works (but lock it immediately, again).
//			session.acquire();
//			session.release();

			return session;
		} catch (UniformInterfaceException x) {
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsAuthenticationException(x);
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
			throw new IOException(x);
		} catch (IOException x) {
			throw x;
		}
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
				throw new AuthenticationException("URL=\"" + x.getResponse().getLocation() + "\": Server replied with error code " + x.getResponse().getStatus() + " and message: " + message);
			}
		}

		throw new AuthenticationException("URL=\"" + x.getResponse().getLocation() + "\": Server replied with error code " + x.getResponse().getStatus() + "!");
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
				throw new IOException("URL=\"" + x.getResponse().getLocation() + "\": Server replied with error code " + x.getResponse().getStatus() + " and message: " + message);
			}
		}

		if (x.getResponse().getStatus() >= 400)
			throw new IOException("URL=\"" + x.getResponse().getLocation() + "\": Server replied with error code " + x.getResponse().getStatus() + "!");
	}

	private static void throwUniformInterfaceExceptionAsKeyStoreNotEmptyException(UniformInterfaceException x)
	throws KeyStoreNotEmptyException
	{
		if (x.getResponse().getStatus() < 400) // Every code < 400 means success => return without trying to read an Error.
			return;

		x.getResponse().bufferEntity();
		if (x.getResponse().hasEntity())
		{
			try {
				Error error = x.getResponse().getEntity(Error.class);
				if (org.cumulus4j.keystore.KeyStoreNotEmptyException.class.getName().equals(error.getType()))
					throw new KeyStoreNotEmptyException(error.getMessage());
			} catch (ClientHandlerException e) {
				//parsing the result failed => ignore it silently
				doNothing();
			}
		}
	}

	private static final void doNothing() { }
}
