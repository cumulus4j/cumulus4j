package org.cumulus4j.keystore;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.cumulus4j.keystore.prop.Long2LongSortedMapProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class DateDependentKeyStrategy
{
	private static final Logger logger = LoggerFactory.getLogger(DateDependentKeyStrategy.class);

	private KeyStore keyStore;

	public static final String PROPERTY_ACTIVE_FROM_TIMESTAMP_2_KEY_ID = "DateDependentKeyStrategy.activeFromTimestamp2KeyID";

	public DateDependentKeyStrategy(KeyStore keyStore)
	{
		if (keyStore == null)
			throw new IllegalArgumentException("keyStore == null");

		this.keyStore = keyStore;
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	/**
	 * Initialise an {@link KeyStore#isEmpty() empty} <code>KeyStore</code>
	 *
	 * @param userName the initial user to be created.
	 * @param password the password for the initial user.
	 * @param keyActivityPeriodMSec how long (in millisec) should each key be valid. If &lt; 1, the
	 * default value of 24 hours (= 86400000 msec) will be used.
	 * @param keyStorePeriodMSec how long should the key store have fresh, unused keys. This number
	 * divided by the <code>keyActivityPeriodMSec</code> determines, how many keys must be generated.
	 * If &lt; 1, the default value of 50 years (50 * 365 days - ignoring leap years!) will be used.
	 * @throws IOException if writing to the key-store-file failed.
	 * @throws KeyStoreNotEmptyException if the <code>KeyStore</code> is not {@link KeyStore#isEmpty() empty}.
	 */
	public void init(String userName, char[] password, long keyActivityPeriodMSec, long keyStorePeriodMSec)
	throws IOException, KeyStoreNotEmptyException
	{
		if (!keyStore.isEmpty())
			throw new IllegalStateException("Key store is not empty! Cannot initialise!");

		if (keyActivityPeriodMSec < 1)
			keyActivityPeriodMSec = 24L * 3600L * 1000L;

		if (keyStorePeriodMSec < 1)
			keyStorePeriodMSec = 50L * 365L * 24L * 3600L * 1000L;

		long numberOfKeysToGenerate = 1 + keyStorePeriodMSec / keyActivityPeriodMSec;
		logger.debug("init: Calculated numberOfKeysToGenerate={}", numberOfKeysToGenerate);

		if (numberOfKeysToGenerate > Integer.MAX_VALUE)
			throw new KeyStoreNotEmptyException("Calculated numberOfKeysToGenerate=" + numberOfKeysToGenerate + " is out of range! Maximum allowed value is " + Integer.MAX_VALUE + "! Reduce keyStorePeriodMSec or increase keyActivityPeriodMSec!");

		try {
			keyStore.createUser(null, null, userName, password);
		} catch (AuthenticationException e) {
			throw new RuntimeException(e);
		} catch (UserAlreadyExistsException e) {
			throw new RuntimeException(e);
		}

		String authUserName = userName;
		char[] authPassword = password;

		try {
			List<GeneratedKey> generatedKeys = keyStore.generateKeys(authUserName, authPassword, (int)numberOfKeysToGenerate);
			long activeFromTimestamp = System.currentTimeMillis();
			Long2LongSortedMapProperty activeFromTimestamp2KeyIDMapProperty = keyStore.getProperty(authUserName, authPassword, Long2LongSortedMapProperty.class, PROPERTY_ACTIVE_FROM_TIMESTAMP_2_KEY_ID);
			for (GeneratedKey generatedKey : generatedKeys) {
				activeFromTimestamp2KeyIDMapProperty.getValue().put(activeFromTimestamp, generatedKey.getKeyID());
				// calculate next validFromTimestamp
				activeFromTimestamp += keyActivityPeriodMSec;
			}

			// Put -1 as END marker.
			activeFromTimestamp2KeyIDMapProperty.getValue().put(activeFromTimestamp, -1L);

			// Write the property.
			keyStore.setProperty(authUserName, authPassword, activeFromTimestamp2KeyIDMapProperty);
		} catch (AuthenticationException e) { // We just created this user - if that exception really occurs here, it's definitely a RuntimeException.
			throw new RuntimeException(e);
		}
	}

	public long getActiveKeyID(String authUserName, char[] authPassword)
	throws AuthenticationException
	{
		return getActiveKeyID(authUserName, authPassword, null);
	}

	public long getActiveKeyID(String authUserName, char[] authPassword, Date timestamp)
	throws AuthenticationException
	{
		if (timestamp == null)
			timestamp = new Date();

		Long2LongSortedMapProperty activeFromTimestamp2KeyIDMapProperty = keyStore.getProperty(authUserName, authPassword, Long2LongSortedMapProperty.class, PROPERTY_ACTIVE_FROM_TIMESTAMP_2_KEY_ID);
		if (activeFromTimestamp2KeyIDMapProperty.getValue().isEmpty())
			throw new IllegalStateException("There is no property named '" + PROPERTY_ACTIVE_FROM_TIMESTAMP_2_KEY_ID + "'! Obviously the key-store was not initalised for this strategy!");

		if (activeFromTimestamp2KeyIDMapProperty.getValue().get(activeFromTimestamp2KeyIDMapProperty.getValue().lastKey()) != -1L)
			throw new IllegalStateException("Expected last entry to be the END marker, but it is not!");

		long timestampMSec = timestamp.getTime();
		if (timestampMSec < activeFromTimestamp2KeyIDMapProperty.getValue().firstKey()) {
			logger.warn("getActiveKeyID: timestamp is out of range (before). Will reuse another key via cyclic extrapolation.");
			while (timestampMSec < activeFromTimestamp2KeyIDMapProperty.getValue().firstKey())
				timestampMSec += activeFromTimestamp2KeyIDMapProperty.getValue().lastKey() - activeFromTimestamp2KeyIDMapProperty.getValue().firstKey();
		}

		if (timestampMSec >= activeFromTimestamp2KeyIDMapProperty.getValue().lastKey()) {
			logger.warn("getActiveKeyID: timestamp is out of range (after). Will reuse another key via cyclic extrapolation.");
			while (timestampMSec >= activeFromTimestamp2KeyIDMapProperty.getValue().lastKey()) {
				timestampMSec -= activeFromTimestamp2KeyIDMapProperty.getValue().lastKey() - activeFromTimestamp2KeyIDMapProperty.getValue().firstKey();
			}
		}

		Long currentActiveFromTimestamp = activeFromTimestamp2KeyIDMapProperty.getValue().headMap(timestampMSec + 1L).lastKey(); // We add 1, because our contract is INCLUSIVE while headMap is EXCLUSIVE.
		Long keyID = activeFromTimestamp2KeyIDMapProperty.getValue().get(currentActiveFromTimestamp);
		if (keyID == null)
			throw new IllegalStateException("keyID == null for currentActiveFromTimestamp == " + currentActiveFromTimestamp);

		if (keyID < 0)
			throw new IllegalStateException("keyID < 0");

		return keyID;
	}
}
