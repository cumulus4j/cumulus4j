package org.cumulus4j.keyserver.front.webapp;

import java.net.URL;

import org.cumulus4j.keyserver.front.webapp.keyserverchannel.KeyServerChannelManager;

public class AppServer
extends org.cumulus4j.keyserver.front.shared.AppServer
{
	private static final long serialVersionUID = 1L;

	private AppServerManager appServerManager;
	private SessionManager sessionManager;
	private KeyServerChannelManager keyServerChannelManager;

	public AppServer(AppServerManager appServerManager, org.cumulus4j.keyserver.front.shared.AppServer source) {
		super(source);
		this.appServerManager = appServerManager;
		this.sessionManager = new SessionManager(appServerManager.getKeyStore());
		this.keyServerChannelManager = new KeyServerChannelManager(sessionManager, source.getAppServerBaseURL());
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

	@Override
	public void setAppServerID(String appServerID) {
		throw new UnsupportedOperationException("unmodifiable");
	}

	@Override
	public void setAppServerBaseURL(URL appServerBaseURL) {
		throw new UnsupportedOperationException("unmodifiable");
	}
}
