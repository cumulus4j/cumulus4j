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

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.cumulus4j.crypto.CryptoRegistry;

public class CipherCacheKeyEncryptionKeyEntry
{
	private AsymmetricCipherKeyPair keyPair;

	private Date expiry;

	public CipherCacheKeyEncryptionKeyEntry(AsymmetricCipherKeyPair keyPair, long keyEncryptionKeyActivePeriodMSec)
	{
		if (keyPair == null)
			throw new IllegalArgumentException("keyPair == null");

		this.keyPair = keyPair;
		this.expiry = new Date(System.currentTimeMillis() + keyEncryptionKeyActivePeriodMSec);
	}

	public Date getExpiry() {
		return expiry;
	}

	public boolean isExpired()
	{
		return new Date().after(expiry);
	}

	public AsymmetricCipherKeyPair getKeyPair() {
		return keyPair;
	}

	private byte[] encodedPublicKey;

	public byte[] getEncodedPublicKey()
	{
		if (encodedPublicKey == null)
			encodedPublicKey = CryptoRegistry.sharedInstance().encodePublicKey(keyPair.getPublic());

		return encodedPublicKey;
	}
}
