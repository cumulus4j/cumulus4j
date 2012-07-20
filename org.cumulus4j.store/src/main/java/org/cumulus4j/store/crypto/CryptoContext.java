package org.cumulus4j.store.crypto;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.EncryptionCoordinateSetManager;
import org.cumulus4j.store.KeyStoreRefManager;
import org.cumulus4j.store.PersistenceManagerConnection;
import org.cumulus4j.store.model.KeyStoreRef;
import org.datanucleus.store.ExecutionContext;

/**
 * Context for encryption and decryption.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoContext
{
	/**
	 * Create a new context.
	 * @param encryptionCoordinateSetManager the <code>EncryptionCoordinateSetManager</code> to be used in this context; must not be <code>null</code>.
	 * @param keyStoreRefManager the <code>KeyStoreRefManager</code> to be used in this context; must not be <code>null</code>.
	 * @param executionContext the <code>ExecutionContext</code> to be used in this context; must not be <code>null</code>.
	 * @param persistenceManagerConnection the <code>PersistenceManagerConnection</code> to be used in this context; must not be <code>null</code>.
	 */
	public CryptoContext(EncryptionCoordinateSetManager encryptionCoordinateSetManager, KeyStoreRefManager keyStoreRefManager, ExecutionContext executionContext, PersistenceManagerConnection persistenceManagerConnection)
	{
		if (encryptionCoordinateSetManager == null)
			throw new IllegalArgumentException("encryptionCoordinateSetManager == null");

		if (keyStoreRefManager == null)
			throw new IllegalArgumentException("keyStoreRefManager == null");

		if (executionContext == null)
			throw new IllegalArgumentException("executionContext == null");

		if (persistenceManagerConnection == null)
			throw new IllegalArgumentException("persistenceManagerConnection == null");

		this.encryptionCoordinateSetManager = encryptionCoordinateSetManager;
		this.keyStoreRefManager = keyStoreRefManager;
		this.executionContext = executionContext;
		this.persistenceManagerConnection = persistenceManagerConnection;
		this.persistenceManagerForData = persistenceManagerConnection.getDataPM();
		this.persistenceManagerForIndex = persistenceManagerConnection.getIndexPM();
	}

	private EncryptionCoordinateSetManager encryptionCoordinateSetManager;

	public KeyStoreRefManager getKeyStoreRefManager() {
		return keyStoreRefManager;
	}

	private KeyStoreRefManager keyStoreRefManager;

	/**
	 * Get the <code>EncryptionCoordinateSetManager</code> to be used in this context; never <code>null</code>.
	 * @return the <code>EncryptionCoordinateSetManager</code> to be used in this context; never <code>null</code>.
	 */
	public EncryptionCoordinateSetManager getEncryptionCoordinateSetManager() {
		return encryptionCoordinateSetManager;
	}

	private ExecutionContext executionContext;

	/**
	 * Get the <code>ExecutionContext</code> to be used in this context; never <code>null</code>.
	 * @return the <code>ExecutionContext</code> to be used in this context; never <code>null</code>.
	 */
	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	private PersistenceManagerConnection persistenceManagerConnection;

	/**
	 * Get the <code>PersistenceManagerConnection</code> to be used in this context; never <code>null</code>.
	 * @return the <code>PersistenceManagerConnection</code> to be used in this context; never <code>null</code>.
	 */
	public PersistenceManagerConnection getPersistenceManagerConnection() {
		return persistenceManagerConnection;
	}

	private PersistenceManager persistenceManagerForData;

	/**
	 * Convenience method synonymous to {@link PersistenceManagerConnection#getDataPM()}.
	 * @return the PM used for the actual data.
	 */
	public PersistenceManager getPersistenceManagerForData() {
		return persistenceManagerForData;
	}

	private PersistenceManager persistenceManagerForIndex;

	/**
	 * Convenience method synonymous to {@link PersistenceManagerConnection#getIndexPM()}.
	 * @return the PM used for index data. If there is no separate index-datastore, this
	 * is the same as {@link #getPersistenceManagerForData()}.
	 */
	public PersistenceManager getPersistenceManagerForIndex() {
		return persistenceManagerForIndex;
	}

	public CryptoSession getCryptoSession()
	{
		ExecutionContext ec = executionContext;
		Object cryptoManagerID = ec.getProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID);
		if (cryptoManagerID == null)
			throw new IllegalStateException("Property \"" + CryptoManager.PROPERTY_CRYPTO_MANAGER_ID + "\" is not set!");

		if (!(cryptoManagerID instanceof String))
			throw new IllegalStateException("Property \"" + CryptoManager.PROPERTY_CRYPTO_MANAGER_ID + "\" is set, but it is an instance of " + cryptoManagerID.getClass().getName() + " instead of java.lang.String!");

		CryptoManager cryptoManager = CryptoManagerRegistry.sharedInstance(ec.getNucleusContext()).getCryptoManager((String) cryptoManagerID);

		Object cryptoSessionID = ec.getProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID);
		if (cryptoSessionID == null)
			throw new IllegalStateException("Property \"" + CryptoSession.PROPERTY_CRYPTO_SESSION_ID + "\" is not set!");

		if (!(cryptoSessionID instanceof String))
			throw new IllegalStateException("Property \"" + CryptoSession.PROPERTY_CRYPTO_SESSION_ID + "\" is set, but it is an instance of " + cryptoSessionID.getClass().getName() + " instead of java.lang.String!");

		CryptoSession cryptoSession = cryptoManager.getCryptoSession((String) cryptoSessionID);
		return cryptoSession;
	}

	private Integer keyStoreRefID;

	public int getKeyStoreRefID() {
		Integer keyStoreRefID = this.keyStoreRefID;
		if (keyStoreRefID == null) {
			String keyStoreID = getCryptoSession().getKeyStoreID();
			KeyStoreRef keyStoreRef = getKeyStoreRefManager().createKeyStoreRef(getPersistenceManagerConnection(), keyStoreID);
			keyStoreRefID = keyStoreRef.getKeyStoreRefID();
			this.keyStoreRefID = keyStoreRefID;
		}
		return keyStoreRefID;
	}
}
