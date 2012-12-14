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
package org.cumulus4j.keystore;

/**
 * Thrown by {@link KeyStore#deleteUser(String, char[], String)}, if
 * an attempt is made to delete the last user. Deleting the last user
 * would cause all data in the <code>KeyStore</code> to be lost,
 * hence this operation is not permitted.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CannotDeleteLastUserException extends KeyStoreException
{
	private static final long serialVersionUID = 1L;

	public CannotDeleteLastUserException() { }

	public CannotDeleteLastUserException(String message) {
		super(message);
	}

	public CannotDeleteLastUserException(Throwable cause) {
		super(cause);
	}

	public CannotDeleteLastUserException(String message, Throwable cause) {
		super(message, cause);
	}
}
