package org.cumulus4j.keystore;

import java.io.IOException;
import java.util.List;

import org.cumulus4j.keystore.prop.Long2LongSortedMapProperty;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class DateDependentKeyStrategy
{
	private KeyStore keyStore;

	public static final String PROPERTY_VALID_FROM_TIMESTAMP_2_KEY_ID = "DateDependentKeyStrategy.validFromTimestamp2KeyID";

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
	 *
	 * @param userName the initial user to be created.
	 * @param password the password for the initial user.
	 * @param keyActivityPeriodMSec how long (in millisec) should each key be valid. If &lt; 1, the
	 * default value of 24 hours (= 86400000 msec) will be used.
	 * @param keyStorePeriodMSec how long should the key store have fresh, unused keys. This number
	 * divided by the <code>keyActivityPeriodMSec</code> determines, how many keys must be generated.
	 * If &lt; 1, the default value of 50 years (50 * 365 days - ignoring leap years!) will be used.
	 * @throws IOException if writing to the key-store-file failed.
	 */
	public void init(String userName, char[] password, long keyActivityPeriodMSec, long keyStorePeriodMSec) throws IOException
	{
		if (!keyStore.isEmpty())
			throw new IllegalStateException("Key store is not empty! Cannot initialise!");

		if (keyActivityPeriodMSec < 1)
			keyActivityPeriodMSec = 24L * 3600L * 1000L;

		if (keyStorePeriodMSec < 1)
			keyStorePeriodMSec = 50L * 365L * 24L * 3600L * 1000L;

		long numberOfKeysToGenerate = 1 + keyStorePeriodMSec / keyActivityPeriodMSec;
		if (numberOfKeysToGenerate > Integer.MAX_VALUE)
			throw new IllegalArgumentException("Calculated numberOfKeysToGenerate=" + numberOfKeysToGenerate + " is out of range! Maximum allowed value is " + Integer.MAX_VALUE + "! Reduce keyStorePeriodMSec or increase keyActivityPeriodMSec!");

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

			Long2LongSortedMapProperty validFromTimestamp2KeyIDMapProperty = keyStore.getProperty(authUserName, authPassword, Long2LongSortedMapProperty.class, PROPERTY_VALID_FROM_TIMESTAMP_2_KEY_ID);


		} catch (AuthenticationException e) { // We just created this user - if that exception really occurs here, it's definitely a RuntimeException.
			throw new RuntimeException(e);
		}
	}


}
