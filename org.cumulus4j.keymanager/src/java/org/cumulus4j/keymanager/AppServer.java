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

import org.cumulus4j.keymanager.channel.KeyManagerChannelManager;

/**
 * <p>
 * An <code>AppServer</code> represents a logical application server. This logical application server
 * might be a cluster/cloud consisting of many physical machines.
 * </p>
 * <p>
 * An <code>AppServer</code> knows how to contact the application server (or more precisely the key-manager-channel-REST-service
 * running on this application server) in order to establish a communication channel. See
 * <a target="_blank" href="http://www.cumulus4j.org/1.0.0/documentation/deployment-scenarios.html">Deployment scenarios</a>.
 * </p>
 * <p>
 * Since {@link Session}s are managed per <code>AppServer</code> (in case one single key-store is used for multiple
 * application servers), this serves as a scope for sessions (thus every <code>AppServer</code> has one instance of
 * {@link SessionManager}).
 * </p>
 * <p>
 * This is not API! Use the classes and interfaces provided by <code>org.cumulus4j.keymanager.api</code> instead.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class AppServer
{
	private static final long serialVersionUID = 1L;

	private AppServerManager appServerManager;
	private String appServerID;
	private String appServerBaseURL;
	private SessionManager sessionManager;
	private KeyManagerChannelManager keyManagerChannelManager;

	public AppServer(AppServerManager appServerManager, String appServerID, String appServerBaseURL)
	{
		if (appServerManager == null)
			throw new IllegalArgumentException("appServerManager == null");

//		if (appServerID == null) // this is now allowed! the appServerID will be assigned when putting this into the AppServerManager.
//			throw new IllegalArgumentException("appServerID == null");

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

	public void setAppServerID(String appServerID)
	{
		if (this.appServerID != null && !this.appServerID.equals(appServerID))
			throw new IllegalArgumentException("this.appServerID is already assigned and new value differs from old value! Cannot modify it afterwards!");

		this.appServerID = appServerID;
	}

	public String getAppServerBaseURL() {
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
