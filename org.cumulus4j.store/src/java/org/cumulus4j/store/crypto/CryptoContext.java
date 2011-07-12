package org.cumulus4j.store.crypto;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.EncryptionCoordinateSetManager;
import org.cumulus4j.store.PersistenceManagerConnection;
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
	 * @param executionContext the <code>ExecutionContext</code> to be used in this context; must not be <code>null</code>.
	 * @param persistenceManagerConnection the <code>PersistenceManagerConnection</code> to be used in this context; must not be <code>null</code>.
	 */
	public CryptoContext(EncryptionCoordinateSetManager encryptionCoordinateSetManager, ExecutionContext executionContext, PersistenceManagerConnection persistenceManagerConnection)
	{
		if (encryptionCoordinateSetManager == null)
			throw new IllegalArgumentException("encryptionCoordinateSetManager == null");

		if (executionContext == null)
			throw new IllegalArgumentException("executionContext == null");

		if (persistenceManagerConnection == null)
			throw new IllegalArgumentException("persistenceManagerConnection == null");

		this.encryptionCoordinateSetManager = encryptionCoordinateSetManager;
		this.executionContext = executionContext;
		this.persistenceManagerConnection = persistenceManagerConnection;
		this.persistenceManagerForData = persistenceManagerConnection.getDataPM();
		this.persistenceManagerForIndex = persistenceManagerConnection.getIndexPM();
	}

	private EncryptionCoordinateSetManager encryptionCoordinateSetManager;

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
}
