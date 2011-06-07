package org.cumulus4j.store.crypto.keymanager;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.CipherOperationMode;
import org.cumulus4j.crypto.CipherRegistry;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CipherCache
{
	private SecureRandom random = new SecureRandom();
	private long activeEncryptionKeyID = -1;
	private Date activeEncryptionKeyUntilExcl = null;
	private Object activeEncryptionKeyMutex = new Object();

	private Map<Long, CipherCacheKeyEntry> keyID2key = Collections.synchronizedMap(new HashMap<Long, CipherCacheKeyEntry>());

	private Map<CipherOperationMode, Map<EncryptionAlgorithm, Map<Long, List<CipherCacheCipherEntry>>>> opmode2encryptionAlgorithm2keyID2cipherEntries = Collections.synchronizedMap(
		new HashMap<CipherOperationMode, Map<EncryptionAlgorithm,Map<Long,List<CipherCacheCipherEntry>>>>()
	);

	public long getActiveEncryptionKeyID()
	{
		long activeEncryptionKeyID;
		Date activeEncryptionKeyUntilExcl;
		synchronized (activeEncryptionKeyMutex) {
			activeEncryptionKeyID = this.activeEncryptionKeyID;
			activeEncryptionKeyUntilExcl = this.activeEncryptionKeyUntilExcl;
		}

		if (activeEncryptionKeyUntilExcl == null)
			return -1;

		if (activeEncryptionKeyUntilExcl.compareTo(new Date()) <= 0)
			return -1;

		return activeEncryptionKeyID;
	}

	public void setActiveEncryptionKeyID(long activeEncryptionKeyID, Date activeUntilExcl)
	{
		if (activeEncryptionKeyID <= 0)
			throw new IllegalArgumentException("activeEncryptionKeyID <= 0");

		if (activeUntilExcl == null)
			throw new IllegalArgumentException("activeUntilExcl == null");

		synchronized (activeEncryptionKeyMutex) {
			this.activeEncryptionKeyID = activeEncryptionKeyID;
			this.activeEncryptionKeyUntilExcl = activeUntilExcl;
		}
	}

	protected byte[] getKeyData(long keyID)
	{
		CipherCacheKeyEntry entry = keyID2key.get(keyID);
		if (entry == null)
			return null;
		else
			return entry.getKeyData();
	}

	protected CipherCacheKeyEntry setKeyData(long keyID, byte[] keyData)
	{
		CipherCacheKeyEntry entry = new CipherCacheKeyEntry(keyID, keyData);
		keyID2key.put(keyID, entry);
		return entry;
	}

	public CipherCacheCipherEntry acquireDecrypter(EncryptionAlgorithm encryptionAlgorithm, long keyID, byte[] iv)
	{
		return acquireDecrypter(encryptionAlgorithm, keyID, null, iv);
	}

	public CipherCacheCipherEntry acquireDecrypter(EncryptionAlgorithm encryptionAlgorithm, long keyID, byte[] keyData, byte[] iv)
	{
		return acquireCipherEntry(CipherOperationMode.DECRYPT, encryptionAlgorithm, keyID, keyData, iv);
	}

	public CipherCacheCipherEntry acquireEncrypter(EncryptionAlgorithm encryptionAlgorithm, long keyID)
	{
		return acquireEncrypter(encryptionAlgorithm, keyID, null);
	}

	public CipherCacheCipherEntry acquireEncrypter(EncryptionAlgorithm encryptionAlgorithm, long keyID, byte[] keyData)
	{
		return acquireCipherEntry(CipherOperationMode.ENCRYPT, encryptionAlgorithm, keyID, keyData, null);
	}

	private CipherCacheCipherEntry acquireCipherEntry(
			CipherOperationMode opmode, EncryptionAlgorithm encryptionAlgorithm, long keyID, byte[] keyData, byte[] iv
	)
	{
		Map<EncryptionAlgorithm, Map<Long, List<CipherCacheCipherEntry>>> encryptionAlgorithm2keyID2encrypters =
			opmode2encryptionAlgorithm2keyID2cipherEntries.get(opmode);

		if (encryptionAlgorithm2keyID2encrypters != null) {
			Map<Long, List<CipherCacheCipherEntry>> keyID2Encrypters = encryptionAlgorithm2keyID2encrypters.get(encryptionAlgorithm);
			if (keyID2Encrypters != null) {
				List<CipherCacheCipherEntry> encrypters = keyID2Encrypters.get(keyID);
				if (encrypters != null) {
					CipherCacheCipherEntry entry = encrypters.remove(0);
					if (entry != null) {
						entry = new CipherCacheCipherEntry(
								setKeyData(keyID, entry.getKeyEntry().getKeyData()), entry
						);
						if (iv == null) {
							iv = new byte[entry.getCipher().getIVSize()];
							random.nextBytes(iv);
						}
						entry.getCipher().init(
								opmode,
								new ParametersWithIV(null, iv) // no key, because we reuse the cipher and want to suppress expensive rekeying
						);
						return entry;
					}
				}
			}
		}

		if (keyData == null) {
			keyData = getKeyData(keyID);
			if (keyData == null)
				return null;
		}

		Cipher cipher;
		try {
			cipher = CipherRegistry.sharedInstance().createCipher(encryptionAlgorithm.getTransformation());
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}

		CipherCacheCipherEntry entry = new CipherCacheCipherEntry(
				setKeyData(keyID, keyData), encryptionAlgorithm, cipher
		);
		if (iv == null) {
			iv = new byte[entry.getCipher().getIVSize()];
			random.nextBytes(iv);
		}
		entry.getCipher().init(
				opmode,
				new ParametersWithIV(new KeyParameter(keyData), iv) // with key, because 1st time we use this cipher
		);
		return entry;
	}

	public void releaseCipherEntry(CipherCacheCipherEntry cipherEntry)
	{
		if (cipherEntry == null)
			return;

		Map<EncryptionAlgorithm, Map<Long, List<CipherCacheCipherEntry>>> encryptionAlgorithm2keyID2cipherEntries;
		synchronized (opmode2encryptionAlgorithm2keyID2cipherEntries) {
			encryptionAlgorithm2keyID2cipherEntries =
				opmode2encryptionAlgorithm2keyID2cipherEntries.get(cipherEntry.getCipher().getMode());

			if (encryptionAlgorithm2keyID2cipherEntries == null) {
				encryptionAlgorithm2keyID2cipherEntries = Collections.synchronizedMap(
						new HashMap<EncryptionAlgorithm, Map<Long,List<CipherCacheCipherEntry>>>()
				);

				opmode2encryptionAlgorithm2keyID2cipherEntries.put(
						cipherEntry.getCipher().getMode(), encryptionAlgorithm2keyID2cipherEntries
				);
			}
		}

		Map<Long, List<CipherCacheCipherEntry>> keyID2cipherEntries;
		synchronized (encryptionAlgorithm2keyID2cipherEntries) {
			keyID2cipherEntries = encryptionAlgorithm2keyID2cipherEntries.get(cipherEntry.getEncryptionAlgorithm());
			if (keyID2cipherEntries == null) {
				keyID2cipherEntries = Collections.synchronizedMap(new HashMap<Long, List<CipherCacheCipherEntry>>());
				encryptionAlgorithm2keyID2cipherEntries.put(cipherEntry.getEncryptionAlgorithm(), keyID2cipherEntries);
			}
		}

		List<CipherCacheCipherEntry> cipherEntries;
		synchronized (keyID2cipherEntries) {
			cipherEntries = keyID2cipherEntries.get(cipherEntry.getKeyEntry().getKeyID());
			if (cipherEntries == null) {
				cipherEntries = Collections.synchronizedList(new LinkedList<CipherCacheCipherEntry>());
				keyID2cipherEntries.put(cipherEntry.getKeyEntry().getKeyID(), cipherEntries);
			}
		}

		cipherEntries.add(cipherEntry);
	}

	public void clear() {
		keyID2key.clear();
		opmode2encryptionAlgorithm2keyID2cipherEntries.clear();
	}
}
