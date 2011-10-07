package org.cumulus4j.keymanager.api.internal.remote;

import java.io.IOException;
import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.api.AuthenticationException;
import org.cumulus4j.keymanager.api.Session;
import org.cumulus4j.keymanager.front.shared.AppServer;
import org.cumulus4j.keymanager.front.shared.OpenSessionResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class RemoteSession implements Session
{
	private RemoteKeyManagerAPI remoteKeyManagerAPI;

	private AppServer appServer;

	public RemoteSession(RemoteKeyManagerAPI remoteKeyManagerAPI, AppServer appServer)
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

	private Date lastOpenSessionResponseLocalTimestamp;
	private OpenSessionResponse lastOpenSessionResponse;

	@Override
	public synchronized String getCryptoSessionID() throws AuthenticationException, IOException
	{
		if (
				lastOpenSessionResponseLocalTimestamp != null &&
				lastOpenSessionResponseLocalTimestamp.after(new Date(System.currentTimeMillis() - 10000)) // TODO make time configurable!
		)
			return lastOpenSessionResponse.getCryptoSessionID();

		try {
			Date now = new Date();
			OpenSessionResponse response = getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "Session/" + getKeyStoreID() + '/' + appServer.getAppServerID() + "/open")
			.accept(MediaType.APPLICATION_XML_TYPE)
			.post(OpenSessionResponse.class);

			if (response == null)
				throw new IllegalStateException("Key server returned null instead of an OpenSessionResponse when opening a session!"); // TODO nice exceptions for this API!

			this.lastOpenSessionResponseLocalTimestamp = now;
			this.lastOpenSessionResponse = response;

			return response.getCryptoSessionID();
		} catch (UniformInterfaceException x) {
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsAuthenticationException(x);
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
			throw new IOException(x);
		}
	}

	private int unlockCounter;

	@Override
	public synchronized void lock() throws AuthenticationException, IOException {
		if (--unlockCounter < 0)
			throw new IllegalStateException("lock() called more often than unlock()!!!");

		if (unlockCounter == 0) // TODO don't immediately lock, but use a separate thread that locks with a delay (in case a new unlock comes in the next few seconds).
			doLock();
	}

	private void doLock() throws AuthenticationException, IOException {
		try {
			getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "Session/" + getKeyStoreID() + '/' + appServer.getAppServerID() + '/' + getCryptoSessionID() + "/lock")
			.post();
		} catch (UniformInterfaceException x) {
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsAuthenticationException(x);
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
			throw new IOException(x);
		}
	}

	@Override
	public synchronized void unlock() throws AuthenticationException, IOException
	{
		if (0 == unlockCounter++)
			doUnlock();
	}

	private void doUnlock() throws AuthenticationException, IOException {
		try {
			getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "Session/" + getKeyStoreID() + '/' + appServer.getAppServerID() + '/' + getCryptoSessionID() + "/unlock")
			.post();
		} catch (UniformInterfaceException x) {
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsAuthenticationException(x);
			RemoteKeyManagerAPI.throwUniformInterfaceExceptionAsIOException(x);
			throw new IOException(x);
		}
	}

	@Override
	public synchronized void close()
	{
		if (lastOpenSessionResponse == null)
			return; // there's nothing to be closed, yet

		String cryptoSessionID = lastOpenSessionResponse.getCryptoSessionID();
		getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "Session/" + getKeyStoreID() + '/' + appServer.getAppServerID() + '/' + cryptoSessionID)
		.delete();

		lastOpenSessionResponse = null;
		lastOpenSessionResponseLocalTimestamp = null;
	}

}
