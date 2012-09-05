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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO representing a user (with a password).
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class UserWithPassword extends User
{
	private static final long serialVersionUID = 1L;

	private char[] password;

	/**
	 * Get the password.
	 * @return the password.
	 */
	public char[] getPassword() {
		return password;
	}

	/**
	 * Set the password.
	 * @param password the password.
	 */
	public void setPassword(char[] password) {
		this.password = password;
	}
}