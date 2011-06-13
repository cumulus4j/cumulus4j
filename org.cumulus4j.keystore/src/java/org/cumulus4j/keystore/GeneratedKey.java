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
 * Container holding a {@link #getKeyID() keyID} and a {@link #getKey() key}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class GeneratedKey
{
	private long keyID;
	private byte[] key;

	/**
	 * Constructor for instantiating a <code>GeneratedKey</code> with a <code>keyID</code> and the actual <code>key</code>.
	 *
	 * @param keyID the identifier of the key.
	 * @param key the actual key.
	 */
	public GeneratedKey(long keyID, byte[] key) {
		this.keyID = keyID;
		this.key = key;
	}

	/**
	 * Get the key-identifier.
	 *
	 * @return the key-identifier.
	 */
	public long getKeyID() {
		return keyID;
	}

	/**
	 * Get the actual key.
	 *
	 * @return the key.
	 */
	public byte[] getKey() {
		return key;
	}
}
