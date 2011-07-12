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
import org.cumulus4j.crypto.CryptoRegistry;

/**
 * {@link CryptoCache}-entry wrapping a {@link Cipher} used for symmetric data-en-/decryption.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoCacheCipherEntry
{
	private CryptoCacheKeyEntry keyEntry;
	private String cipherTransformation;
	private Cipher cipher;

	private Date lastUsageTimestamp = new Date();

	/**
	 * Create a new instance.
	 * @param keyEntry the corresponding key-entry. Must not be <code>null</code>.
	 * @param cipherTransformation the transformation (as passed to {@link CryptoRegistry#createCipher(String)}). Must not be <code>null</code>.
	 * @param cipher the cipher. Must not be <code>null</code>.
	 */
	protected CryptoCacheCipherEntry(CryptoCacheKeyEntry keyEntry, String cipherTransformation, Cipher cipher)
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

	/**
	 * Create a new instance copying an old one. Used to refresh an existing entry (as it assigns a new
	 * {@link #getLastUsageTimestamp() lastUsageTimestamp}) while keeping this class immutable.
	 * @param keyEntry the key-entry.
	 * @param original the original from which to copy.
	 */
	protected CryptoCacheCipherEntry(CryptoCacheKeyEntry keyEntry, CryptoCacheCipherEntry original)
	{
		this(keyEntry, original.getCipherTransformation(), original.getCipher());
	}

	/**
	 * Get the corresponding key-entry.
	 * @return the corresponding key-entry; never <code>null</code>.
	 */
	public CryptoCacheKeyEntry getKeyEntry() {
		return keyEntry;
	}

	/**
	 * Get the transformation (as passed to {@link CryptoRegistry#createCipher(String)}).
	 * @return the transformation (as passed to {@link CryptoRegistry#createCipher(String)}); never <code>null</code>.
	 */
	public String getCipherTransformation() {
		return cipherTransformation;
	}

	/**
	 * Get the cipher.
	 * @return the cipher; never <code>null</code>.
	 */
	public Cipher getCipher() {
		return cipher;
	}

	/**
	 * Get the timestamp when the cipher was used the last time.
	 * @return the timestamp when the cipher was used the last time.
	 */
	public Date getLastUsageTimestamp() {
		return lastUsageTimestamp;
	}
}
