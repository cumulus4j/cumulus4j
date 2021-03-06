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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.cumulus4j.keystore.KeyStore;

/**
 * <p>
 * Manager for {@link AppServer}s.
 * </p>
 * <p>
 * This is the actual key-manager-entry-point, as for every {@link KeyStore}, there can be many <code>AppServer</code>s in use.
 * An instance of <code>AppServerManager</code> is bound to an instance of KeyStore (i.e. they are in a 1-1-relationship).
 * </p>
 * <p>
 * This is not API! Use the classes and interfaces provided by <code>org.cumulus4j.keymanager.api</code> instead.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
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

		if (this != appServer.getAppServerManager())
			throw new IllegalArgumentException("appServer.appServerManager != this");

		if (appServer.getAppServerBaseURL() == null)
			throw new IllegalArgumentException("appServer.appServerBaseURL == null");

		URL url;
		try {
			url = new URL(appServer.getAppServerBaseURL());
		} catch (MalformedURLException e) {
			throw new IllegalArgumentException("appServer.appServerBaseURL is not a valid URL: " + e, e);
		}

		if (appServer.getAppServerID() == null) {
			AppServer oldAppServer;
			String id;
			int index = -1;
			do {
				id = url.getHost();
				if (url.getPort() < 0) {
					if (url.getDefaultPort() >= 0)
						id += '-' + url.getDefaultPort();
				}
				else
					id += '-' + url.getPort();

				if (++index > 0)
					id += '-' + index;

				oldAppServer = this.getAppServerForAppServerID(id);
			} while (oldAppServer != null && !appServer.getAppServerBaseURL().equals(oldAppServer.getAppServerBaseURL()));

			appServer.setAppServerID(id);
		}

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
