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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
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

	private Map<CipherOperationMode, Map<String, Map<Long, List<CryptoCacheCipherEntry>>>> opmode2encryptionAlgorithm2keyID2cipherEntries = Collections.synchronizedMap(
		new HashMap<CipherOperationMode, Map<String,Map<Long,List<CryptoCacheCipherEntry>>>>()
	);

	private KeyManagerCryptoManager cryptoManager;

	public CryptoCache(KeyManagerCryptoManager cryptoManager)
	{
		if (cryptoManager == null)
			throw new IllegalArgumentException("cryptoManager == null");

		this.cryptoManager = cryptoManager;
	}

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

	protected CryptoCacheKeyEntry setKeyData(long keyID, byte[] keyData)
	{
		CryptoCacheKeyEntry entry = new CryptoCacheKeyEntry(keyID, keyData);
		keyID2key.put(keyID, entry);
		return entry;
	}

	public CryptoCacheCipherEntry acquireDecrypter(String encryptionAlgorithm, long keyID, byte[] iv)
	{
		return acquireDecrypter(encryptionAlgorithm, keyID, null, iv);
	}

	public CryptoCacheCipherEntry acquireDecrypter(String encryptionAlgorithm, long keyID, byte[] keyData, byte[] iv)
	{
		return acquireCipherEntry(CipherOperationMode.DECRYPT, encryptionAlgorithm, keyID, keyData, iv);
	}

	public CryptoCacheCipherEntry acquireEncrypter(String encryptionAlgorithm, long keyID)
	{
		return acquireEncrypter(encryptionAlgorithm, keyID, null);
	}

	public CryptoCacheCipherEntry acquireEncrypter(String encryptionAlgorithm, long keyID, byte[] keyData)
	{
		return acquireCipherEntry(CipherOperationMode.ENCRYPT, encryptionAlgorithm, keyID, keyData, null);
	}

	private CryptoCacheCipherEntry acquireCipherEntry(
			CipherOperationMode opmode, String encryptionAlgorithm, long keyID, byte[] keyData, byte[] iv
	)
	{
		Map<String, Map<Long, List<CryptoCacheCipherEntry>>> encryptionAlgorithm2keyID2encrypters =
			opmode2encryptionAlgorithm2keyID2cipherEntries.get(opmode);

		if (encryptionAlgorithm2keyID2encrypters != null) {
			Map<Long, List<CryptoCacheCipherEntry>> keyID2Encrypters = encryptionAlgorithm2keyID2encrypters.get(encryptionAlgorithm);
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
			cipher = CryptoRegistry.sharedInstance().createCipher(encryptionAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		} catch (NoSuchPaddingException e) {
			throw new RuntimeException(e);
		}

		CryptoCacheCipherEntry entry = new CryptoCacheCipherEntry(
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

	public void releaseCipherEntry(CryptoCacheCipherEntry cipherEntry)
	{
		initTimerTaskOrRemoveExpiredEntriesPeriodically();

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

		Map<String, Map<Long, List<CryptoCacheCipherEntry>>> encryptionAlgorithm2keyID2cipherEntries;
		synchronized (opmode2encryptionAlgorithm2keyID2cipherEntries) {
			encryptionAlgorithm2keyID2cipherEntries =
				opmode2encryptionAlgorithm2keyID2cipherEntries.get(cipherEntry.getCipher().getMode());

			if (encryptionAlgorithm2keyID2cipherEntries == null) {
				encryptionAlgorithm2keyID2cipherEntries = Collections.synchronizedMap(
						new HashMap<String, Map<Long,List<CryptoCacheCipherEntry>>>()
				);

				opmode2encryptionAlgorithm2keyID2cipherEntries.put(
						cipherEntry.getCipher().getMode(), encryptionAlgorithm2keyID2cipherEntries
				);
			}
		}

		Map<Long, List<CryptoCacheCipherEntry>> keyID2cipherEntries;
		synchronized (encryptionAlgorithm2keyID2cipherEntries) {
			keyID2cipherEntries = encryptionAlgorithm2keyID2cipherEntries.get(cipherEntry.getEncryptionAlgorithm());
			if (keyID2cipherEntries == null) {
				keyID2cipherEntries = Collections.synchronizedMap(new HashMap<Long, List<CryptoCacheCipherEntry>>());
				encryptionAlgorithm2keyID2cipherEntries.put(cipherEntry.getEncryptionAlgorithm(), keyID2cipherEntries);
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

	public void clear()
	{
		logger.trace("clear: entered");
		keyID2key.clear();
		opmode2encryptionAlgorithm2keyID2cipherEntries.clear();
	}

	private Map<String, CryptoCacheKeyEncryptionKeyEntry> keyEncryptionTransformation2keyEncryptionKey = Collections.synchronizedMap(
			new HashMap<String, CryptoCacheKeyEncryptionKeyEntry>()
	);

	private Map<String, List<CryptoCacheKeyDecrypterEntry>> keyEncryptionTransformation2keyDecryptors = Collections.synchronizedMap(
			new HashMap<String, List<CryptoCacheKeyDecrypterEntry>>()
	);

	protected long getKeyEncryptionKeyActivePeriodMSec()
	{
		return 3600L * 1000L;
	}

	protected CryptoCacheKeyEncryptionKeyEntry getKeyEncryptionKey(String keyEncryptionTransformation)
	{
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

	public CryptoCacheKeyDecrypterEntry acquireKeyDecryptor(String keyEncryptionTransformation)
	{
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
	}

	public void releaseKeyDecryptor(CryptoCacheKeyDecrypterEntry decryptorEntry)
	{
		initTimerTaskOrRemoveExpiredEntriesPeriodically();

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

	protected AsymmetricCipherKeyPairGenerator getAsymmetricCipherKeyPairGenerator(String keyEncryptionTransformation)
	{
		String algorithmName = CryptoRegistry.splitTransformation(keyEncryptionTransformation)[0];
		try {
			return CryptoRegistry.sharedInstance().createKeyPairGenerator(algorithmName, true);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}


	private static volatile Timer removeExpiredEntriesTimer = null;
	private static volatile boolean removeExpiredEntriesTimerInitialised = false;
	private volatile boolean removeExpiredEntriesTaskInitialised = false;

	private class RemoveExpiredEntriesTask extends TimerTask
	{
		private final Logger logger = LoggerFactory.getLogger(RemoveExpiredEntriesTask.class);

		private final long expiryTimerPeriodMSec;

		public RemoveExpiredEntriesTask(long expiryTimerPeriodMSec) {
			this.expiryTimerPeriodMSec = expiryTimerPeriodMSec;
		}

		@Override
		public void run() {
			logger.debug("run: entered");
			removeExpiredEntries(true);

			long currentPeriodMSec = getCryptoCacheEntryExpiryTimerPeriodMSec();
			if (currentPeriodMSec != expiryTimerPeriodMSec) {
				logger.info(
						"run: The expiryTimerPeriodMSec changed (oldValue={}, newValue={}). Re-scheduling this task.",
						expiryTimerPeriodMSec, currentPeriodMSec
				);
				this.cancel();

				removeExpiredEntriesTimer.schedule(new RemoveExpiredEntriesTask(currentPeriodMSec), currentPeriodMSec, currentPeriodMSec);
			}
		}
	};

	private final void initTimerTaskOrRemoveExpiredEntriesPeriodically()
	{
		if (!removeExpiredEntriesTimerInitialised) {
			synchronized (AbstractCryptoManager.class) {
				if (!removeExpiredEntriesTimerInitialised) {
					if (getCryptoCacheEntryExpiryTimerPeriodMSec() > 0)
						removeExpiredEntriesTimer = new Timer();

					removeExpiredEntriesTimerInitialised = true;
				}
			}
		}

		if (!removeExpiredEntriesTaskInitialised) {
			synchronized (this) {
				if (!removeExpiredEntriesTaskInitialised) {
					if (removeExpiredEntriesTimer != null) {
						long periodMSec = getCryptoCacheEntryExpiryTimerPeriodMSec();
						removeExpiredEntriesTimer.schedule(new RemoveExpiredEntriesTask(periodMSec), periodMSec, periodMSec);
					}
					removeExpiredEntriesTaskInitialised = true;
				}
			}
		}

		if (removeExpiredEntriesTimer == null) {
			logger.trace("initTimerTaskOrRemoveExpiredEntriesPeriodically: No timer enabled => calling removeExpiredEntries(false) now.");
			removeExpiredEntries(false);
		}
	}

	private Date lastRemoveExpiredCacheEntriesTimestamp = null;

	private void removeExpiredEntries(boolean force)
	{
		synchronized (this) {
			if (
					!force && (
							lastRemoveExpiredCacheEntriesTimestamp != null && lastRemoveExpiredCacheEntriesTimestamp.after(new Date(System.currentTimeMillis() - getCryptoCacheEntryExpiryAgeMSec()))
					)
			)
			{
				logger.trace("removeExpiredEntries: force == false and period not yet elapsed. Skipping.");
				return;
			}

			lastRemoveExpiredCacheEntriesTimestamp = new Date();
		}

		Date removeEntriesBeforeThisTimestamp = new Date(
				System.currentTimeMillis() - getCryptoCacheEntryExpiryAgeMSec()
		);

		int removedEntryCounter = 0;
		synchronized (keyEncryptionTransformation2keyEncryptionKey) {
			for (Iterator<Map.Entry<String, CryptoCacheKeyEncryptionKeyEntry>> it1 = keyEncryptionTransformation2keyEncryptionKey.entrySet().iterator(); it1.hasNext(); ) {
				Map.Entry<String, CryptoCacheKeyEncryptionKeyEntry> me1 = it1.next();
				if (me1.getValue().isExpired()) {
					it1.remove();
					++removedEntryCounter;
				}
			}
		}
		logger.debug("removeExpiredEntries: Removed {} instances of CryptoCacheKeyEncryptionKeyEntry.", removedEntryCounter);


		// There are not many keyEncryptionTransformations (usually only ONE!), hence copying this is fine and very fast.
		String[] keyEncryptionTransformations;
		synchronized (keyEncryptionTransformation2keyDecryptors) {
			keyEncryptionTransformations = keyEncryptionTransformation2keyDecryptors.keySet().toArray(
					new String[keyEncryptionTransformation2keyDecryptors.size()]
			);
		}

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
				}
			}
		}
		logger.debug("removeExpiredEntries: Removed {} instances of CryptoCacheKeyDecrypterEntry.", removedEntryCounter);


		removedEntryCounter = 0;
		synchronized (keyID2key) {
			for (Iterator<Map.Entry<Long, CryptoCacheKeyEntry>> it1 = keyID2key.entrySet().iterator(); it1.hasNext(); ) {
				Map.Entry<Long, CryptoCacheKeyEntry> me1 = it1.next();

				if (me1.getValue().getLastUsageTimestamp().before(removeEntriesBeforeThisTimestamp)) {
					it1.remove();
					++removedEntryCounter;
				}
			}
		}
		logger.debug("removeExpiredEntries: Removed {} instances of CryptoCacheKeyEntry.", removedEntryCounter);


		removedEntryCounter = 0;
		int removedListCounter = 0;
		for (CipherOperationMode opmode : CipherOperationMode.values()) {
			Map<String, Map<Long, List<CryptoCacheCipherEntry>>> encryptionAlgorithm2keyID2cipherEntries = opmode2encryptionAlgorithm2keyID2cipherEntries.get(opmode);
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
					for (Iterator<Entry<Long, List<CryptoCacheCipherEntry>>> it1 = keyID2cipherEntries.entrySet().iterator(); it1.hasNext(); ) {
						Entry<Long, List<CryptoCacheCipherEntry>> me1 = it1.next();
						List<CryptoCacheCipherEntry> entries = me1.getValue();
						synchronized (entries) {
							for (Iterator<CryptoCacheCipherEntry> it2 = entries.iterator(); it2.hasNext(); ) {
								CryptoCacheCipherEntry entry = it2.next();
								if (entry.getLastUsageTimestamp().before(removeEntriesBeforeThisTimestamp)) {
									it2.remove();
									++removedEntryCounter;
								}
							}

							if (entries.isEmpty()) {
								it1.remove();
								++removedListCounter;
							}
						}
					}
				}
			}
		}
		logger.debug("removeExpiredEntries: Removed {} instances of CryptoCacheCipherEntry.", removedEntryCounter);
		logger.debug("removeExpiredEntries: Removed {} instances of (empty) List<CryptoCacheCipherEntry>.", removedListCounter);
	}

	/**
	 * <p>
	 * Persistence property to control when the timer for cleaning up expired {@link CryptoCache}-entries is called. The
	 * value configured here is a period, i.e. the timer will be triggered every X ms (roughly).
	 * </p><p>
	 * If this persistence property is not present (or not a valid number), the default is 60000 (1 minute), which means
	 * the timer will wake up once a minute and call {@link #removeExpiredEntries(boolean)} with <code>force = true</code>.
	 * </p><p>
	 * If this persistence property is set to 0, the timer is deactivated and cleanup happens only synchronously
	 * when one of the release-methods is called (periodically - not every time a method is called).
	 * </p>
	 */
	public static final String PROPERTY_CRYPTO_CACHE_ENTRY_EXPIRY_TIMER_PERIOD_MSEC = "cumulus4j.cryptoCacheEntryExpiryTimerPeriodMSec";

	private long cryptoCacheEntryExpiryTimerPeriodMSec = Long.MIN_VALUE;

	/**
	 * <p>
	 * Persistence property to control after which time an unused {@link CryptoCache}-entry expires.
	 * </p><p>
	 * Entries that are unused for the configured time in milliseconds are considered expired and
	 * either periodically removed by a timer (see property {@value #PROPERTY_CRYPTO_CACHE_ENTRY_EXPIRY_TIMER_PERIOD_MSEC})
	 * or periodically removed synchronously during a call to one of the release-methods.
	 * </p><p>
	 * If this property is not present (or not a valid number), the default value is 1800000 (30 minutes).
	 * </p>
	 */
	public static final String PROPERTY_CRYPTO_CACHE_ENTRY_EXPIRY_AGE_MSEC = "cumulus4j.cryptoCacheEntryExpiryAgeMSec";

	private long cryptoCacheEntryExpiryAgeMSec = Long.MIN_VALUE;



	/**
	 * <p>
	 * Get the period in which expired entries are searched and closed.
	 * </p>
	 * <p>
	 * This value can be configured using the persistence property {@value #PROPERTY_CRYPTO_CACHE_ENTRY_EXPIRY_TIMER_PERIOD_MSEC}.
	 * A value of 0 means to deactivate the timer. In this case, only periodic cleanup during the release-methods
	 * occurs.
	 * </p>
	 *
	 * @return the period in milliseconds.
	 */
	protected long getCryptoCacheEntryExpiryTimerPeriodMSec()
	{
		long val = cryptoCacheEntryExpiryTimerPeriodMSec;
		if (val == Long.MIN_VALUE) {
			String propName = PROPERTY_CRYPTO_CACHE_ENTRY_EXPIRY_TIMER_PERIOD_MSEC;
			String propVal = (String) cryptoManager.getCryptoManagerRegistry().getNucleusContext().getPersistenceConfiguration().getProperty(propName);
			if (propVal != null && !propVal.trim().isEmpty()) {
				try {
					val = Long.parseLong(propVal.trim());
					logger.info("getCryptoCacheEntryExpiryTimerPeriodMSec: Property '{}' is set to {} ms.", propName, val);
				} catch (NumberFormatException x) {
					logger.warn("getCryptoCacheEntryExpiryTimerPeriodMSec: Property '{}' is set to '{}', which is an ILLEGAL value (no valid number). Falling back to default value.", propName, propVal);
				}
			}

			if (val == Long.MIN_VALUE) {
				val = 60000L;
				logger.info("getCryptoCacheEntryExpiryTimerPeriodMSec: Property '{}' is not set. Using default value {}.", propName, val);
			}

			cryptoCacheEntryExpiryTimerPeriodMSec = val;
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
	 * timer checks {@link #getCryptoCacheEntryExpiryTimerPeriodMSec() periodically} for expired entries.
	 * </p>
	 *
	 * @return the expiry age (of non-usage-time) in milliseconds, after which an entry should be expired (and thus removed).
	 */
	protected long getCryptoCacheEntryExpiryAgeMSec()
	{
		long val = cryptoCacheEntryExpiryAgeMSec;
		if (val == Long.MIN_VALUE) {
			String propName = PROPERTY_CRYPTO_CACHE_ENTRY_EXPIRY_AGE_MSEC;
			String propVal = (String) cryptoManager.getCryptoManagerRegistry().getNucleusContext().getPersistenceConfiguration().getProperty(propName);
			if (propVal != null && !propVal.trim().isEmpty()) {
				try {
					val = Long.parseLong(propVal.trim());
					logger.info("getCryptoCacheEntryExpiryAgeMSec: Property '{}' is set to {} ms.", propName, val);
				} catch (NumberFormatException x) {
					logger.warn("getCryptoCacheEntryExpiryAgeMSec: Property '{}' is set to '{}', which is an ILLEGAL value (no valid number). Falling back to default value.", propName, propVal);
				}
			}

			if (val == Long.MIN_VALUE) {
				val =  30L * 60000L;
				logger.info("getCryptoCacheEntryExpiryAgeMSec: Property '{}' is not set. Using default value {}.", propName, val);
			}

			cryptoCacheEntryExpiryAgeMSec = val;
		}
		return val;
	}
}
