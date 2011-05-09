package org.cumulus4j.keymanager;

import java.net.URL;

import org.cumulus4j.keymanager.keyserverchannel.KeyServerChannelManager;

public class AppServer
{
	private static final long serialVersionUID = 1L;

	private AppServerManager appServerManager;
	private String appServerID;
	private URL appServerBaseURL;
	private SessionManager sessionManager;
	private KeyServerChannelManager keyServerChannelManager;

	public AppServer(AppServerManager appServerManager, String appServerID, URL appServerBaseURL)
	{
		if (appServerManager == null)
			throw new IllegalArgumentException("appServerManager == null");

		if (appServerID == null)
			throw new IllegalArgumentException("appServerID == null");

		if (appServerBaseURL == null)
			throw new IllegalArgumentException("appServerBaseURL == null");

		this.appServerManager = appServerManager;
		this.sessionManager = new SessionManager(appServerManager.getKeyStore());
		this.keyServerChannelManager = new KeyServerChannelManager(sessionManager, appServerBaseURL);
	}

	public String getAppServerID() {
		return appServerID;
	}

	public URL getAppServerBaseURL() {
		return appServerBaseURL;
	}

	public AppServerManager getAppServerManager() {
		return appServerManager;
	}

	public SessionManager getSessionManager() {
		return sessionManager;
	}

	public KeyServerChannelManager getKeyServerChannelManager() {
		return keyServerChannelManager;
	}
}