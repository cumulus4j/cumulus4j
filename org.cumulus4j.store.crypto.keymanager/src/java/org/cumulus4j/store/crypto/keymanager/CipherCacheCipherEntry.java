package org.cumulus4j.store.crypto.keymanager;

class CipherCacheCipherEntry
{
	private CipherCacheKeyEntry cipherCacheKeyEntry;

	public CipherCacheCipherEntry(CipherCacheKeyEntry cipherCacheKeyEntry)
	{
		if (cipherCacheKeyEntry == null)
			throw new IllegalArgumentException("cipherCacheKeyEntry == null");

		this.cipherCacheKeyEntry = cipherCacheKeyEntry;
	}


}
