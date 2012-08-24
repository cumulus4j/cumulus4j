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

import java.lang.ref.WeakReference;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.crypto.NoSuchPaddingException;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.CipherOperationMode;
import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.store.crypto.AbstractCryptoManager;
import org.cumulus4j.store.crypto.CryptoManagerRegistry;
import org.datanucleus.NucleusContext;
import org.datanucleus.PersistenceConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Cache for secret keys, {@link Cipher}s and other crypto-related objects.
 * </p><p>
 * There exists one instance of <code>CryptoCache</code> per {@link KeyManagerCryptoManager}.
 * This cache therefore holds objects across multiple {@link KeyManagerCryptoSession sessions}.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoCache
{
	private static final Logger logger = LoggerFactory.getLogger(CryptoCache.class);

	private SecureRandom random = new SecureRandom();
	private long activeEncryptionKeyID = -1;
	private Date activeEncryptionKeyUntilExcl = null;
	private Object activeEncryptionKeyMutex = new Object();

	private Map<Long, CryptoCacheKeyEntry> keyID2key = Collections.synchronizedMap(new HashMap<Long, CryptoCacheKeyEntry>());

	private Map<CipherOperationMode, Map<String, Map<Long, List<CryptoCacheCipherEntry>>>> opmode2cipherTransformation2keyID2cipherEntries = Collections.synchronizedMap(
		new HashMap<CipherOperationMode, Map<String,Map<Long,List<CryptoCacheCipherEntry>>>>()
	);

	private KeyManagerCryptoManager cryptoManager;

	/**
	 * Create a <code>CryptoCache</code> instance.
	 * @param cryptoManager the owning <code>CryptoManager</code>.
	 */
	public CryptoCache(KeyManagerCryptoManager cryptoManager)
	{
		if (cryptoManager == null)
			throw new IllegalArgumentException("cryptoManager == null");

		this.cryptoManager = cryptoManager;
	}

	/**
	 * Get the currently active encryption key. If there has none yet be {@link #setActiveEncryptionKeyID(long, Date) set}
	 * or the <code>activeUntilExcl</code> has been reached (i.e. the previous active key expired),
	 * this method returns -1.
	 * @return the currently active encryption key or -1, if there is none.
	 * @see #setActiveEncryptionKeyID(long, Date)
	 */
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

	/**
	 * Set the currently active encryption key.
	 * @param activeEncryptionKeyID identifier of the symmetric secret key that is currently active.
	 * @param activeUntilExcl timestamp until when (excluding) the specified key is active.
	 * @see #getActiveEncryptionKeyID()
	 */
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

	/**
	 * Get the actual key data for the given key identifier.
	 * @param keyID identifier of the requested key.
	 * @return actual key data or <code>null</code>, if the specified key is not cached.
	 */
	protected byte[] getKeyData(long keyID)
	{
		CryptoCacheKeyEntry entry = keyID2key.get(keyID);
		if (entry == null) {
			if (logger.isTraceEnabled()) logger.trace("getKeyData: No cached key with keyID={} found.", keyID);
			return null;
		}
		else {
			if (logger.isTraceEnabled()) logger.trace("getKeyData: Found cached key with keyID={}.", keyID);
			return entry.getKeyData();
		}
	}

	/**
	 * Put a certain key into this cache.
	 * @param keyID identifier of the key. Must be &lt;= 0.
	 * @param keyData actual key. Must not be <code>null</code>.
	 * @return the immutable entry for the given key in this cache.
	 */
	protected CryptoCacheKeyEntry setKeyData(long keyID, byte[] keyData)
	{
		CryptoCacheKeyEntry entry = new CryptoCacheKeyEntry(keyID, keyData);
		keyID2key.put(keyID, entry);
		return entry;
	}

	/**
	 * <p>
	 * Acquire a decrypter and {@link Cipher#init(CipherOperationMode, org.bouncycastle.crypto.CipherParameters) initialise} it so that
	 * it is ready to be used.
	 * </p><p>
	 * This method can only return a <code>Cipher</code>, if there is one cached, already, or at least the key is cached so that a new
	 * <code>Cipher</code> can be created. If there is neither a cipher nor a key cached, this method returns <code>null</code>.
	 * The key - if found - is refreshed (with the current timestamp) by this operation and will thus be evicted later.
	 * </p><p>
	 * <b>Important:</b> You must use a try-finally-block ensuring that {@link #releaseCipherEntry(CryptoCacheCipherEntry)} is called!
	 * </p>
	 *
	 * @param cipherTransformation the encryption algorithm (the complete transformation as passed to {@link CryptoRegistry#createCipher(String)}).
	 * @param keyID identifier of the key.
	 * @param iv initialisation vector. Must be the same as the one that was used for encryption.
	 * @return <code>null</code> or an entry wrapping the desired cipher.
	 * @see #acquireDecrypter(String, long, byte[], byte[])
	 * @see #releaseCipherEntry(CryptoCacheCipherEntry)
	 */
	public CryptoCacheCipherEntry acquireDecrypter(String cipherTransformation, long keyID, byte[] iv)
	{
		return acquireDecrypter(cipherTransformation, keyID, null, iv);
	}

	/**
	 * <p>
	 * Acquire a decrypter and {@link Cipher#init(CipherOperationMode, org.bouncycastle.crypto.CipherParameters) initialise} it so that
	 * it is ready to be used.
	 * </p><p>
	 * This method returns an existing <code>Cipher</code>, if there is one cached, already. Otherwise a new <code>Cipher</code> is created.
	 * The key is added (with the current timestamp) into the cache.
	 * </p><p>
	 * <b>Important:</b> You must use a try-finally-block ensuring that {@link #releaseCipherEntry(CryptoCacheCipherEntry)} is called!
	 * </p>
	 *
	 * @param encryptionAlgorithm the encryption algorithm (the complete transformation as passed to {@link CryptoRegistry#createCipher(String)}).
	 * @param keyID identifier of the key.
	 * @param keyData the actual key. If it is <code>null</code>, the key is fetched from the cache. If it is not cached,
	 * this method returns <code>null</code>.
	 * @param iv initialisation vector. Must be the same as the one that was used for encryption.
	 * @return an entry wrapping the desired cipher. Never returns <code>null</code>, if <code>keyData</code> was specified.
	 * If <code>keyData == null</code> and the key is not cached, <code>null</code> is returned.
	 * @see #acquireDecrypter(String, long, byte[])
	 * @see #releaseCipherEntry(CryptoCacheCipherEntry)
	 */
	public CryptoCacheCipherEntry acquireDecrypter(String encryptionAlgorithm, long keyID, byte[] keyData, byte[] iv)
	{
		return acquireCipherEntry(CipherOperationMode.DECRYPT, encryptionAlgorithm, keyID, keyData, iv);
	}

	/**
	 * <p>
	 * Acquire an encrypter and {@link Cipher#init(CipherOperationMode, org.bouncycastle.crypto.CipherParameters) initialise} it so that
	 * it is ready to be used.
	 * </p><p>
	 * This method can only return a <code>Cipher</code>, if there is one cached, already, or at least the key is cached so that a new
	 * <code>Cipher</code> can be created. If there is neither a cipher nor a key cached, this method returns <code>null</code>.
	 * The key - if found - is refreshed (with the current timestamp) by this operation and will thus be evicted later.
	 * </p><p>
	 * You should use a try-finally-block ensuring that {@link #releaseCipherEntry(CryptoCacheCipherEntry)} is called!
	 * </p><p>
	 * This method generates a random IV (initialisation vector) every time it is called. The IV can be obtained via
	 * {@link Cipher#getParameters()} and casting the result to {@link ParametersWithIV}. The IV is required for decryption.
	 * </p>
	 *
	 * @param encryptionAlgorithm the encryption algorithm (the complete transformation as passed to {@link CryptoRegistry#createCipher(String)}).
	 * @param keyID identifier of the key.
	 * @return <code>null</code> or an entry wrapping the desired cipher.
	 * @see #acquireEncrypter(String, long, byte[])
	 * @see #releaseCipherEntry(CryptoCacheCipherEntry)
	 */
	public CryptoCacheCipherEntry acquireEncrypter(String encryptionAlgorithm, long keyID)
	{
		return acquireEncrypter(encryptionAlgorithm, keyID, null);
	}

	/**
	 * <p>
	 * Acquire an encrypter and {@link Cipher#init(CipherOperationMode, org.bouncycastle.crypto.CipherParameters) initialise} it so that
	 * it is ready to be used.
	 * </p><p>
	 * This method returns an existing <code>Cipher</code>, if there is one cached, already. Otherwise a new <code>Cipher</code> is created.
	 * The key is added (with the current timestamp) into the cache.
	 * </p><p>
	 * You should use a try-finally-block ensuring that {@link #releaseCipherEntry(CryptoCacheCipherEntry)} is called!
	 * </p><p>
	 * This method generates a random IV (initialisation vector) every time it is called. The IV can be obtained via
	 * {@link Cipher#getParameters()} and casting the result to {@link ParametersWithIV}. The IV is required for decryption.
	 * </p>
	 *
	 * @param cipherTransformation the encryption algorithm (the complete transformation as passed to {@link CryptoRegistry#createCipher(String)}).
	 * @param keyID identifier of the key.
	 * @param keyData the actual key. If it is <code>null</code>, the key is fetched from the cache. If it is not cached,
	 * this method returns <code>null</code>.
	 * @return an entry wrapping the desired cipher. Never returns <code>null</code>, if <code>keyData</code> was specified.
	 * If <code>keyData == null</code> and the key is not cached, <code>null</code> is returned.
	 * @see #acquireEncrypter(String, long)
	 * @see #releaseCipherEntry(CryptoCacheCipherEntry)
	 */
	public CryptoCacheCipherEntry acquireEncrypter(String cipherTransformation, long keyID, byte[] keyData)
	{
		return acquireCipherEntry(CipherOperationMode.ENCRYPT, cipherTransformation, keyID, keyData, null);
	}

	private CryptoCacheCipherEntry acquireCipherEntry(
			CipherOperationMode opmode, String cipherTransformation, long keyID, byte[] keyData, byte[] iv
	)
	{
		try {
			Map<String, Map<Long, List<CryptoCacheCipherEntry>>> cipherTransformation2keyID2encrypters =
				opmode2cipherTransformation2keyID2cipherEntries.get(opmode);

			if (cipherTransformation2keyID2encrypters != null) {
				Map<Long, List<CryptoCacheCipherEntry>> keyID2Encrypters = cipherTransformation2keyID2encrypters.get(cipherTransformation);
				if (keyID2Encrypters != null) {
					List<CryptoCacheCipherEntry> encrypters = keyID2Encrypters.get(keyID);
					if (encrypters != null) {
						CryptoCacheCipherEntry entry = popOrNull(encrypters);
						if (entry != null) {
							entry = new CryptoCacheCipherEntry(
									setKeyData(keyID, entry.getKeyEntry().getKeyData()), entry
							);
							if (iv == null) {
								iv = new byte[entry.getCipher().getIVSize()];
								random.nextBytes(iv);
							}

							if (logger.isTraceEnabled())
								logger.trace(
										"acquireCipherEntry: Found cached Cipher@{} for opmode={}, encryptionAlgorithm={} and keyID={}. Initialising it with new IV (without key).",
										new Object[] { System.identityHashCode(entry.getCipher()), opmode, cipherTransformation, keyID }
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
				cipher = CryptoRegistry.sharedInstance().createCipher(cipherTransformation);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (NoSuchPaddingException e) {
				throw new RuntimeException(e);
			}

			CryptoCacheCipherEntry entry = new CryptoCacheCipherEntry(
					setKeyData(keyID, keyData), cipherTransformation, cipher
			);
			if (iv == null) {
				iv = new byte[entry.getCipher().getIVSize()];
				random.nextBytes(iv);
			}

			if (logger.isTraceEnabled())
				logger.trace(
						"acquireCipherEntry: Created new Cipher@{} for opmode={}, encryptionAlgorithm={} and keyID={}. Initialising it with key and IV.",
						new Object[] { System.identityHashCode(entry.getCipher()), opmode, cipherTransformation, keyID }
				);

			entry.getCipher().init(
					opmode,
					new ParametersWithIV(new KeyParameter(keyData), iv) // with key, because 1st time we use this cipher
			);
			return entry;
		} finally {
			// We do this at the end in order to maybe still fetch an entry that is about to expire just right now.
			// Otherwise it might happen, that we delete one and recreate it again instead of just reusing it. Marco :-)
			initTimerTaskOrRemoveExpiredEntriesPeriodically();
		}
	}

	/**
	 * <p>
	 * Release a {@link Cipher} wrapped in the given entry.
	 * </p><p>
	 * This should be called in a finally block ensuring that the Cipher is put back into the cache.
	 * </p>
	 * @param cipherEntry the entry to be put back into the cache or <code>null</code>, if it was not yet assigned.
	 * This method accepts <code>null</code> as argument to make usage in a try-finally-block easier and less error-prone
	 * (no <code>null</code>-checks required).
	 * @see #acquireDecrypter(String, long, byte[])
	 * @see #acquireDecrypter(String, long, byte[], byte[])
	 * @see #acquireEncrypter(String, long)
	 * @see #acquireEncrypter(String, long, byte[])
	 */
	public void releaseCipherEntry(CryptoCacheCipherEntry cipherEntry)
	{
		if (cipherEntry == null)
			return;

		if (logger.isTraceEnabled())
			logger.trace(
					"releaseCipherEntry: Releasing Cipher@{} for opmode={}, encryptionAlgorithm={} keyID={}.",
					new Object[] {
							System.identityHashCode(cipherEntry.getCipher()),
							cipherEntry.getCipher().getMode(),
							cipherEntry.getCipherTransformation(),
							cipherEntry.getKeyEntry().getKeyID()
					}
			);

		Map<String, Map<Long, List<CryptoCacheCipherEntry>>> cipherTransformation2keyID2cipherEntries;
		synchronized (opmode2cipherTransformation2keyID2cipherEntries) {
			cipherTransformation2keyID2cipherEntries =
				opmode2cipherTransformation2keyID2cipherEntries.get(cipherEntry.getCipher().getMode());

			if (cipherTransformation2keyID2cipherEntries == null) {
				cipherTransformation2keyID2cipherEntries = Collections.synchronizedMap(
						new HashMap<String, Map<Long,List<CryptoCacheCipherEntry>>>()
				);

				opmode2cipherTransformation2keyID2cipherEntries.put(
						cipherEntry.getCipher().getMode(), cipherTransformation2keyID2cipherEntries
				);
			}
		}

		Map<Long, List<CryptoCacheCipherEntry>> keyID2cipherEntries;
		synchronized (cipherTransformation2keyID2cipherEntries) {
			keyID2cipherEntries = cipherTransformation2keyID2cipherEntries.get(cipherEntry.getCipherTransformation());
			if (keyID2cipherEntries == null) {
				keyID2cipherEntries = Collections.synchronizedMap(new HashMap<Long, List<CryptoCacheCipherEntry>>());
				cipherTransformation2keyID2cipherEntries.put(cipherEntry.getCipherTransformation(), keyID2cipherEntries);
			}
		}

		List<CryptoCacheCipherEntry> cipherEntries;
		synchronized (keyID2cipherEntries) {
			cipherEntries = keyID2cipherEntries.get(cipherEntry.getKeyEntry().getKeyID());
			if (cipherEntries == null) {
				cipherEntries = Collections.synchronizedList(new LinkedList<CryptoCacheCipherEntry>());
				keyID2cipherEntries.put(cipherEntry.getKeyEntry().getKeyID(), cipherEntries);
			}
		}

		cipherEntries.add(cipherEntry);
	}

	/**
	 * Clear this cache entirely. This evicts all cached objects - no matter what type.
	 */
	public void clear()
	{
		logger.trace("clear: entered");
		keyID2key.clear();
		opmode2cipherTransformation2keyID2cipherEntries.clear();
		synchronized (activeEncryptionKeyMutex) {
			activeEncryptionKeyID = -1;
			activeEncryptionKeyUntilExcl = null;
		}
	}

	private Map<String, CryptoCacheKeyEncryptionKeyEntry> keyEncryptionTransformation2keyEncryptionKey = Collections.synchronizedMap(
			new HashMap<String, CryptoCacheKeyEncryptionKeyEntry>()
	);

	private Map<String, List<CryptoCacheKeyDecrypterEntry>> keyEncryptionTransformation2keyDecryptors = Collections.synchronizedMap(
			new HashMap<String, List<CryptoCacheKeyDecrypterEntry>>()
	);

	/**
	 * How long should the public-private-key-pair for secret-key-encryption be used. After that time, a new
	 * public-private-key-pair is generated.
	 * @return the time a public-private-key-pair should be used.
	 * @see #getKeyEncryptionKey(String)
	 */
	protected long getKeyEncryptionKeyActivePeriodMSec()
	{
		return 3600L * 1000L * 5L; // use the same key pair for 5 hours - TODO must make this configurable via a persistence property!
	}

	/**
	 * Get the key-pair that is currently active for secret-key-encryption.
	 * @param keyEncryptionTransformation the transformation to be used for secret-key-encryption. Must not be <code>null</code>.
	 * @return entry wrapping the key-pair that is currently active for secret-key-encryption.
	 */
	protected CryptoCacheKeyEncryptionKeyEntry getKeyEncryptionKey(String keyEncryptionTransformation)
	{
		if (keyEncryptionTransformation == null)
			throw new IllegalArgumentException("keyEncryptionTransformation == null");

		synchronized (keyEncryptionTransformation2keyEncryptionKey) {
			CryptoCacheKeyEncryptionKeyEntry entry = keyEncryptionTransformation2keyEncryptionKey.get(keyEncryptionTransformation);
			if (entry != null && !entry.isExpired())
				return entry;
			else
				entry = null;

			String engineAlgorithmName = CryptoRegistry.splitTransformation(keyEncryptionTransformation)[0];

			AsymmetricCipherKeyPairGenerator keyPairGenerator;
			try {
				keyPairGenerator = CryptoRegistry.sharedInstance().createKeyPairGenerator(engineAlgorithmName, true);
			} catch (NoSuchAlgorithmException e) {
				throw new RuntimeException(e);
			} catch (IllegalArgumentException e) {
				throw new RuntimeException(e);
			}

			AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();
			entry = new CryptoCacheKeyEncryptionKeyEntry(keyPair, getKeyEncryptionKeyActivePeriodMSec());
			keyEncryptionTransformation2keyEncryptionKey.put(keyEncryptionTransformation, entry);
			return entry;
		}
	}

	/**
	 * Remove the first element from the given list and return it.
	 * If the list is empty, return <code>null</code>. This method is thread-safe, if the given <code>list</code> is.
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

	/**
	 * Acquire a cipher to be used for secret-key-decryption. The cipher is already initialised with the current
	 * {@link #getKeyEncryptionKey(String) keyEncryptionKey} and can thus be directly used.
	 * <p>
	 * You should call {@link #releaseKeyDecryptor(CryptoCacheKeyDecrypterEntry)} to put the cipher back into the cache!
	 * </p>
	 * @param keyEncryptionTransformation the transformation to be used for secret-key-encryption. Must not be <code>null</code>.
	 * @return entry wrapping the cipher that is ready to be used for secret-key-decryption.
	 * @see #releaseKeyDecryptor(CryptoCacheKeyDecrypterEntry)
	 */
	public CryptoCacheKeyDecrypterEntry acquireKeyDecryptor(String keyEncryptionTransformation)
	{
		if (keyEncryptionTransformation == null)
			throw new IllegalArgumentException("keyEncryptionTransformation == null");

		try {
			List<CryptoCacheKeyDecrypterEntry> decryptors = keyEncryptionTransformation2keyDecryptors.get(keyEncryptionTransformation);
			if (decryptors != null) {
				CryptoCacheKeyDecrypterEntry entry;
				do {
					entry = popOrNull(decryptors);
					if (entry != null && !entry.getKeyEncryptionKey().isExpired()) {
						entry.updateLastUsageTimestamp();
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

			CryptoCacheKeyEncryptionKeyEntry keyEncryptionKey = getKeyEncryptionKey(keyEncryptionTransformation);
			keyDecryptor.init(CipherOperationMode.DECRYPT, keyEncryptionKey.getKeyPair().getPrivate());
			CryptoCacheKeyDecrypterEntry entry = new CryptoCacheKeyDecrypterEntry(keyEncryptionKey, keyEncryptionTransformation, keyDecryptor);
			return entry;
		} finally {
			// We do this at the end in order to maybe still fetch an entry that is about to expire just right now.
			// Otherwise it might happen, that we delete one and recreate it again instead of just reusing it. Marco :-)
			initTimerTaskOrRemoveExpiredEntriesPeriodically();
		}
	}

	/**
	 * Release a cipher (put it back into the cache).
	 * @param decryptorEntry the entry to be released or <code>null</code> (silently ignored).
	 */
	public void releaseKeyDecryptor(CryptoCacheKeyDecrypterEntry decryptorEntry)
	{
		if (decryptorEntry == null)
			return;

		List<CryptoCacheKeyDecrypterEntry> keyDecryptors;
		synchronized (keyEncryptionTransformation2keyDecryptors) {
			keyDecryptors = keyEncryptionTransformation2keyDecryptors.get(decryptorEntry.getKeyEncryptionTransformation());
			if (keyDecryptors == null) {
				keyDecryptors = Collections.synchronizedList(new LinkedList<CryptoCacheKeyDecrypterEntry>());
				keyEncryptionTransformation2keyDecryptors.put(decryptorEntry.getKeyEncryptionTransformation(), keyDecryptors);
			}
		}

		keyDecryptors.add(decryptorEntry);
	}

	/**
	 * Get a key-pair-generator for the given transformation.
	 * @param keyEncryptionTransformation the transformation (based on an asymmetric crypto algorithm) for which to obtain
	 * a key-pair-generator.
	 * @return the key-pair-generator.
	 */
	protected AsymmetricCipherKeyPairGenerator getAsymmetricCipherKeyPairGenerator(String keyEncryptionTransformation)
	{
		String algorithmName = CryptoRegistry.splitTransformation(keyEncryptionTransformation)[0];
		try {
			return CryptoRegistry.sharedInstance().createKeyPairGenerator(algorithmName, true);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}


	private static volatile Timer cleanupTimer = null;
	private static volatile boolean cleanupTimerInitialised = false;
	private volatile boolean cleanupTaskInitialised = false;

	private static class CleanupTask extends TimerTask
	{
		private final Logger logger = LoggerFactory.getLogger(CleanupTask.class);

		private WeakReference<CryptoCache> cryptoCacheRef;
		private final long expiryTimerPeriodMSec;

		public CleanupTask(CryptoCache cryptoCache, long expiryTimerPeriodMSec)
		{
			if (cryptoCache == null)
				throw new IllegalArgumentException("cryptoCache == null");

			this.cryptoCacheRef = new WeakReference<CryptoCache>(cryptoCache);
			this.expiryTimerPeriodMSec = expiryTimerPeriodMSec;
		}

		@Override
		public void run() {
			try {
				logger.debug("run: entered");
				final CryptoCache cryptoCache = cryptoCacheRef.get();
				if (cryptoCache == null) {
					logger.info("run: CryptoCache was garbage-collected. Cancelling this TimerTask.");
					this.cancel();
					return;
				}

				cryptoCache.removeExpiredEntries(true);

				long currentPeriodMSec = cryptoCache.getCleanupTimerPeriod();
				if (currentPeriodMSec != expiryTimerPeriodMSec) {
					logger.info(
							"run: The expiryTimerPeriodMSec changed (oldValue={}, newValue={}). Re-scheduling this task.",
							expiryTimerPeriodMSec, currentPeriodMSec
					);
					this.cancel();

					cleanupTimer.schedule(new CleanupTask(cryptoCache, currentPeriodMSec), currentPeriodMSec, currentPeriodMSec);
				}
			} catch (Throwable x) {
				// The TimerThread is cancelled, if a task throws an exception. Furthermore, they are not logged at all.
				// Since we do not want the TimerThread to die, we catch everything (Throwable - not only Exception) and log
				// it here. IMHO there's nothing better we can do. Marco :-)
				logger.error("run: " + x, x);
			}
		}
	};

	private final void initTimerTaskOrRemoveExpiredEntriesPeriodically()
	{
		if (!cleanupTimerInitialised) {
			synchronized (AbstractCryptoManager.class) {
				if (!cleanupTimerInitialised) {
					if (getCleanupTimerEnabled())
						cleanupTimer = new Timer(CryptoCache.class.getSimpleName(), true);

					cleanupTimerInitialised = true;
				}
			}
		}

		if (!cleanupTaskInitialised) {
			synchronized (this) {
				if (!cleanupTaskInitialised) {
					if (cleanupTimer != null) {
						long periodMSec = getCleanupTimerPeriod();
						cleanupTimer.schedule(new CleanupTask(this, periodMSec), periodMSec, periodMSec);
					}
					cleanupTaskInitialised = true;
				}
			}
		}

		if (cleanupTimer == null) {
			logger.trace("initTimerTaskOrRemoveExpiredEntriesPeriodically: No timer enabled => calling removeExpiredEntries(false) now.");
			removeExpiredEntries(false);
		}
	}

	private Date lastRemoveExpiredEntriesTimestamp = null;

	private void removeExpiredEntries(boolean force)
	{
		synchronized (this) {
			if (
					!force && (
							lastRemoveExpiredEntriesTimestamp != null &&
							lastRemoveExpiredEntriesTimestamp.after(new Date(System.currentTimeMillis() - getCleanupTimerPeriod()))
					)
			)
			{
				logger.trace("removeExpiredEntries: force == false and period not yet elapsed. Skipping.");
				return;
			}

			lastRemoveExpiredEntriesTimestamp = new Date();
		}

		Date removeEntriesBeforeThisTimestamp = new Date(
				System.currentTimeMillis() - getCryptoCacheEntryExpiryAge()
		);

		int totalEntryCounter = 0;
		int removedEntryCounter = 0;
		synchronized (keyEncryptionTransformation2keyEncryptionKey) {
			for (Iterator<Map.Entry<String, CryptoCacheKeyEncryptionKeyEntry>> it1 = keyEncryptionTransformation2keyEncryptionKey.entrySet().iterator(); it1.hasNext(); ) {
				Map.Entry<String, CryptoCacheKeyEncryptionKeyEntry> me1 = it1.next();
				if (me1.getValue().isExpired()) {
					it1.remove();
					++removedEntryCounter;
				}
				else
					++totalEntryCounter;
			}
		}
		logger.debug("removeExpiredEntries: Removed {} instances of CryptoCacheKeyEncryptionKeyEntry ({} left).", removedEntryCounter, totalEntryCounter);


		// There are not many keyEncryptionTransformations (usually only ONE!), hence copying this is fine and very fast.
		String[] keyEncryptionTransformations;
		synchronized (keyEncryptionTransformation2keyDecryptors) {
			keyEncryptionTransformations = keyEncryptionTransformation2keyDecryptors.keySet().toArray(
					new String[keyEncryptionTransformation2keyDecryptors.size()]
			);
		}

		totalEntryCounter = 0;
		removedEntryCounter = 0;
		for (String keyEncryptionTransformation : keyEncryptionTransformations) {
			List<CryptoCacheKeyDecrypterEntry> entries = keyEncryptionTransformation2keyDecryptors.get(keyEncryptionTransformation);
			if (entries == null) // should never happen, but better check :-)
				continue;

			synchronized (entries) {
				for (Iterator<CryptoCacheKeyDecrypterEntry> itEntry = entries.iterator(); itEntry.hasNext(); ) {
					CryptoCacheKeyDecrypterEntry entry = itEntry.next();
					if (entry.getLastUsageTimestamp().before(removeEntriesBeforeThisTimestamp) || entry.getKeyEncryptionKey().isExpired()) {
						itEntry.remove();
						++removedEntryCounter;
					}
					else
						++totalEntryCounter;
				}
			}
		}
		logger.debug("removeExpiredEntries: Removed {} instances of CryptoCacheKeyDecrypterEntry ({} left).", removedEntryCounter, totalEntryCounter);


		totalEntryCounter = 0;
		removedEntryCounter = 0;
		synchronized (keyID2key) {
			for (Iterator<Map.Entry<Long, CryptoCacheKeyEntry>> it1 = keyID2key.entrySet().iterator(); it1.hasNext(); ) {
				Map.Entry<Long, CryptoCacheKeyEntry> me1 = it1.next();
				if (me1.getValue().getLastUsageTimestamp().before(removeEntriesBeforeThisTimestamp)) {
					it1.remove();
					++removedEntryCounter;
				}
				else
					++totalEntryCounter;
			}
		}
		logger.debug("removeExpiredEntries: Removed {} instances of CryptoCacheKeyEntry ({} left).", removedEntryCounter, totalEntryCounter);


		totalEntryCounter = 0;
		removedEntryCounter = 0;
		int totalListCounter = 0;
		int removedListCounter = 0;
		for (CipherOperationMode opmode : CipherOperationMode.values()) {
			Map<String, Map<Long, List<CryptoCacheCipherEntry>>> encryptionAlgorithm2keyID2cipherEntries = opmode2cipherTransformation2keyID2cipherEntries.get(opmode);
			if (encryptionAlgorithm2keyID2cipherEntries == null)
				continue;

			// There are not many encryptionAlgorithms (usually only ONE!), hence copying this is fine and very fast.
			String[] encryptionAlgorithms;
			synchronized (encryptionAlgorithm2keyID2cipherEntries) {
				encryptionAlgorithms = encryptionAlgorithm2keyID2cipherEntries.keySet().toArray(
						new String[encryptionAlgorithm2keyID2cipherEntries.size()]
				);
			}

			for (String encryptionAlgorithm : encryptionAlgorithms) {
				Map<Long, List<CryptoCacheCipherEntry>> keyID2cipherEntries = encryptionAlgorithm2keyID2cipherEntries.get(encryptionAlgorithm);
				if (keyID2cipherEntries == null) // should never happen, but well, better check ;-)
					continue;

				synchronized (keyID2cipherEntries) {
					for (Iterator<Map.Entry<Long, List<CryptoCacheCipherEntry>>> it1 = keyID2cipherEntries.entrySet().iterator(); it1.hasNext(); ) {
						Map.Entry<Long, List<CryptoCacheCipherEntry>> me1 = it1.next();
						List<CryptoCacheCipherEntry> entries = me1.getValue();
						synchronized (entries) {
							for (Iterator<CryptoCacheCipherEntry> it2 = entries.iterator(); it2.hasNext(); ) {
								CryptoCacheCipherEntry entry = it2.next();
								if (entry.getLastUsageTimestamp().before(removeEntriesBeforeThisTimestamp)) {
									it2.remove();
									++removedEntryCounter;
								}
								else
									++totalEntryCounter;
							}

							if (entries.isEmpty()) {
								it1.remove();
								++removedListCounter;
							}
							else
								++totalListCounter;
						}
					}
				}
			}
		}
		logger.debug("removeExpiredEntries: Removed {} instances of CryptoCacheCipherEntry ({} left).", removedEntryCounter, totalEntryCounter);
		logger.debug("removeExpiredEntries: Removed {} instances of empty List<CryptoCacheCipherEntry> ({} non-empty lists left).", removedListCounter, totalListCounter);
	}

	/**
	 * <p>
	 * Persistence property to control when the timer for cleaning up expired {@link CryptoCache}-entries is called. The
	 * value configured here is a period in milliseconds, i.e. the timer will be triggered every X ms (roughly).
	 * </p><p>
	 * If this persistence property is not present (or not a valid number), the default is 60000 (1 minute), which means
	 * the timer will wake up once a minute and call {@link #removeExpiredEntries(boolean)} with <code>force = true</code>.
	 * </p>
	 */
	public static final String PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_PERIOD = "cumulus4j.CryptoCache.cleanupTimer.period";

	/**
	 * <p>
	 * Persistence property to control whether the timer for cleaning up expired {@link CryptoCache}-entries is enabled. The
	 * value configured here can be either <code>true</code> or <code>false</code>.
	 * </p><p>
	 * If this persistence property is not present (or not a valid number), the default is <code>true</code>, which means the
	 * timer is enabled and will periodically call {@link #removeExpiredEntries(boolean)} with <code>force = true</code>.
	 * </p><p>
	 * If this persistence property is set to <code>false</code>, the timer is deactivated and cleanup happens only synchronously
	 * when one of the release-methods is called; periodically - not every time a method is called. The period is in this
	 * case the same as for the timer, i.e. configurable via {@link #PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_PERIOD}.
	 * </p>
	 */
	public static final String PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_ENABLED = "cumulus4j.CryptoCache.cleanupTimer.enabled";

	private long cleanupTimerPeriod = Long.MIN_VALUE;

	private Boolean cleanupTimerEnabled = null;

	/**
	 * <p>
	 * Persistence property to control after which time an unused entry expires.
	 * </p><p>
	 * Entries that are unused for the configured time in milliseconds are considered expired and
	 * either periodically removed by a timer (see property {@value #PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_PERIOD})
	 * or periodically removed synchronously during a call to one of the release-methods.
	 * </p><p>
	 * If this property is not present (or not a valid number), the default value is 1800000 (30 minutes).
	 * </p>
	 */
	public static final String PROPERTY_CRYPTO_CACHE_ENTRY_EXPIRY_AGE = "cumulus4j.CryptoCache.entryExpiryAge";

	private long cryptoCacheEntryExpiryAge = Long.MIN_VALUE;

	/**
	 * <p>
	 * Get the period in which expired entries are searched and closed.
	 * </p>
	 * <p>
	 * This value can be configured using the persistence property {@value #PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_PERIOD}.
	 * </p>
	 *
	 * @return the period in milliseconds.
	 * @see #PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_PERIOD
	 * @see #PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_ENABLED
	 */
	protected long getCleanupTimerPeriod()
	{
		long val = cleanupTimerPeriod;
		if (val == Long.MIN_VALUE) {
			String propName = PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_PERIOD;
			String propVal = (String) cryptoManager.getCryptoManagerRegistry().getNucleusContext().getPersistenceConfiguration().getProperty(propName);
			propVal = propVal == null ? null : propVal.trim();
			if (propVal != null && !propVal.isEmpty()) {
				try {
					val = Long.parseLong(propVal);
					if (val <= 0) {
						logger.warn("Persistence property '{}' is set to '{}', which is an ILLEGAL value (<= 0). Falling back to default value.", propName, propVal);
						val = Long.MIN_VALUE;
					}
					else
						logger.info("Persistence property '{}' is set to {} ms.", propName, val);
				} catch (NumberFormatException x) {
					logger.warn("Persistence property '{}' is set to '{}', which is an ILLEGAL value (no valid number). Falling back to default value.", propName, propVal);
				}
			}

			if (val == Long.MIN_VALUE) {
				val = 60000L;
				logger.info("Persistence property '{}' is not set. Using default value {}.", propName, val);
			}

			cleanupTimerPeriod = val;
		}
		return val;
	}

	/**
	 * <p>
	 * Get the enabled status of the timer used to cleanup.
	 * </p>
	 * <p>
	 * This value can be configured using the persistence property {@value #PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_ENABLED}.
	 * </p>
	 *
	 * @return the enabled status.
	 * @see #PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_PERIOD
	 * @see #PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_ENABLED
	 */
	protected boolean getCleanupTimerEnabled()
	{
		Boolean val = cleanupTimerEnabled;
		if (val == null) {
			String propName = PROPERTY_CRYPTO_CACHE_CLEANUP_TIMER_ENABLED;
			String propVal = (String) cryptoManager.getCryptoManagerRegistry().getNucleusContext().getPersistenceConfiguration().getProperty(propName);
			propVal = propVal == null ? null : propVal.trim();
			if (propVal != null && !propVal.isEmpty()) {
				if (propVal.equalsIgnoreCase(Boolean.TRUE.toString()))
					val = Boolean.TRUE;
				else if (propVal.equalsIgnoreCase(Boolean.FALSE.toString()))
					val = Boolean.FALSE;

				if (val == null)
					logger.warn("getCryptoCacheCleanupTimerEnabled: Property '{}' is set to '{}', which is an ILLEGAL value. Falling back to default value.", propName, propVal);
				else
					logger.info("getCryptoCacheCleanupTimerEnabled: Property '{}' is set to '{}'.", propName, val);
			}

			if (val == null) {
				val = Boolean.TRUE;
				logger.info("getCryptoCacheCleanupTimerEnabled: Property '{}' is not set. Using default value {}.", propName, val);
			}

			cleanupTimerEnabled = val;
		}
		return val;
	}

	/**
	 * <p>
	 * Get the age after which an unused entry expires.
	 * </p>
	 * <p>
	 * An entry expires when its lastUsageTimestamp
	 * is longer in the past than this expiry age. Note, that the entry might be kept longer, because a
	 * timer checks {@link #getCryptoCacheEntryExpiryTimerPeriod() periodically} for expired entries.
	 * </p>
	 *
	 * @return the expiry age (of non-usage-time) in milliseconds, after which an entry should be expired (and thus removed).
	 */
	protected long getCryptoCacheEntryExpiryAge()
	{
		long val = cryptoCacheEntryExpiryAge;
		if (val == Long.MIN_VALUE) {
			String propName = PROPERTY_CRYPTO_CACHE_ENTRY_EXPIRY_AGE;

			CryptoManagerRegistry cryptoManagerRegistry = cryptoManager.getCryptoManagerRegistry();
			if (cryptoManagerRegistry == null)
				throw new IllegalStateException("cryptoManager.getCryptoManagerRegistry() returned null!");

			NucleusContext nucleusContext = cryptoManagerRegistry.getNucleusContext();
			if (nucleusContext == null)
				throw new IllegalStateException("cryptoManagerRegistry.getNucleusContext() returned null!");

			PersistenceConfiguration persistenceConfiguration = nucleusContext.getPersistenceConfiguration();
			if (persistenceConfiguration == null)
				throw new IllegalStateException("nucleusContext.getPersistenceConfiguration() returned null!");

			String propVal = (String) persistenceConfiguration.getProperty(propName);
			// TODO Fix NPE! Just had a NullPointerException in the above line:
//			22:48:39,028 ERROR [Timer-3][CryptoCache$CleanupTask] run: java.lang.NullPointerException
//			java.lang.NullPointerException
//			        at org.cumulus4j.store.crypto.keymanager.CryptoCache.getCryptoCacheEntryExpiryAge(CryptoCache.java:950)
//			        at org.cumulus4j.store.crypto.keymanager.CryptoCache.removeExpiredEntries(CryptoCache.java:686)
//			        at org.cumulus4j.store.crypto.keymanager.CryptoCache.access$000(CryptoCache.java:56)
//			        at org.cumulus4j.store.crypto.keymanager.CryptoCache$CleanupTask.run(CryptoCache.java:615)
//			        at java.util.TimerThread.mainLoop(Timer.java:512)
//			        at java.util.TimerThread.run(Timer.java:462)
			// Need to check what exactly is null and if that is allowed or there is another problem.
			propVal = propVal == null ? null : propVal.trim();
			if (propVal != null && !propVal.isEmpty()) {
				try {
					val = Long.parseLong(propVal);
					logger.info("getCryptoCacheEntryExpiryAgeMSec: Property '{}' is set to {} ms.", propName, val);
				} catch (NumberFormatException x) {
					logger.warn("getCryptoCacheEntryExpiryAgeMSec: Property '{}' is set to '{}', which is an ILLEGAL value (no valid number). Falling back to default value.", propName, propVal);
				}
			}

			if (val == Long.MIN_VALUE) {
				val =  30L * 60000L;
				logger.info("getCryptoCacheEntryExpiryAgeMSec: Property '{}' is not set. Using default value {}.", propName, val);
			}

			cryptoCacheEntryExpiryAge = val;
		}
		return val;
	}
}
