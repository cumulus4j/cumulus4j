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

import java.security.GeneralSecurityException;

/**
 * Base class for all exceptions thrown by {@link KeyStore}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class KeyStoreException extends GeneralSecurityException
{
	private static final long serialVersionUID = 1L;

	public KeyStoreException() {
	}

	public KeyStoreException(String message) {
		super(message);
	}

	public KeyStoreException(Throwable cause) {
		super(cause);
	}

	public KeyStoreException(String message, Throwable cause) {
		super(message, cause);
	}

}
