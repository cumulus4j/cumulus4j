package org.cumulus4j.store;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.store.model.EncryptionCoordinateSet;

/**
 * <p>
 * Manager for {@link EncryptionCoordinateSet} instances.
 * </p><p>
 * There exists one <code>EncryptionCoordinateSetManager</code> instance per {@link Cumulus4jStoreManager}.
 * The <code>EncryptionCoordinateSet</code>s held by this manager are detached (with all properties)
 * and thus kept across all transactions.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class EncryptionCoordinateSetManager
{
	private Map<Integer, EncryptionCoordinateSet> encryptionCoordinateSetID2EncryptionCoordinateSet = new HashMap<Integer, EncryptionCoordinateSet>();

	private Map<String, EncryptionCoordinateSet> encryptionCoordinateString2EncryptionCoordinateSet = new HashMap<String, EncryptionCoordinateSet>();

	private static String getEncryptionCoordinateString(String cipherTransformation, String macAlgorithm)
	{
		return cipherTransformation + "::" + macAlgorithm;
	}
	private static String getEncryptionCoordinateString(EncryptionCoordinateSet encryptionCoordinateSet)
	{
		return getEncryptionCoordinateString(encryptionCoordinateSet.getCipherTransformation(), encryptionCoordinateSet.getMACAlgorithm());
	}

	/**
	 * Create an instance.
	 */
	public EncryptionCoordinateSetManager() { }

	/**
	 * Get the {@link EncryptionCoordinateSet} identified by the given <code>encryptionCoordinateSetID</code>.
	 * If no such <code>EncryptionCoordinateSet</code> exists, <code>null</code> is returned.
	 * @param persistenceManagerConnection the connection to the underlying datastore(s).
	 * @param encryptionCoordinateSetID {@link EncryptionCoordinateSet#getEncryptionCoordinateSetID() identifier} of the
	 * <code>EncryptionCoordinateSet</code> to be retrieved.
	 * @return the {@link EncryptionCoordinateSet} identified by the given <code>encryptionCoordinateSetID</code> or <code>null</code>.
	 */
	public synchronized EncryptionCoordinateSet getEncryptionCoordinateSet(PersistenceManagerConnection persistenceManagerConnection, int encryptionCoordinateSetID)
	{
		EncryptionCoordinateSet encryptionCoordinateSet = encryptionCoordinateSetID2EncryptionCoordinateSet.get(encryptionCoordinateSetID);
		if (encryptionCoordinateSet == null) {
			PersistenceManager pm = persistenceManagerConnection.getDataPM();
			encryptionCoordinateSet = EncryptionCoordinateSet.getEncryptionCoordinateSet(pm, encryptionCoordinateSetID);
			if (encryptionCoordinateSet != null) {
				pm.getFetchPlan().setMaxFetchDepth(-1);
				pm.getFetchPlan().setGroup(FetchPlan.ALL);
				encryptionCoordinateSet = pm.detachCopy(encryptionCoordinateSet);
				encryptionCoordinateSetID2EncryptionCoordinateSet.put(
						encryptionCoordinateSet.getEncryptionCoordinateSetID(), encryptionCoordinateSet
				);
				encryptionCoordinateString2EncryptionCoordinateSet.put(
						getEncryptionCoordinateString(encryptionCoordinateSet), encryptionCoordinateSet
				);
			}
		}
		return encryptionCoordinateSet;
	}

	/**
	 * <p>
	 * Get the {@link EncryptionCoordinateSet} identified by the given properties.
	 * </p><p>
	 * If it does not yet exist in the in-memory-cache,
	 * it is looked up in the datastore. If it is found there, it is detached, cached and returned. If it does not exist in the
	 * datastore either, it is - if <code>create == true</code> - created, persisted, detached, cached and returned; if
	 * <code>create == false</code>, <code>null</code> is returned instead.
	 * </p><p>
	 * The <code>EncryptionCoordinateSet</code> instances are only held in the
	 * {@link PersistenceManagerConnection#getDataPM() data-datastore} (not in the index-datastore). This might change in the future
	 * (in case replication becomes necessary).
	 * </p>
	 *
	 * @param create whether to create a new instance, if it does not yet exist. If <code>true</code>, a new instance
	 * will be created, persisted, detached, cached and returned, if it does not yet exist. If <code>false</code>, <code>null</code>
	 * will be returned instead.
	 * @param persistenceManagerConnection the connection to the underlying datastore(s).
	 * @param cipherTransformation the transformation (as passed to {@link CryptoRegistry#createCipher(String)}) used to encrypt and
	 * decrypt the persistent data (or index).
	 * @param macAlgorithm the <a href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a> algorithm (as passed to {@link CryptoRegistry#createMACCalculator(String, boolean)})
	 * used to verify peristent records for integrity. Might be {@link EncryptionCoordinateSet#MAC_ALGORITHM_NONE} to deactivate
	 * the MAC calculation.
	 * @return the <code>EncryptionCoordinateSet</code> (detached) matching the given properties. If <code>create == true</code>, this
	 * is never <code>null</code>. If <code>create == false</code> and there does not yet exist an appropriate
	 * <code>EncryptionCoordinateSet</code>, this is <code>null</code>.
	 */
	protected EncryptionCoordinateSet _createOrGetEncryptionCoordinateSet(boolean create, PersistenceManagerConnection persistenceManagerConnection, String cipherTransformation, String macAlgorithm)
	{
		String encryptionCoordinateString = getEncryptionCoordinateString(cipherTransformation, macAlgorithm);
		EncryptionCoordinateSet encryptionCoordinateSet = encryptionCoordinateString2EncryptionCoordinateSet.get(encryptionCoordinateString);
		if (encryptionCoordinateSet == null) {
			PersistenceManager pm = persistenceManagerConnection.getDataPM();

			if (create)
				encryptionCoordinateSet = EncryptionCoordinateSet.createEncryptionCoordinateSet(pm, cipherTransformation, macAlgorithm);
			else
				encryptionCoordinateSet = EncryptionCoordinateSet.getEncryptionCoordinateSet(pm, cipherTransformation, macAlgorithm);

			if (encryptionCoordinateSet != null) {
				pm.getFetchPlan().setMaxFetchDepth(-1);
				pm.getFetchPlan().setGroup(FetchPlan.ALL);
				encryptionCoordinateSet = pm.detachCopy(encryptionCoordinateSet);
				encryptionCoordinateSetID2EncryptionCoordinateSet.put(
						encryptionCoordinateSet.getEncryptionCoordinateSetID(), encryptionCoordinateSet
				);
				encryptionCoordinateString2EncryptionCoordinateSet.put(
						getEncryptionCoordinateString(encryptionCoordinateSet), encryptionCoordinateSet
				);
			}
		}
		return encryptionCoordinateSet;
	}

	/**
	 * <p>
	 * Get the {@link EncryptionCoordinateSet} identified by the given properties.
	 * </p><p>
	 * If there is no appropriate <code>EncryptionCoordinateSet</code> (neither in the in-memory-cache nor in the datastore),
	 * <code>null</code> is returned.
	 * </p><p>
	 * This method delegates to {@link #_createOrGetEncryptionCoordinateSet(boolean, PersistenceManagerConnection, String, String)} with
	 * <code>create == false</code>.
	 * </p>
	 *
	 * @param persistenceManagerConnection the connection to the underlying datastore(s).
	 * @param cipherTransformation the transformation (as passed to {@link CryptoRegistry#createCipher(String)}) used to encrypt and
	 * decrypt the persistent data (or index).
	 * @param macAlgorithm the <a href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a> algorithm (as passed to {@link CryptoRegistry#createMACCalculator(String, boolean)})
	 * used to verify peristent records for integrity. Might be {@link EncryptionCoordinateSet#MAC_ALGORITHM_NONE} to deactivate
	 * the MAC calculation.
	 * @return the <code>EncryptionCoordinateSet</code> (detached) matching the given properties or <code>null</code>.
	 */
	public synchronized EncryptionCoordinateSet getEncryptionCoordinateSet(PersistenceManagerConnection persistenceManagerConnection, String cipherTransformation, String macAlgorithm)
	{
		return _createOrGetEncryptionCoordinateSet(false, persistenceManagerConnection, cipherTransformation, macAlgorithm);
	}

	/**
	 * <p>
	 * Get the {@link EncryptionCoordinateSet} identified by the given properties.
	 * </p><p>
	 * If there is no appropriate <code>EncryptionCoordinateSet</code> (neither in the in-memory-cache nor in the datastore),
	 * it is created and persisted.
	 * </p><p>
	 * This method delegates to {@link #_createOrGetEncryptionCoordinateSet(boolean, PersistenceManagerConnection, String, String)} with
	 * <code>create == true</code>.
	 * </p>
	 *
	 * @param persistenceManagerConnection the connection to the underlying datastore(s).
	 * @param cipherTransformation the transformation (as passed to {@link CryptoRegistry#createCipher(String)}) used to encrypt and
	 * decrypt the persistent data (or index).
	 * @param macAlgorithm the <a href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a> algorithm (as passed to {@link CryptoRegistry#createMACCalculator(String, boolean)})
	 * used to verify peristent records for integrity. Might be {@link EncryptionCoordinateSet#MAC_ALGORITHM_NONE} to deactivate
	 * the MAC calculation.
	 * @return the <code>EncryptionCoordinateSet</code> (detached) matching the given properties; never <code>null</code>.
	 */
	public synchronized EncryptionCoordinateSet createEncryptionCoordinateSet(PersistenceManagerConnection persistenceManagerConnection, String cipherTransformation, String macAlgorithm)
	{
		return _createOrGetEncryptionCoordinateSet(true, persistenceManagerConnection, cipherTransformation, macAlgorithm);
	}
}
