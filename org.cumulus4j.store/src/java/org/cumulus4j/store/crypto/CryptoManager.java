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
package org.cumulus4j.store.crypto;

import org.cumulus4j.store.model.EncryptionCoordinateSet;
import org.datanucleus.NucleusContext;

/**
 * <p>
 * {@link CryptoManager}s allow the Cumulus4j-DataNucleus-plug-in to encrypt and decrypt data.
 * </p>
 * <p>
 * The primary purpose to make this feature pluggable is to provide different possibilities
 * of the communication between the Cumulus4j-backend and a key store. For example, one client
 * might prefer to manage the keys on the client while another client provides the coordinates
 * of a key server to the backend.
 * </p>
 * <p>
 * There is one shared instance of <code>CryptoManager</code> per {@link NucleusContext} and
 * {@link #getCryptoManagerID() cryptoManagerID}. Due to this, instances of <code>CryptoManager</code>
 * must be thread-safe!
 * </p>
 * <p>
 * A <code>CryptoManager</code> must not be instantiated directly, but instead obtained via
 * {@link CryptoManagerRegistry#getCryptoManager(String)}.
 * </p>
 * <p>
 * <b>Important:</b> It is strongly recommended to subclass {@link AbstractCryptoManager} instead of
 * directly implementing this interface!
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface CryptoManager
{
	/**
	 * <p>
	 * Property-name used to pass the {@link #getCryptoManagerID() cryptoManagerID} to the Cumulus4j-core.
	 * </p>
	 * <p>
	 * The property can either be set in the persistence-unit/persistence-properties-file for the
	 * <code>PersistenceManagerFactory</code>/<code>EntityManagerFactory</code> or it can be
	 * passed via
	 * {@link javax.jdo.PersistenceManager#setProperty(String, Object)} or
	 * {@link javax.persistence.EntityManager#setProperty(String, Object)}. If it is not set
	 * on the PM/EM level, the default-value set on the PMF/EMF level will be used.
	 * </p>
	 */
	static final String PROPERTY_CRYPTO_MANAGER_ID = "cumulus4j.cryptoManagerID";

	/**
	 * <p>
	 * Property to control the encryption algorithm that is used to encrypt data within the datastore. Both
	 * data and index are encrypted using this algorithm.
	 * </p>
	 * <p>
	 * By default (if the property {@value #PROPERTY_ENCRYPTION_ALGORITHM} is not specified),
	 * "Twofish/GCM/NoPadding" is used. For example, to switch to "AES/CFB/NoPadding", you'd have
	 * to specify "cumulus4j.encryptionAlgorithm=AES/CFB/NoPadding" in the persistence-unit/persistence-properties-file.
	 * </p>
	 * <p>
	 * See <a href="http://cumulus4j.org/documentation/supported-algorithms.html">this document</a>
	 * for further information about what values are supported.
	 * </p>
	 * <p>
	 * The encryption algorithm used during encryption is stored in the encryption-record's meta-data in order
	 * to use the correct algorithm during decryption, no matter what current encryption algorithm is configured.
	 * Therefore, you can safely change this setting at any time - it will affect future encryption
	 * operations, only.
	 * </p>
	 * <p>
	 * <b>Important:</b> The default MAC algorithm is "NONE", which is a very bad choice for most encryption algorithms!
	 * Therefore, you must change the MAC algorithm via the property {@value #PROPERTY_MAC_ALGORITHM}
	 * if you change the encryption algorithm!
	 * </p>
	 * <p>
	 * The property can be set in the persistence-unit/persistence-properties-file for the
	 * <code>PersistenceManagerFactory</code>/<code>EntityManagerFactory</code>.
	 * </p>
	 * @see #getEncryptionAlgorithm()
	 */
	static final String PROPERTY_ENCRYPTION_ALGORITHM = "cumulus4j.encryptionAlgorithm";

	/**
	 * <p>
	 * Property to control the <a href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a>
	 * algorithm that is used to protect the data within the key-store against manipulation.
	 * </p>
	 * <p>
	 * Whenever data is encrypted, this MAC algorithm is used to calculate a MAC over the original plain-text-data.
	 * The MAC is then stored together with the plain-text-data within the encrypted area.
	 * When data is decrypted, the MAC is calculated again over the decrypted plain-text-data and compared to the
	 * original MAC in order to make sure (1) that data was correctly decrypted [i.e. the key is correct] and
	 * (2) that the data in the datastore was not manipulated by an attacker.
	 * </p>
	 * <p>
	 * The MAC algorithm used during encryption is stored in the encryption-record's meta-data in order
	 * to use the correct algorithm during decryption, no matter what current MAC algorithm is configured.
	 * Therefore, you can safely change this setting at any time - it will affect future encryption
	 * operations, only.
	 * </p>
	 * <p>
	 * Some block cipher modes (e.g. <a href="http://en.wikipedia.org/wiki/Galois/Counter_Mode">GCM</a>) already include authentication
	 * and therefore no MAC is necessary. In this case, you can specify the MAC algorithm {@value #MAC_ALGORITHM_NONE}.
	 * </p>
	 * <p>
	 * <b>Important:</b> If you specify the MAC algorithm "NONE" and use an encryption algorithm without
	 * authentication, the key store will not be able to detect a wrong password and instead return
	 * corrupt data!!! Be VERY careful with the MAC algorithm "NONE"!!!
	 * </p>
	 * <p>
	 * The default value (used when this system property is not specified) is "NONE", because the default
	 * encryption algorithm is "Twofish/GCM/NoPadding", which (due to "GCM") does not require an additional
	 * MAC.
	 * </p>
	 * <p>
	 * The property can be set in the persistence-unit/persistence-properties-file for the
	 * <code>PersistenceManagerFactory</code>/<code>EntityManagerFactory</code>.
	 * </p>
	 * @see #getMacAlgorithm()
	 */
	static final String PROPERTY_MAC_ALGORITHM = "cumulus4j.macAlgorithm";

	/**
	 * <p>
	 * Constant for deactivating the <a href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a>.
	 * </p>
	 * <p>
	 * <b>Important: Deactivating the MAC is dangerous!</b> Choose this value only, if you are absolutely
	 * sure that your {@link #PROPERTY_ENCRYPTION_ALGORITHM encryption algorithm} already
	 * provides authentication - like <a href="http://en.wikipedia.org/wiki/Galois/Counter_Mode">GCM</a>
	 * does for example.
	 * </p>
	 * @see #PROPERTY_MAC_ALGORITHM
	 */
	static final String MAC_ALGORITHM_NONE = EncryptionCoordinateSet.MAC_ALGORITHM_NONE;

	/**
	 * Get the registry which manages this {@link CryptoManager}.
	 * This method should normally never return <code>null</code>, because
	 * the registry is {@link #setCryptoManagerRegistry(CryptoManagerRegistry) set} immediately
	 * after instantiation.
	 * @return the registry holding this {@link CryptoManager}.
	 * @see #setCryptoManagerRegistry(CryptoManagerRegistry)
	 */
	CryptoManagerRegistry getCryptoManagerRegistry();

	/**
	 * Set the registry which manages this {@link CryptoManager}.
	 * This method is called by the {@link CryptoManagerRegistry} whenever
	 * it creates a new instance of <code>CryptoManager</code>.
	 *
	 * @param cryptoManagerRegistry
	 * @see #getCryptoManagerRegistry()
	 */
	void setCryptoManagerRegistry(CryptoManagerRegistry cryptoManagerRegistry);

	/**
	 * <p>
	 * Set the <code>cryptoManagerID</code> of this instance.
	 * </p>
	 * <p>
	 * This method is called with the value configured in the <code>plugin.xml</code>
	 * directly after instantiating the <code>CryptoManager</code>.
	 * </p>
	 * <p>
	 * <b>You must never directly call this method! It is not an API method!</b>
	 * </p>
	 *
	 * @param cryptoManagerID the identifier to set.
	 * @see #getCryptoManagerID()
	 */
	void setCryptoManagerID(String cryptoManagerID);

	/**
	 * <p>
	 * Get the <code>cryptoManagerID</code> of this instance.
	 * </p>
	 * <p>
	 * The <code>cryptoManagerID</code> is configured in the <code>plugin.xml</code> when registering an extension
	 * to the extension-point <code>org.cumulus4j.api.cryptoManager</code>. It is then used by the client to
	 * specify which method of key-exchange (or key-management in general) and encryption/decryption is desired.
	 * This is done by setting the property {@link #PROPERTY_CRYPTO_MANAGER_ID}.
	 * </p>
	 * <p>
	 * This method is thread-safe.
	 * </p>
	 *
	 * @return the <code>cryptoManagerID</code> of this instance.
	 */
	String getCryptoManagerID();

	/**
	 * <p>
	 * Get the {@link CryptoSession} identified by the given <code>cryptoSessionID</code>.
	 * </p>
	 * <p>
	 * Usually, every client opens one crypto-session. How exactly this happens, is highly dependent
	 * on the <code>CryptoManager</code> and <code>CryptoSession</code> implementation. The
	 * {@link CryptoSession#getCryptoSessionID() cryptoSessionID} is then passed from the client to
	 * the server which itself passes it to the <code>PersistenceManager</code> (or <code>EntityManager</code>)
	 * via the property with the name {@link CryptoSession#PROPERTY_CRYPTO_SESSION_ID}.
	 * </p>
	 * <p>
	 * Calling this method with a non-existing <code>cryptoSessionID</code> implicitely creates
	 * a <code>CryptoSession</code> instance and returns it. A future call to this method with the same
	 * <code>cryptoSessionID</code> returns the same <code>CryptoSession</code> instance.
	 * </p>
	 * <p>
	 * A <code>CryptoSession</code> should only be kept in the memory of a <code>CryptoManager</code> for a limited time.
	 * It is recommended to remove it
	 * a short configurable time (e.g. 10 minutes) after the {@link CryptoSession#getLastUsageTimestamp() last usage}.
	 * </p>
	 * <p>
	 * This method must call {@link CryptoSession#updateLastUsageTimestamp()}.
	 * </p>
	 * <p>
	 * This method is thread-safe.
	 * </p>
	 *
	 * @param cryptoSessionID the {@link CryptoSession#getCryptoSessionID() cryptoSessionID} for which to look up or
	 * create a <code>CryptoSession</code>.
	 * @return the <code>CryptoSession</code> identified by the given identifier; never <code>null</code>.
	 */
	CryptoSession getCryptoSession(String cryptoSessionID);

	/**
	 * <p>
	 * Notify the {@link CryptoManager} about the fact that a session is currently being closed.
	 * </p>
	 * <p>
	 * <b>Important:</b> This method must never be called directly! It must be called by {@link CryptoSession#close()}.
	 * </p>
	 *
	 * @param cryptoSession the session that is currently closed.
	 */
	void onCloseCryptoSession(CryptoSession cryptoSession);

	/**
	 * Get the value of the property {@value #PROPERTY_ENCRYPTION_ALGORITHM}.
	 * This property can be configured in the persistence-unit/persistence-properties-file.
	 * @return the currently configured encryption algorithm.
	 * @see #PROPERTY_ENCRYPTION_ALGORITHM
	 */
	String getEncryptionAlgorithm();

	/**
	 * Get the value of the property {@value #PROPERTY_MAC_ALGORITHM}.
	 * This property can be configured in the persistence-unit/persistence-properties-file.
	 * @return the currently configured MAC algorithm.
	 * @see #PROPERTY_MAC_ALGORITHM
	 */
	String getMacAlgorithm();

}
