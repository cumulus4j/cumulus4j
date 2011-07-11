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
package org.cumulus4j.keymanager.front.webapp;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Authentication information (username + password).
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Auth
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String userName;

	private char[] password;

	/**
	 * Create an empty instance.
	 */
	public Auth() { }

	/**
	 * Create an instance with the given values.
	 * @param userName the user-name (might be <code>null</code>).
	 * @param password the password (might be <code>null</code>).
	 */
	public Auth(String userName, char[] password)
	{
		this.userName = userName;
		this.password = password;
	}

	/**
	 * Get the user-name.
	 * @return the user-name or <code>null</code>.
	 */
	public String getUserName() {
		return userName;
	}

	/**
	 * Set the user-name.
	 * @param userName the user-name or <code>null</code>.
	 */
	public void setUserName(String userName) {
		this.userName = userName;
	}

	/**
	 * <p>
	 * Get the password.
	 * </p>
 	 * <p>
	 * <b>Warning: the char-array returned by this method might be modified later</b> (overwritten with 0), e.g. if
	 * {@link #clear()} is called! If you want to use this char-array elsewhere, you must clone it immediately!
	 * </p>

	 * @return the password or <code>null</code>.
	 */
	public char[] getPassword() {
		return password;
	}

	/**
	 * <p>
	 * Set the password.
	 * </p>
	 * <p>
	 * <b>Warning: the char-array passed to this method is modified</b> (overwritten with 0), if
	 * {@link #clear()} is called! If you want to use this char-array elsewhere, you must pass
	 * a clone here!
	 * </p>
	 * @param password the password or <code>null</code>.
	 */
	public void setPassword(char[] password)
	{
		this.password = password;
	}

	/**
	 * Clear the sensitive data from this <code>Auth</code> instance. If the <code>password</code>
	 * is not <code>null</code>, it is overwritten with 0. This method is called by the
	 * {@link #finalize()} method of this class!
	 */
	public void clear()
	{
		if (password != null)
			Arrays.fill(password, (char)0);

		password = null;
	}

	@Override
	protected void finalize() throws Throwable {
		clear();
	}
}
