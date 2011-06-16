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
public class CipherCacheCipherEntry
{
	private CipherCacheKeyEntry keyEntry;
	private String encryptionAlgorithm;
	private Cipher cipher;
	private Date lastUse = new Date();

	public CipherCacheCipherEntry(CipherCacheKeyEntry keyEntry, String encryptionAlgorithm, Cipher cipher)
	{
		if (keyEntry == null)
			throw new IllegalArgumentException("keyEntry == null");

		if (encryptionAlgorithm == null)
			throw new IllegalArgumentException("encryptionAlgorithm == null");

		if (cipher == null)
			throw new IllegalArgumentException("cipher == null");

		this.keyEntry = keyEntry;
		this.encryptionAlgorithm = encryptionAlgorithm;
		this.cipher = cipher;
	}

	public CipherCacheCipherEntry(CipherCacheKeyEntry keyEntry, CipherCacheCipherEntry original)
	{
		this(keyEntry, original.getEncryptionAlgorithm(), original.getCipher());
	}

	public CipherCacheKeyEntry getKeyEntry() {
		return keyEntry;
	}

	public String getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	public Cipher getCipher() {
		return cipher;
	}

	public Date getLastUse() {
		return lastUse;
	}
}
