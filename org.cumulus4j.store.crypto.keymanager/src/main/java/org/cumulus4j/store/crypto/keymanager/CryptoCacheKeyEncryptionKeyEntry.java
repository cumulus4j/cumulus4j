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

/**
 * {@link CryptoCache}-entry wrapping a {@link AsymmetricCipherKeyPair key-pair} used for asymmetric en-/decryption of secret keys.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoCacheKeyEncryptionKeyEntry
{
	private AsymmetricCipherKeyPair keyPair;

	private Date expiry;

	/**
	 * Create an instance.
	 * @param keyPair the key-pair used for en-/decrypting secret keys.
	 * @param keyEncryptionKeyActivePeriodMSec the length (in milliseconds) how long the key-pair should be used.
	 */
	protected CryptoCacheKeyEncryptionKeyEntry(AsymmetricCipherKeyPair keyPair, long keyEncryptionKeyActivePeriodMSec)
	{
		if (keyPair == null)
			throw new IllegalArgumentException("keyPair == null");

		this.keyPair = keyPair;
		this.expiry = new Date(System.currentTimeMillis() + keyEncryptionKeyActivePeriodMSec);
	}

	/**
	 * Get the timestamp after which the key-pair expires. This instance of <code>CryptoCacheKeyEncryptionKeyEntry</code>
	 * should be evicted then.
	 * @return the timestamp after which the key-pair expires; never <code>null</code>.
	 */
	public Date getExpiry() {
		return expiry;
	}

	/**
	 * Determine, if this entry is expired.
	 * @return <code>true</code>, if the key-pair is expired and should not be used anymore; <code>false</code> otherwise.
	 */
	public boolean isExpired()
	{
		return new Date().after(expiry);
	}

	/**
	 * Get the key-pair.
	 * @return the key-pair; never <code>null</code>.
	 */
	public AsymmetricCipherKeyPair getKeyPair() {
		return keyPair;
	}

	private byte[] encodedPublicKey;

	/**
	 * Get the encoded (serialised) public key. This can be sent to the remote key-manager where
	 * {@link CryptoRegistry#decodePublicKey(byte[])} can be used to decode (deserialise) the byte array
	 * again.
	 * @return the encoded (serialised) public key.
	 */
	public byte[] getEncodedPublicKey()
	{
		if (encodedPublicKey == null)
			encodedPublicKey = CryptoRegistry.sharedInstance().encodePublicKey(keyPair.getPublic());

		return encodedPublicKey;
	}
}
