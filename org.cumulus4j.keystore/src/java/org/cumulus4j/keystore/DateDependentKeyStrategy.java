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
package org.cumulus4j.keystore;

import java.io.IOException;
import java.util.Date;
import java.util.List;

import org.cumulus4j.keystore.prop.Long2LongSortedMapProperty;
import org.cumulus4j.keystore.prop.Property;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Key management strategy determining the currently active encryption key by the current time.
 * </p><p>
 * See <a href="../../../documentation/date-dependent-key-strategy.html">Date-dependent key-strategy</a> for further
 * details.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class DateDependentKeyStrategy
{
	private static final Logger logger = LoggerFactory.getLogger(DateDependentKeyStrategy.class);

	private KeyStore keyStore;

	/**
	 * Name of the {@link Property} where the key-strategy's timestamp-to-key-map is stored.
	 * The property is of type {@link Long2LongSortedMapProperty}.
	 */
	public static final String PROPERTY_ACTIVE_FROM_TIMESTAMP_2_KEY_ID = "DateDependentKeyStrategy.activeFromTimestamp2KeyID";

	/**
	 * Create a new instance for the given {@link KeyStore}.
	 * @param keyStore the <code>KeyStore</code> to work with. Must not be <code>null</code>.
	 */
	public DateDependentKeyStrategy(KeyStore keyStore)
	{
		if (keyStore == null)
			throw new IllegalArgumentException("keyStore == null");

		this.keyStore = keyStore;
	}

	/**
	 * Get the {@link KeyStore} that was passed to {@link #DateDependentKeyStrategy(KeyStore)}.
	 * @return the <code>KeyStore</code> this strategy instance works with. Never <code>null</code>.
	 */
	public KeyStore getKeyStore() {
		return keyStore;
	}

	/**
	 * <p>
	 * Initialise an {@link KeyStore#isEmpty() empty} <code>KeyStore</code>.
	 * </p><p>
	 * This initialisation consists of creating a user and a few (thousand) keys. How many keys,
	 * depends on the parameters <code>keyActivityPeriodMSec</code> and <code>keyStorePeriodMSec</code>.
	 * The keys are added to a {@link Long2LongSortedMapProperty} (i.e. a <code>Map</code>) with the
	 * key being the "from-timestamp" and the value being the key-ID. The "from-timestamp" is the time
	 * (including) from which on the key will be used as "active encryption key". The "active encryption
	 * key" is the key, that will be used for encryption in the app-server at a certain moment in time.
	 * </p>
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

//	/**
//	 * Get the timestamp till when the active key will be valid (excluding). This
//	 * is a convenience method delegating to {@link #getActiveKeyValidUntilExcl(String, char[], Date)}
//	 * with the argument <code>timestamp</code> being <code>null</code>.
//	 *
//	 * @param authUserName the authenticated user authorizing this action.
//	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
//	 * @return the timestamp at which the current active key will stop being active.
//	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
//	 * is not correct for the given <code>authUserName</code>.
//	 */
//	public Date getActiveKeyValidUntilExcl(String authUserName, char[] authPassword) throws AuthenticationException
//	{
//		return getActiveKeyValidUntilExcl(authUserName, authPassword, null);
//	}

//	/**
//	 * Get the timestamp till when the active key will be valid (excluding). The active key is
//	 * determined based on the given <code>timestamp</code> (which can be <code>null</code> to mean 'now').
//	 *
//	 * @param authUserName the authenticated user authorizing this action.
//	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
//	 * @param timestamp the timestamp specifying the time at which the queried key is / was / will be active.
//	 * Can be <code>null</code>, which is interpreted as NOW.
//	 * @return the timestamp at which the given key is valid. This is always after the given timestamp (or now, if <code>timestamp == null</code>),
//	 * even if there is no key with this validity, because the key-store is extrapolated if necessary (in an eternal cycle).
//	 * <p>
//	 * For example, if the key-store was generated with daily (24 h) key-rotation and keys for the time range from
//	 * 2011-01-01 00:00 [including] until 2011-04-01 00:00 [excluding]
//	 * and the given timestamp is 2011-05-01 23:45, the active key will be exactly the same as it was 2011-02-01 23:45. Though
//	 * this key originally was valid only till 2011-02-02 00:00 [excluding], the result of this method would now be 2011-05-02 00:00.
//	 * </p>
//	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
//	 * is not correct for the given <code>authUserName</code>.
//	 */
//	public Date getActiveKeyValidUntilExcl(String authUserName, char[] authPassword, Date timestamp) throws AuthenticationException
//	{
//		return new Date(determineActiveKeyIDAndValidFromAndValidTo(authUserName, authPassword, timestamp)[2]);
//	}

//	/**
//	 * Get the currently active key's ID. This
//	 * is a convenience method delegating to {@link #getActiveKeyID(String, char[], Date)}
//	 * with the argument <code>timestamp</code> being <code>null</code>.
//	 *
//	 * @param authUserName the authenticated user authorizing this action.
//	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
//	 * @return
//	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
//	 * is not correct for the given <code>authUserName</code>.
//	 */
//	public long getActiveKeyID(String authUserName, char[] authPassword)
//	throws AuthenticationException
//	{
//		return getActiveKeyID(authUserName, authPassword, null);
//	}

	/**
	 * <p>
	 * Get the details of the key which is / was / will be active at the given <code>timestamp</code>.
	 * </p>
	 * @param authUserName the authenticated user authorizing this action.
	 * @param authPassword the password for authenticating the user specified by <code>authUserName</code>.
	 * @param timestamp the timestamp at which the active key should be determined. If <code>null</code>, NOW (<code>new Date()</code>) is assumed.
	 * @return the active key at the given <code>timestamp</code>.
	 * @throws AuthenticationException if the specified <code>authUserName</code> does not exist or the specified <code>authPassword</code>
	 * is not correct for the given <code>authUserName</code>.
	 */
	public ActiveKey getActiveKey(String authUserName, char[] authPassword, Date timestamp)
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

		Long currentActiveUntilTimestamp = activeFromTimestamp2KeyIDMapProperty.getValue().tailMap(timestampMSec + 1L).firstKey();

		long diff = timestamp.getTime() - timestampMSec;

		return new ActiveKey(
				keyID,
				new Date(currentActiveFromTimestamp + diff),
				new Date(currentActiveUntilTimestamp + diff)
		);
	}

	/**
	 * Descriptor of the active key.
	 *
	 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
	 */
	public static final class ActiveKey
	{
		private long keyID;
		private Date activeFromIncl;
		private Date activeToExcl;

		private ActiveKey(long keyID, Date activeFromIncl, Date activeToExcl)
		{
			this.keyID = keyID;
			this.activeFromIncl = activeFromIncl;
			this.activeToExcl = activeToExcl;
		}

		/**
		 * Get the key's identifier.
		 * @return the key-ID.
		 */
		public long getKeyID() {
			return keyID;
		}
		/**
		 * <p>
		 * Get the timestamp from which on the key is active (including).
		 * </p>
		 * <p>
		 * This timestamp is extrapolated (if necessary) according to the timestamp given to
		 * {@link DateDependentKeyStrategy#getActiveKey(String, char[], Date)}. Please see
		 * the documentation of {@link #getActiveToExcl()} for more details about this extrapolation.
		 * </p>
		 * @return the timestamp from which on the key is active (including).
		 */
		public Date getActiveFromIncl() {
			return activeFromIncl;
		}

		/**
		 * <p>
		 * Get the timestamp until which the key is active (excluding).
		 * </p>
		 * <p>
		 * This is always after (never before, never equal to) the timestamp given
		 * to {@link DateDependentKeyStrategy#getActiveKey(String, char[], Date)},
		 * even if there is no key with this validity in the key-store, because the key-store is extrapolated if necessary
		 * (in an eternal cycle).
		 * </p>
		 * <p>
		 * For example, if the key-store was generated with daily (24 h) key-rotation and keys for the time range from
		 * 2011-01-01 00:00 [including] until 2011-04-01 00:00 [excluding]
		 * and the given timestamp is 2011-05-01 23:45, the active key will be exactly the same as it was 2011-02-01 23:45. Though
		 * this key originally was valid only till 2011-02-02 00:00 [excluding], the result of this method would now be 2011-05-02 00:00.
		 * </p>
		 * @return the timestamp until which the key is active (excluding).
		 */
		public Date getActiveToExcl() {
			return activeToExcl;
		}
	}
}
