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

import com.google.appengine.api.datastore.Blob;

/**
 * Object representing encrypted information (aka <a target="_blank" href="http://en.wikipedia.org/wiki/Ciphertext">ciphertext</a>).
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Ciphertext
{
	private long keyID = -1;

	/**
	 * Get the identifier of the key used to encrypt the {@link #getData() data}.
	 *
	 * @return the key identifier.
	 */
	public long getKeyID() {
		return keyID;
	}
	/**
	 * Set the identifier of the key used to encrypt the {@link #getData() data}.
	 * @param keyID the key identifier.
	 */
	public void setKeyID(long keyID) {
		this.keyID = keyID;
	}

	private byte[] data;

	/**
	 * Get the encrypted information.
	 * @return the encrypted information or <code>null</code>.
	 */
	public byte[] getData() {
		return data;
	}
	/**
	 * Set the encrypted information.
	 * @param blob the encrypted information or <code>null</code>.
	 */
	public void setData(byte[] data) {
		this.data = data;
	}
}
