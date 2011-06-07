package org.cumulus4j.store.crypto.keymanager;

import java.util.Date;

import org.cumulus4j.crypto.Cipher;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CipherCacheCipherEntry
{
	private CipherCacheKeyEntry keyEntry;
	private EncryptionAlgorithm encryptionAlgorithm;
	private Cipher cipher;
	private Date lastUse = new Date();

	public CipherCacheCipherEntry(CipherCacheKeyEntry keyEntry, EncryptionAlgorithm encryptionAlgorithm, Cipher cipher)
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

	public EncryptionAlgorithm getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	public Cipher getCipher() {
		return cipher;
	}

	public Date getLastUse() {
		return lastUse;
	}
}
