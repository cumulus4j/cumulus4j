package org.cumulus4j.keymanager.api.internal.remote;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.api.AuthenticationException;
import org.cumulus4j.keymanager.api.CryptoSession;
import org.cumulus4j.keymanager.front.shared.AcquireCryptoSessionResponse;
import org.cumulus4j.keymanager.front.shared.AppServer;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterface;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class RemoteCryptoSession implements CryptoSession
{
	private RemoteKeyManagerAPI remoteKeyManagerAPI;

	private AppServer appServer;

	public RemoteCryptoSession(RemoteKeyManagerAPI remoteKeyManagerAPI, AppServer appServer)
	{
		if (remoteKeyManagerAPI == null)
			throw new IllegalArgumentException("remoteKeyManagerAPI == null");

		if (appServer == null)
			throw new IllegalArgumentException("appServer == null");

		this.remoteKeyManagerAPI = remoteKeyManagerAPI;
		this.appServer = appServer;
	}

	// BEGIN some convenience methods
	private final Client getClient() { return remoteKeyManagerAPI.getClient(); }
	private final String getKeyManagerBaseURL() { return remoteKeyManagerAPI.getKeyManagerBaseURL(); }
	private final String getKeyStoreID() { return remoteKeyManagerAPI.getKeyStoreID(); }
	private static final String appendFinalSlash(String url) { return RemoteKeyManagerAPI.appendFinalSlash(url); }
	// END some convenience methods

	@Override
	public String getAppServerID() {
		return appServer.getAppServerID();
	}

	@Override
	public String getAppServerBaseURL() {
		return appServer.getAppServerBaseURL();
	}

	private Date acquireSessionResponseLocalTimestamp;
	private AcquireCryptoSessionResponse acquireCryptoSessionResponse;

//	@Override
//	public synchronized String getCryptoSessionID() throws AuthenticationException, IOException
//	{
//		if (
//				acquireSessionResponseLocalTimestamp != null &&
//				acquireSessionResponseLocalTimestamp.after(new Date(System.currentTimeMillis() - 10000)) // TODO make time configurable!
//		)
//			return acquireCryptoSessionResponse.getCryptoSessionID();
//
//		try {
//			Date now = new Date();
//			AcquireCryptoSessionResponse response = getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "CryptoSession/" + getKeyStoreID() + '/' + appServer.getAppServerID() + "/open")
//			.accept(MediaType.APPLICATION_XML_TYPE)
//			.post(AcquireCryptoSessionResponse.class);
//
//			if (response == null)
//				throw new IllegalStateException("Key server returned null instead of an AcquireCryptoSessionResponse when opening a session!"); // TODO nice exceptions for this API!
//
//			this.lastOpenSessionResponseLocalTimestamp = now;
//			this.acquireSessionResponse = response;
//
//			return response.getCryptoSessionID();
//		} catch (UniformInterfaceException x) {
//			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsAuthenticationException(x);
//			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
//			throw new IOException(x);
//		}
//	}

	private int unlockCounter;

	@Override
	public synchronized void release() throws AuthenticationException, IOException
	{
		if (--unlockCounter < 0)
			throw new IllegalStateException("release() called more often than acquire()!!!");

		if (unlockCounter == 0) // TODO don't immediately lock, but use a separate thread that locks with a delay (in case a new unlock comes in the next few seconds).
			doRelease();
	}

	private void doRelease() throws AuthenticationException, IOException
	{
		if (acquireCryptoSessionResponse == null)
			throw new IllegalStateException("acquireCryptoSessionResponse == null");

		String cryptoSessionID = acquireCryptoSessionResponse.getCryptoSessionID();

		try {
			String url = (
					appendFinalSlash(getKeyManagerBaseURL())
					+ "CryptoSession/" + getKeyStoreID() + '/' + appServer.getAppServerID() + '/' + cryptoSessionID + "/release"
			);

			UniformInterface jui = getClient().resource(url);
			jui.post();

		} catch (UniformInterfaceException x) {
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsAuthenticationException(x);
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
			throw new IOException(x);
		}

		acquireCryptoSessionResponse = null;
		acquireSessionResponseLocalTimestamp = null;
	}

	@Override
	public synchronized String acquire() throws AuthenticationException, IOException
	{
		if (0 == unlockCounter++)
			doAcquire();
		else if (acquireSessionResponseLocalTimestamp.getTime() + 10000L < System.currentTimeMillis()) // TODO make configurable!
			doReacquire();

		return acquireCryptoSessionResponse.getCryptoSessionID();
	}

	private void doAcquire() throws AuthenticationException, IOException
	{
		try {
			Date now = new Date();
			String url = (
					appendFinalSlash(getKeyManagerBaseURL())
					+ "CryptoSession/" + getKeyStoreID() + '/' + appServer.getAppServerID() + "/acquire"
			);

			UniformInterface jui = getClient().resource(url).accept(MediaType.APPLICATION_XML_TYPE);
			AcquireCryptoSessionResponse response = jui.post(AcquireCryptoSessionResponse.class);

			if (response == null)
				throw new IllegalStateException("Key server returned null instead of an AcquireCryptoSessionResponse when opening a session!"); // TODO nice exceptions for this API!

			this.acquireSessionResponseLocalTimestamp = now;
			this.acquireCryptoSessionResponse = response;
		} catch (UniformInterfaceException x) {
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsAuthenticationException(x);
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
			throw new IOException(x);
		}
	}

	private void doReacquire() throws AuthenticationException, IOException
	{
		if (acquireCryptoSessionResponse == null)
			throw new IllegalStateException("acquireCryptoSessionResponse == null");

		String cryptoSessionID = acquireCryptoSessionResponse.getCryptoSessionID();

		try {
			Date now = new Date();
			String url = (
					appendFinalSlash(getKeyManagerBaseURL())
					+ "CryptoSession/" + getKeyStoreID() + '/' + appServer.getAppServerID() + '/' + cryptoSessionID + "/reacquire"
			);

			UniformInterface jui = getClient().resource(url).accept(MediaType.APPLICATION_XML_TYPE);
			AcquireCryptoSessionResponse response = jui.post(AcquireCryptoSessionResponse.class);

			if (response == null)
				throw new IllegalStateException("Key server returned null instead of an AcquireCryptoSessionResponse when opening a session!"); // TODO nice exceptions for this API!

			this.acquireSessionResponseLocalTimestamp = now;
			this.acquireCryptoSessionResponse = response;
		} catch (UniformInterfaceException x) {
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsAuthenticationException(x);
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
			throw new IOException(x);
		}
	}

//	@Override
//	public synchronized void close()
//	{
//		if (acquireCryptoSessionResponse == null)
//			return; // there's nothing to be closed, yet
//
//		String cryptoSessionID = acquireCryptoSessionResponse.getCryptoSessionID();
//		getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "CryptoSession/" + getKeyStoreID() + '/' + appServer.getAppServerID() + '/' + cryptoSessionID)
//		.delete();
//
//		acquireCryptoSessionResponse = null;
//		acquireSessionResponseLocalTimestamp = null;
//	}

}
