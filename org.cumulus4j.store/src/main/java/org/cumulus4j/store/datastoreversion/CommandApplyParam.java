package org.cumulus4j.store.datastoreversion;

import java.util.Map;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.DatastoreVersion;

public class CommandApplyParam
{
	private CryptoContext cryptoContext;
	private PersistenceManager persistenceManager;
	private DatastoreVersion datastoreVersion;
	private Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap;

	/**
	 *
	 * @param cryptoContext the context; must not be <code>null</code>.
	 * @param persistenceManager the persistence-manager; must not be <code>null</code>.
	 * @param datastoreVersion the current datastore-version (representing the last execution of the same command
	 * as currently being applied). Always <code>null</code>, if the command is final. Only not <code>null</code>, if
	 * the command is not final and was already applied in an earlier version.
	 * @param datastoreVersionID2DatastoreVersionMap
	 */
	public CommandApplyParam(
			CryptoContext cryptoContext, PersistenceManager persistenceManager, DatastoreVersion datastoreVersion,
			Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap
	)
	{
		if (cryptoContext == null)
			throw new IllegalArgumentException("cryptoContext == null");

		if (persistenceManager == null)
			throw new IllegalArgumentException("persistenceManager == null");

		if (datastoreVersionID2DatastoreVersionMap == null)
			throw new IllegalArgumentException("datastoreVersionID2DatastoreVersionMap == null");

		this.cryptoContext = cryptoContext;
		this.persistenceManager = persistenceManager;
		this.datastoreVersion = datastoreVersion;
		this.datastoreVersionID2DatastoreVersionMap = datastoreVersionID2DatastoreVersionMap;
	}

	public CryptoContext getCryptoContext() {
		return cryptoContext;
	}

	public PersistenceManager getPersistenceManager() {
		return persistenceManager;
	}

	public DatastoreVersion getDatastoreVersion() {
		return datastoreVersion;
	}

	public Map<String, DatastoreVersion> getDatastoreVersionID2DatastoreVersionMap() {
		return datastoreVersionID2DatastoreVersionMap;
	}
}
