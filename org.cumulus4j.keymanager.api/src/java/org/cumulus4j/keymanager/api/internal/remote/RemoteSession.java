package org.cumulus4j.keymanager.api.internal.remote;

import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.cumulus4j.keymanager.api.Session;
import org.cumulus4j.keymanager.front.shared.AppServer;
import org.cumulus4j.keymanager.front.shared.OpenSessionResponse;

import com.sun.jersey.api.client.Client;

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
	public synchronized String getCryptoSessionID()
	{
		// TODO don't do this always, but take lastOpenSessionResponse.getExpiry() [with a safety margin] into account (AND TIME DIFFERENCE BETWEEN LOCAL AND SERVER TIME!!!)

		Date now = new Date();
		OpenSessionResponse response = getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "Session/" + getKeyStoreID() + '/' + appServer.getAppServerID() + "/open")
		.accept(MediaType.APPLICATION_XML_TYPE)
		.post(OpenSessionResponse.class);

		if (response == null)
			throw new IllegalStateException("Key server returned null instead of an OpenSessionResponse when opening a session!"); // TODO nice exceptions for this API!

		this.lastOpenSessionResponseLocalTimestamp = now;
		this.lastOpenSessionResponse = response;

		return response.getCryptoSessionID();
	}

	private int unlockCounter;

	@Override
	public synchronized void lock() {
		if (--unlockCounter < 0)
			throw new IllegalStateException("lock() called more often than unlock()!!!");

		if (unlockCounter == 0) // TODO don't immediately lock, but use a separate thread that locks with a delay (in case a new unlock comes in the next few seconds).
			doLock();
	}

	private void doLock() {
		getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "Session/" + getKeyStoreID() + '/' + appServer.getAppServerID() + '/' + getCryptoSessionID() + "/lock")
		.post();
	}

	@Override
	public synchronized void unlock()
	{
		if (0 == unlockCounter++)
			doUnlock();
	}

	private void doUnlock() {
		getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "Session/" + getKeyStoreID() + '/' + appServer.getAppServerID() + '/' + getCryptoSessionID() + "/unlock")
		.post();
	}

	@Override
	public void close() {
		// TODO optimize this (it might open a brand new session just to close it).
		getClient().resource(appendFinalSlash(getKeyManagerBaseURL()) + "Session/" + getKeyStoreID() + '/' + appServer.getAppServerID() + '/' + getCryptoSessionID())
		.delete();
	}

}
