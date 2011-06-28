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
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class CryptoCacheKeyEntry
{
	public CryptoCacheKeyEntry(long keyID, byte[] keyData)
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

	public long getKeyID() {
		return keyID;
	}
	public byte[] getKeyData() {
		return keyData;
	}

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
