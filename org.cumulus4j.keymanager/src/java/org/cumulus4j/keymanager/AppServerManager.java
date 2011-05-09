package org.cumulus4j.keymanager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cumulus4j.keystore.KeyStore;

public class AppServerManager
{
	private KeyStore keyStore;

	private Map<String, AppServer> appServerID2appServer = new HashMap<String, AppServer>();
	private Collection<AppServer> appServers = null;

	public AppServerManager(KeyStore keyStore)
	{
		if (keyStore == null)
			throw new IllegalArgumentException("keyStore == null");

		this.keyStore = keyStore;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	public synchronized AppServer getAppServerForAppServerID(String appServerID)
	{
		if (appServerID == null)
			throw new IllegalArgumentException("appServerID == null");

		return appServerID2appServer.get(appServerID);
	}

	public synchronized void putAppServer(AppServer appServer)
	{
		if (appServer == null)
			throw new IllegalArgumentException("appServer == null");

		if (appServer.getAppServerID() == null)
			throw new IllegalArgumentException("appServer.appServerID == null");

		if (appServer.getAppServerBaseURL() == null)
			throw new IllegalArgumentException("appServer.appServerBaseURL == null");

		if (this != appServer.getAppServerManager())
			throw new IllegalArgumentException("appServer.appServerManager != this");

		appServerID2appServer.put(appServer.getAppServerID(), appServer);
		appServers = null;
	}

	public synchronized Collection<AppServer> getAppServers() {
		if (appServers == null) {
			appServers = Collections.unmodifiableCollection(new ArrayList<AppServer>(appServerID2appServer.values()));
		}
		return appServers;
	}

	public synchronized void removeAppServer(String appServerID) {
		appServerID2appServer.remove(appServerID);
	}
}
