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
package org.cumulus4j.keymanager.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * DTO representing an app-server.
 * </p><p>
 * An <code>AppServer</code> represents a logical application server. This logical application server
 * might be a cluster/cloud consisting of many physical machines.
 * </p><p>
 * An <code>AppServer</code> contains the coordinates needed to contact the application server
 * (or more precisely the key-manager-channel-REST-service
 * running on this application server) in order to establish a communication channel. See
 * <a href="http://www.cumulus4j.org/1.0.0/documentation/deployment-scenarios.html">Deployment scenarios</a>.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class AppServer
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String appServerID;

	private String appServerBaseURL;

	/**
	 * <p>
	 * Get the app-server's ID.
	 * </p><p>
	 * If this instance of <code>AppServer</code> is used to PUT an app-server into the key-server,
	 * then this property can be <code>null</code> in order to have the
	 * key-server assign the ID. The app-server's ID is sent back to the client in a {@link PutAppServerResponse}.
	 * </p>
	 * @return the app-server's ID.
	 * @see #setAppServerID(String)
	 */
	public String getAppServerID() {
		return appServerID;
	}
	/**
	 * Set the app-server's ID.
	 * @param appServerID the app-server's ID.
	 * @see #getAppServerID()
	 */
	public void setAppServerID(String appServerID) {
		this.appServerID = appServerID;
	}
	/**
	 * Get the base-url of the app-server-key-manager-channel. This is the part of the URL before the "/KeyManagerChannel" -
	 * e.g. if the REST URL of the KeyManagerChannel-service is
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keymanager.back.webapp/KeyManagerChannel", then this must be
	 * "https://serverUsingCumulus4j.mydomain.org/org.cumulus4j.keymanager.back.webapp".
	 * @return the base-url of the app-server-key-manager-channel.
	 * @see #setAppServerBaseURL(String)
	 */
	public String getAppServerBaseURL() {
		return appServerBaseURL;
	}
	/**
	 * Set the base-url of the app-server-key-manager-channel.
	 * @param appServerBaseURL the base-url of the app-server-key-manager-channel.
	 * @see #getAppServerBaseURL()
	 */
	public void setAppServerBaseURL(String appServerBaseURL) {
		this.appServerBaseURL = appServerBaseURL;
	}
}
