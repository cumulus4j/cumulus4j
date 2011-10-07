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
package org.cumulus4j.store.crypto.keymanager;

import java.util.Arrays;
import java.util.Date;

/**
 * {@link CryptoCache}-entry wrapping a secret key used for symmetric en-/decryption of actual data.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoCacheKeyEntry
{
	/**
	 * Create a new instance.
	 * @param keyID identifier of the key to be cached; must be &gt;= 0.
	 * @param keyData actual key data (raw). Warning: This byte array will be overwritten with 0 by the {@link #finalize()} method!
	 */
	protected CryptoCacheKeyEntry(long keyID, byte[] keyData)
	{
		if (keyID < 0)
			throw new IllegalArgumentException("keyID < 0");

		if (keyData == null)
			throw new IllegalArgumentException("keyData == null");

		this.keyID = keyID;
		this.keyData = keyData.clone(); // necessary, because we overwrite the keyData in the finalize() method.
	}

	private long keyID = -1;

	private byte[] keyData;

	private Date lastUsageTimestamp = new Date();

	/**
	 * Get the identifier of the key being cached.
	 * @return the identifier of the key being cached.
	 */
	public long getKeyID() {
		return keyID;
	}

	/**
	 * Get the actual raw key data.
	 * @return the actual raw key data.
	 */
	public byte[] getKeyData() {
		return keyData;
	}

	/**
	 * Get the timestamp when the key was used the last time.
	 * @return the timestamp when the key was used the last time.
	 */
	public Date getLastUsageTimestamp() {
		return lastUsageTimestamp;
	}

	@Override
	protected void finalize() throws Throwable
	{
		Arrays.fill(keyData, (byte)0);
		super.finalize();
	}
}
