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

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * DTO representing a list of {@link User}s.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class UserList
{
	private List<User> users = new ArrayList<User>();

	/**
	 * Get the users. This property is initialised to an empty {@link List}
	 * by the default constructor (i.e. never <code>null</code>, if not explicitly set).
	 * @return the users.
	 * @see #setUsers(List)
	 */
	public List<User> getUsers() {
		return users;
	}
	/**
	 * Set the users.
	 * @param users the users.
	 * @see #getUsers()
	 */
	public void setUsers(List<User> users) {
		this.users = users;
	}
}
