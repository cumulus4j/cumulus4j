package org.cumulus4j.store.crypto.keymanager;

import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.cumulus4j.crypto.Cipher;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class CipherCache
{
	// TODO continue implementing this!

	private Map<Long, CipherCacheKeyEntry> keyID2key = new HashMap<Long, CipherCacheKeyEntry>();

	private Map<Long, Deque<CipherCacheCipherEntry>> keyID2ciphers = new HashMap<Long, Deque<CipherCacheCipherEntry>>();

	public synchronized Cipher getEncrypter(EncryptionAlgorithm encryptionAlgorithm)
	{
		throw new UnsupportedOperationException("NYI");
	}

}
