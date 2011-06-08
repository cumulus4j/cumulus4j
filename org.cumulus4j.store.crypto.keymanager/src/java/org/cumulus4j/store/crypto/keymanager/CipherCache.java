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

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.CipherOperationMode;
import org.cumulus4j.crypto.CryptoRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CipherCache
{
	// TODO we need a mechanism removing old entries from this cache!!!
	private static final Logger logger = LoggerFactory.getLogger(CipherCache.class);

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
		if (entry == null) {
			if (logger.isTraceEnabled()) logger.trace("getKeyData: No cached key with keyID={} found.", keyID);
			return null;
		}
		else {
			if (logger.isTraceEnabled()) logger.trace("getKeyData: Found cached key with keyID={}.", keyID);
			return entry.getKeyData();
		}
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
					CipherCacheCipherEntry entry = popOrNull(encrypters);
					if (entry != null) {
						entry = new CipherCacheCipherEntry(
								setKeyData(keyID, entry.getKeyEntry().getKeyData()), entry
						);
						if (iv == null) {
							iv = new byte[entry.getCipher().getIVSize()];
							random.nextBytes(iv);
						}

						if (logger.isTraceEnabled())
							logger.trace(
									"acquireCipherEntry: Found cached Cipher@{} for opmode={}, encryptionAlgorithm={} and keyID={}. Initialising it with new IV (without key).",
									new Object[] { System.identityHashCode(entry.getCipher()), opmode, encryptionAlgorithm, keyID }
							);

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
			cipher = CryptoRegistry.sharedInstance().createCipher(encryptionAlgorithm.getTransformation());
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

		if (logger.isTraceEnabled())
			logger.trace(
					"acquireCipherEntry: Created new Cipher@{} for opmode={}, encryptionAlgorithm={} and keyID={}. Initialising it with key and IV.",
					new Object[] { System.identityHashCode(entry.getCipher()), opmode, encryptionAlgorithm, keyID }
			);

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

		if (logger.isTraceEnabled())
			logger.trace(
					"releaseCipherEntry: Releasing Cipher@{} for opmode={}, encryptionAlgorithm={} keyID={}.",
					new Object[] {
							System.identityHashCode(cipherEntry.getCipher()),
							cipherEntry.getCipher().getMode(),
							cipherEntry.getEncryptionAlgorithm(),
							cipherEntry.getKeyEntry().getKeyID()
					}
			);

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

	public void clear()
	{
		logger.trace("clear: entered");
		keyID2key.clear();
		opmode2encryptionAlgorithm2keyID2cipherEntries.clear();
	}

	private Map<String, CipherCacheKeyEncryptionKeyEntry> keyEncryptionTransformation2keyEncryptionKey = Collections.synchronizedMap(
			new HashMap<String, CipherCacheKeyEncryptionKeyEntry>()
	);

	private Map<String, List<CipherCacheKeyDecrypterEntry>> keyEncryptionTransformation2keyDecryptors = Collections.synchronizedMap(
			new HashMap<String, List<CipherCacheKeyDecrypterEntry>>()
	);

	protected long getKeyEncryptionKeyActivePeriodMSec()
	{
		return 3600L * 1000L;
	}

	protected CipherCacheKeyEncryptionKeyEntry getKeyEncryptionKey(String keyEncryptionTransformation)
	{
		synchronized (keyEncryptionTransformation2keyEncryptionKey) {
			CipherCacheKeyEncryptionKeyEntry entry = keyEncryptionTransformation2keyEncryptionKey.get(keyEncryptionTransformation);
			if (entry != null && !entry.isExpired())
				return entry;
			else
				entry = null;

			String engineAlgorithmName = CryptoRegistry.splitTransformation(keyEncryptionTransformation)[0];

			AsymmetricCipherKeyPairGenerator keyPairGenerator;
			try {
				keyPairGenerator = CryptoRegistry.sharedInstance().createKeyPairGenerator(engineAlgorithmName);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}

			AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();
			entry = new CipherCacheKeyEncryptionKeyEntry(keyPair, getKeyEncryptionKeyActivePeriodMSec());
			keyEncryptionTransformation2keyEncryptionKey.put(keyEncryptionTransformation, entry);
			return entry;
		}
	}

	/**
	 * Remove the first element from the given list and return it.
	 * If the list is empty, return <code>null</code>.
	 *
	 * @param <T> the type of the list's elements.
	 * @param list the list; must not be <code>null</code>.
	 * @return the first element of the list (after removing it) or <code>null</code>, if the list
	 * was empty.
	 */
	private static <T> T popOrNull(List<? extends T> list)
	{
		try {
			T element = list.remove(0);
			return element;
		} catch (IndexOutOfBoundsException x) {
			return null;
		}
	}

	public CipherCacheKeyDecrypterEntry acquireKeyDecryptor(String keyEncryptionTransformation)
	{
		List<CipherCacheKeyDecrypterEntry> decryptors = keyEncryptionTransformation2keyDecryptors.get(keyEncryptionTransformation);
		if (decryptors != null) {
			CipherCacheKeyDecrypterEntry entry;
			do {
				entry = popOrNull(decryptors);
				if (entry != null && !entry.getKeyEncryptionKey().isExpired()) {
					return entry;
				}
			} while (entry != null);
		}

		Cipher keyDecryptor;
		try {
			keyDecryptor = CryptoRegistry.sharedInstance().createCipher(keyEncryptionTransformation);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}

		CipherCacheKeyEncryptionKeyEntry keyEncryptionKey = getKeyEncryptionKey(keyEncryptionTransformation);
		keyDecryptor.init(CipherOperationMode.DECRYPT, keyEncryptionKey.getKeyPair().getPrivate());
		CipherCacheKeyDecrypterEntry entry = new CipherCacheKeyDecrypterEntry(keyEncryptionKey, keyEncryptionTransformation, keyDecryptor);
		return entry;
	}

	public void releaseKeyDecryptor(CipherCacheKeyDecrypterEntry decryptorEntry)
	{
		if (decryptorEntry == null)
			return;

		List<CipherCacheKeyDecrypterEntry> keyDecryptors;
		synchronized (keyEncryptionTransformation2keyDecryptors) {
			keyDecryptors = keyEncryptionTransformation2keyDecryptors.get(decryptorEntry.getKeyEncryptionTransformation());
			if (keyDecryptors == null) {
				keyDecryptors = Collections.synchronizedList(new LinkedList<CipherCacheKeyDecrypterEntry>());
				keyEncryptionTransformation2keyDecryptors.put(decryptorEntry.getKeyEncryptionTransformation(), keyDecryptors);
			}
		}

		keyDecryptors.add(decryptorEntry);
	}

	protected AsymmetricCipherKeyPairGenerator getAsymmetricCipherKeyPairGenerator(String keyEncryptionTransformation)
	{
		String algorithmName = CryptoRegistry.splitTransformation(keyEncryptionTransformation)[0];
		try {
			return CryptoRegistry.sharedInstance().createKeyPairGenerator(algorithmName);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
