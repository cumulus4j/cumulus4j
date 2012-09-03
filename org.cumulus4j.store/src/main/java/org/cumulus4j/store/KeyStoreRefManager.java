package org.cumulus4j.store;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.cumulus4j.store.model.KeyStoreRef;
import org.cumulus4j.store.model.KeyStoreRefDAO;

public class KeyStoreRefManager {

	private Map<Integer, KeyStoreRef> keyStoreRefID2KeyStoreRef = new HashMap<Integer, KeyStoreRef>();
	private Map<String, KeyStoreRef> keyStoreID2KeyStoreRef = new HashMap<String, KeyStoreRef>();

	public KeyStoreRefManager() { }

	/**
	 * Get the {@link KeyStoreRef} identified by the given <code>keyStoreRefID</code>.
	 * If no such <code>KeyStoreRef</code> exists, <code>null</code> is returned.
	 * @param persistenceManagerConnection the connection to the underlying datastore(s).
	 * @param keyStoreRefID {@link KeyStoreRef#getKeyStoreRefID() identifier} of the
	 * <code>KeyStoreRef</code> to be retrieved.
	 * @return the {@link KeyStoreRef} identified by the given <code>keyStoreRefID</code> or <code>null</code>.
	 */
	public synchronized KeyStoreRef getKeyStoreRef(PersistenceManagerConnection persistenceManagerConnection, int keyStoreRefID)
	{
		KeyStoreRef keyStoreRef = keyStoreRefID2KeyStoreRef.get(keyStoreRefID);
		if (keyStoreRef == null) {
			PersistenceManager pm = persistenceManagerConnection.getDataPM();
			keyStoreRef = new KeyStoreRefDAO(pm).getKeyStoreRef(keyStoreRefID);
			if (keyStoreRef != null) {
				pm.getFetchPlan().setMaxFetchDepth(-1);
				pm.getFetchPlan().setGroup(FetchPlan.ALL);
				keyStoreRef = pm.detachCopy(keyStoreRef);
				keyStoreRefID2KeyStoreRef.put(
						keyStoreRef.getKeyStoreRefID(), keyStoreRef
				);
				keyStoreID2KeyStoreRef.put(
						keyStoreRef.getKeyStoreID(), keyStoreRef
				);
			}
		}
		return keyStoreRef;
	}

	/**
	 * <p>
	 * Get the {@link KeyStoreRef} identified by the given properties.
	 * </p><p>
	 * If it does not yet exist in the in-memory-cache,
	 * it is looked up in the datastore. If it is found there, it is detached, cached and returned. If it does not exist in the
	 * datastore either, it is - if <code>create == true</code> - created, persisted, detached, cached and returned; if
	 * <code>create == false</code>, <code>null</code> is returned instead.
	 * </p><p>
	 * The <code>KeyStoreRef</code> instances are only held in the
	 * {@link PersistenceManagerConnection#getDataPM() data-datastore} (not in the index-datastore). This might change in the future
	 * (in case replication becomes necessary).
	 * </p>
	 *
	 * @param create whether to create a new instance, if it does not yet exist. If <code>true</code>, a new instance
	 * will be created, persisted, detached, cached and returned, if it does not yet exist. If <code>false</code>, <code>null</code>
	 * will be returned instead.
	 * @param persistenceManagerConnection the connection to the underlying datastore(s).
	 * @param keyStoreID the <code>KeyStore</code>'s ID. Must not be <code>null</code>.
	 * @return the <code>KeyStoreRef</code> (detached) matching the given properties. If <code>create == true</code>, this
	 * is never <code>null</code>. If <code>create == false</code> and there does not yet exist an appropriate
	 * <code>KeyStoreRef</code>, this is <code>null</code>.
	 */
	protected KeyStoreRef _createOrGetKeyStoreRef(boolean create, PersistenceManagerConnection persistenceManagerConnection, String keyStoreID)
	{
		KeyStoreRef keyStoreRef = keyStoreID2KeyStoreRef.get(keyStoreID);
		if (keyStoreRef == null) {
			PersistenceManager pm = persistenceManagerConnection.getDataPM();

			if (create)
				keyStoreRef = new KeyStoreRefDAO(pm).createKeyStoreRef(keyStoreID);
			else
				keyStoreRef = new KeyStoreRefDAO(pm).getKeyStoreRef(keyStoreID);

			if (keyStoreRef != null) {
				pm.getFetchPlan().setMaxFetchDepth(-1);
				pm.getFetchPlan().setGroup(FetchPlan.ALL);
				keyStoreRef = pm.detachCopy(keyStoreRef);
				keyStoreRefID2KeyStoreRef.put(
						keyStoreRef.getKeyStoreRefID(), keyStoreRef
				);
				keyStoreID2KeyStoreRef.put(
						keyStoreRef.getKeyStoreID(), keyStoreRef
				);
			}
		}
		return keyStoreRef;
	}

	/**
	 * <p>
	 * Get the {@link KeyStoreRef} identified by the given properties.
	 * </p><p>
	 * If there is no appropriate <code>KeyStoreRef</code> (neither in the in-memory-cache nor in the datastore),
	 * <code>null</code> is returned.
	 * </p><p>
	 * This method delegates to {@link #_createOrGetKeyStoreRef(boolean, PersistenceManagerConnection, String, String)} with
	 * <code>create == false</code>.
	 * </p>
	 *
	 * @param persistenceManagerConnection the connection to the underlying datastore(s).
	 * @param keyStoreID the <code>KeyStore</code>'s ID. Must not be <code>null</code>.
	 * @return the <code>KeyStoreRef</code> (detached) matching the given properties or <code>null</code>.
	 */
	public synchronized KeyStoreRef getKeyStoreRef(PersistenceManagerConnection persistenceManagerConnection, String keyStoreID)
	{
		return _createOrGetKeyStoreRef(false, persistenceManagerConnection, keyStoreID);
	}

	/**
	 * <p>
	 * Get the {@link KeyStoreRef} identified by the given properties.
	 * </p><p>
	 * If there is no appropriate <code>KeyStoreRef</code> (neither in the in-memory-cache nor in the datastore),
	 * it is created and persisted.
	 * </p><p>
	 * This method delegates to {@link #_createOrGetKeyStoreRef(boolean, PersistenceManagerConnection, String, String)} with
	 * <code>create == true</code>.
	 * </p>
	 *
	 * @param persistenceManagerConnection the connection to the underlying datastore(s).
	 * @param keyStoreID the <code>KeyStore</code>'s ID. Must not be <code>null</code>.
	 * @return the <code>KeyStoreRef</code> (detached) matching the given properties; never <code>null</code>.
	 */
	public synchronized KeyStoreRef createKeyStoreRef(PersistenceManagerConnection persistenceManagerConnection, String keyStoreID)
	{
		return _createOrGetKeyStoreRef(true, persistenceManagerConnection, keyStoreID);
	}
}
