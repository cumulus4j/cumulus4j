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
 * {@link CryptoCache}-entry wrapping a {@link Cipher} used for secret-key-decryption.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoCacheKeyDecrypterEntry
{
	private CryptoCacheKeyEncryptionKeyEntry keyEncryptionKey;

	private String keyEncryptionTransformation;

	private Cipher keyDecryptor;

	private volatile Date lastUsageTimestamp = new Date();

	/**
	 * Create a new instance.
	 * @param keyEncryptionKey corresponding key-pair-entry.
	 * @param keyEncryptionTransformation the (public-private-key-pair-)transformation used to encrypt the secret keys used for symmetric encryption/decryption of the actual data.
	 * @param keyDecryptor the cipher.
	 */
	protected CryptoCacheKeyDecrypterEntry(CryptoCacheKeyEncryptionKeyEntry keyEncryptionKey, String keyEncryptionTransformation, Cipher keyDecryptor)
	{
		if (keyEncryptionKey == null)
			throw new IllegalArgumentException("keyEncryptionKey == null");

		if (keyEncryptionTransformation == null)
			throw new IllegalArgumentException("keyEncryptionTransformation == null");

		if (keyDecryptor == null)
			throw new IllegalArgumentException("keyDecryptor == null");

		this.keyEncryptionKey = keyEncryptionKey;
		this.keyEncryptionTransformation = keyEncryptionTransformation;
		this.keyDecryptor = keyDecryptor;
	}

	public CryptoCacheKeyEncryptionKeyEntry getKeyEncryptionKey() {
		return keyEncryptionKey;
	}

	/**
	 * Get the (public-private-key-pair-)transformation used to encrypt the secret keys used for symmetric en-/decryption
	 * of the actual data.
	 * @return the (public-private-key-pair-)transformation used to encrypt the secret keys used for symmetric en-/decryption
	 * of the actual data.
	 */
	public String getKeyEncryptionTransformation() {
		return keyEncryptionTransformation;
	}

	/**
	 * Get the cipher.
	 * @return the cipher.
	 */
	public Cipher getKeyDecryptor() {
		return keyDecryptor;
	}

	/**
	 * Get the timestamp when the cipher was used the last time.
	 * @return the timestamp when the cipher was used the last time.
	 */
	public Date getLastUsageTimestamp() {
		return lastUsageTimestamp;
	}

	/**
	 * Update the {@link #getLastUsageTimestamp() lastUsageTimestamp} (set it to NOW).
	 */
	public void updateLastUsageTimestamp() {
		this.lastUsageTimestamp = new Date();
	}
}
