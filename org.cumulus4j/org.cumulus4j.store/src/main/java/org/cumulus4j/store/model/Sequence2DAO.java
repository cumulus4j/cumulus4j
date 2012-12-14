package org.cumulus4j.store.model;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.identity.StringIdentity;

import org.cumulus4j.store.crypto.CryptoContext;

public class Sequence2DAO extends AbstractDAO {

	private int keyStoreRefID;

	public Sequence2DAO() { }

	/**
	 * @param pmData the backend-<code>PersistenceManager</code> used to access the underlying datastore; must not be <code>null</code>.
	 * If there are multiple datastores (data + index), then this is the one used for data.
	 * @param keyStoreRefID the key-store-reference-ID obtained usually from {@link CryptoContext#getKeyStoreRefID()}.
	 */
	public Sequence2DAO(PersistenceManager pmData, int keyStoreRefID) {
		super(pmData);
		this.keyStoreRefID = keyStoreRefID;
	}

	/**
	 * Get the <code>Sequence</code> identified by the given <code>sequenceName</code>.
	 * If no such <code>Sequence</code> exists, this method returns <code>null</code>.
	 * @param sequenceName the name of the sequence; must not be <code>null</code>.
	 * @return the <code>Sequence</code> identified by the given <code>sequenceName</code> or <code>null</code>, if no such
	 * <code>Sequence</code> exists.
	 */
	public Sequence2 getSequence2(String sequenceName)
	{
		StringIdentity id = new StringIdentity(Sequence2.class, Sequence2.createSequenceID(keyStoreRefID, sequenceName));
		Sequence2 sequence;
		try {
			sequence = (Sequence2) pm.getObjectById(id);
		} catch (JDOObjectNotFoundException x) {
			sequence = null;
		}
		return sequence;
	}

	/**
	 * Get the <code>Sequence</code> identified by the given <code>sequenceName</code>.
	 * If no such <code>Sequence</code> exists, this method creates &amp; persists one.
	 * @param sequenceName the name of the sequence; must not be <code>null</code>.
	 * @return the <code>Sequence</code> identified by the given <code>sequenceName</code>; never <code>null</code>.
	 */
	public Sequence2 createSequence2(String sequenceName)
	{
		Sequence2 sequence = getSequence2(sequenceName);
		if (sequence == null)
			sequence = pm.makePersistent(new Sequence2(keyStoreRefID, sequenceName));

		return sequence;
	}

}
