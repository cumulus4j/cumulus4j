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
