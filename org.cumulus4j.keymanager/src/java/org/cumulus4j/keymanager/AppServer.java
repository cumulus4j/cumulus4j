/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.keymanager;

import java.net.URL;

import org.cumulus4j.keymanager.channel.KeyManagerChannelManager;

public class AppServer
{
	private static final long serialVersionUID = 1L;

	private AppServerManager appServerManager;
	private String appServerID;
	private URL appServerBaseURL;
	private SessionManager sessionManager;
	private KeyManagerChannelManager keyManagerChannelManager;

	public AppServer(AppServerManager appServerManager, String appServerID, URL appServerBaseURL)
	{
		if (appServerManager == null)
			throw new IllegalArgumentException("appServerManager == null");

		if (appServerID == null)
			throw new IllegalArgumentException("appServerID == null");

		if (appServerBaseURL == null)
			throw new IllegalArgumentException("appServerBaseURL == null");

		this.appServerManager = appServerManager;
		this.appServerID = appServerID;
		this.appServerBaseURL = appServerBaseURL;
		this.sessionManager = new SessionManager(appServerManager.getKeyStore());
		this.keyManagerChannelManager = new KeyManagerChannelManager(sessionManager, appServerBaseURL);
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

	public KeyManagerChannelManager getKeyManagerChannelManager() {
		return keyManagerChannelManager;
	}
}
