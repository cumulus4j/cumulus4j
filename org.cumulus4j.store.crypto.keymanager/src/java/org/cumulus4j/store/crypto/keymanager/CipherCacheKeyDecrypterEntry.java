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
