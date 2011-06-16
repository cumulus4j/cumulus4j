package org.cumulus4j.store.crypto;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.EncryptionCoordinateSetManager;
import org.cumulus4j.store.PersistenceManagerConnection;
import org.datanucleus.store.ExecutionContext;

public class CryptoContext
{

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

	public EncryptionCoordinateSetManager getEncryptionCoordinateSetManager() {
		return encryptionCoordinateSetManager;
	}

	private ExecutionContext executionContext;

	public ExecutionContext getExecutionContext() {
		return executionContext;
	}

	private PersistenceManagerConnection persistenceManagerConnection;

	public PersistenceManagerConnection getPersistenceManagerConnection() {
		return persistenceManagerConnection;
	}

	private PersistenceManager persistenceManagerForData;

	public PersistenceManager getPersistenceManagerForData() {
		return persistenceManagerForData;
	}

	private PersistenceManager persistenceManagerForIndex;

	public PersistenceManager getPersistenceManagerForIndex() {
		return persistenceManagerForIndex;
	}
}
