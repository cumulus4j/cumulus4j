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
