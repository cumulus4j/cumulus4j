package org.cumulus4j.store.datastoreversion;

import java.util.Date;
import java.util.Properties;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.WorkInProgressException;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.DatastoreVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandApplyParam
{
	private static final Logger logger = LoggerFactory.getLogger(CommandApplyParam.class);
	public static final String PROPERTY_KEY_TIMEOUT = "cumulus4j.DatastoreVersionCommand.applyWorkInProgressTimeout";

	private Date applyStartTimestamp = new Date();
	private Cumulus4jStoreManager storeManager;
	private CryptoContext cryptoContext;
	private PersistenceManager persistenceManager;
	private DatastoreVersion datastoreVersion;
//	private Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap;
	private Properties workInProgressStateProperties;

	/**
	 *
	 * @param storeManager TODO
	 * @param cryptoContext the context; must not be <code>null</code>.
	 * @param persistenceManager the persistence-manager; must not be <code>null</code>.
	 * @param datastoreVersion the current datastore-version (representing the last execution of the same command
	 * as currently being applied). Always <code>null</code>, if the command is final. Only not <code>null</code>, if
	 * the command is not final and was already applied in an earlier version.
	 * @param workInProgressStateProperties TODO
	 */
	public CommandApplyParam(
			Cumulus4jStoreManager storeManager, CryptoContext cryptoContext, PersistenceManager persistenceManager,
			DatastoreVersion datastoreVersion,
			//			Map<String, DatastoreVersion> datastoreVersionID2DatastoreVersionMap,
			Properties workInProgressStateProperties
	)
	{
		if (storeManager == null)
			throw new IllegalArgumentException("storeManager == null");

		if (cryptoContext == null)
			throw new IllegalArgumentException("cryptoContext == null");

		if (persistenceManager == null)
			throw new IllegalArgumentException("persistenceManager == null");

//		if (datastoreVersionID2DatastoreVersionMap == null)
//			throw new IllegalArgumentException("datastoreVersionID2DatastoreVersionMap == null");

		if (workInProgressStateProperties == null)
			throw new IllegalArgumentException("workInProgressStateProperties == null");

		this.storeManager = storeManager;
		this.cryptoContext = cryptoContext;
		this.persistenceManager = persistenceManager;
		this.datastoreVersion = datastoreVersion;
//		this.datastoreVersionID2DatastoreVersionMap = datastoreVersionID2DatastoreVersionMap;
		this.workInProgressStateProperties = workInProgressStateProperties;
	}

	public Cumulus4jStoreManager getStoreManager() {
		return storeManager;
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

//	public Map<String, DatastoreVersion> getDatastoreVersionID2DatastoreVersionMap() {
//		return datastoreVersionID2DatastoreVersionMap;
//	}

	public Properties getWorkInProgressStateProperties() {
		return workInProgressStateProperties;
	}

	/**
	 * Get the time in milliseconds after which a {@link WorkInProgressException} should be thrown by the
	 * {@link DatastoreVersionCommand}.
	 * @return the timeout in milliseconds.
	 */
	public long getDatastoreVersionCommandApplyWorkInProgressTimeout() {
		long result = 10L * 1000L;
		Object propertyValue = storeManager.getProperty(PROPERTY_KEY_TIMEOUT);
		if (propertyValue == null)
			return result;

		long val;
		try {
			val = Long.parseLong(propertyValue.toString());
		} catch (NumberFormatException x) {
			logger.warn(String.format("Value for property '%s' is not a valid number!", PROPERTY_KEY_TIMEOUT), x);
			return result;
		}

		if (val <= 0) {
			logger.warn("Value for property '{}' is zero or negative, but must be positive!", PROPERTY_KEY_TIMEOUT);
			return result;
		}

		result = val;
		return result;
	}

	public Date getApplyStartTimestamp() {
		return applyStartTimestamp;
	}

	public boolean isDatastoreVersionCommandApplyWorkInProgressTimeoutExceeded() {
		return System.currentTimeMillis() - applyStartTimestamp.getTime() > getDatastoreVersionCommandApplyWorkInProgressTimeout();
	}
}
