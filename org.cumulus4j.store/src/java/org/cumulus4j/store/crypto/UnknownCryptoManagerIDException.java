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
package org.cumulus4j.store.crypto;

/**
 * Thrown by {@link CryptoManagerRegistry#getCryptoManager(String)}, if there is no {@link CryptoManager}
 * registered for the given ID.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class UnknownCryptoManagerIDException extends IllegalArgumentException
{
	private static final long serialVersionUID = 1L;
	private String cryptoManagerID;

	/**
	 * Create a new exception instance.
	 * @param cryptoManagerID the ID for which there is no {@link CryptoManager} registered.
	 */
	public UnknownCryptoManagerIDException(String cryptoManagerID) {
		super("There is no CryptoManager registered with cryptoManagerID=\"" + cryptoManagerID + "\"!");
		this.cryptoManagerID = cryptoManagerID;
	}

	/**
	 * Get the ID for which there is no {@link CryptoManager} registered.
	 * @return the ID for which there is no {@link CryptoManager} registered. Might be <code>null</code>,
	 * if there was no <code>cryptoManagerID</code> specified at all.
	 */
	public String getCryptoManagerID() {
		return cryptoManagerID;
	}
}
