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

import java.util.Date;

import org.cumulus4j.crypto.Cipher;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoCacheCipherEntry
{
	private CryptoCacheKeyEntry keyEntry;
	private String cipherTransformation;
	private Cipher cipher;

	private Date lastUsageTimestamp = new Date();

	public CryptoCacheCipherEntry(CryptoCacheKeyEntry keyEntry, String cipherTransformation, Cipher cipher)
	{
		if (keyEntry == null)
			throw new IllegalArgumentException("keyEntry == null");

		if (cipherTransformation == null)
			throw new IllegalArgumentException("cipherTransformation == null");

		if (cipher == null)
			throw new IllegalArgumentException("cipher == null");

		this.keyEntry = keyEntry;
		this.cipherTransformation = cipherTransformation;
		this.cipher = cipher;
	}

	public CryptoCacheCipherEntry(CryptoCacheKeyEntry keyEntry, CryptoCacheCipherEntry original)
	{
		this(keyEntry, original.getCipherTransformation(), original.getCipher());
	}

	public CryptoCacheKeyEntry getKeyEntry() {
		return keyEntry;
	}

	public String getCipherTransformation() {
		return cipherTransformation;
	}

	public Cipher getCipher() {
		return cipher;
	}

	public Date getLastUsageTimestamp() {
		return lastUsageTimestamp;
	}
}
