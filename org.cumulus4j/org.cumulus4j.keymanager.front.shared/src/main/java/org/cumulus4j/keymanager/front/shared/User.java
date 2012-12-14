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
 * DTO representing a user.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class User
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String userName;

	/**
	 * Create an empty <code>User</code> instance.
	 */
	public User() { }

	/**
	 * Create a <code>User</code> instance with a <code>userName</code>.
	 * @param userName the user's name (as used for log-in).
	 */
	public User(String userName) {
		this.userName = userName;
	}

	/**
	 * Get the user's name (as used for log-in).
	 * @return the user's name.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Set the user's name (as used for log-in).
	 * @param userName the user's name.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
