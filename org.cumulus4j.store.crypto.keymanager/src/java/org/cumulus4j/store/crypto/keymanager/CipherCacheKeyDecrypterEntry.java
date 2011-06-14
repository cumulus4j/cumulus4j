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

import org.cumulus4j.crypto.Cipher;

public class CipherCacheKeyDecrypterEntry
{
	private CipherCacheKeyEncryptionKeyEntry keyEncryptionKey;

	private String keyEncryptionTransformation;

	private Cipher keyDecryptor;

	public CipherCacheKeyDecrypterEntry(CipherCacheKeyEncryptionKeyEntry keyEncryptionKey, String keyEncryptionTransformation, Cipher keyDecryptor)
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

	public CipherCacheKeyEncryptionKeyEntry getKeyEncryptionKey() {
		return keyEncryptionKey;
	}

	public String getKeyEncryptionTransformation() {
		return keyEncryptionTransformation;
	}

	public Cipher getKeyDecryptor() {
		return keyDecryptor;
	}
}
