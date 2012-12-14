package org.cumulus4j.store.model;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.identity.IntIdentity;

public class KeyStoreRefDAO extends AbstractDAO {

	public KeyStoreRefDAO() { }

	/**
	 * @param pm the backend-{@link PersistenceManager} (the one used for data, if there is a separate index-DB used).
	 */
	public KeyStoreRefDAO(PersistenceManager pm) {
		super(pm);
	}

	/**
	 * {@inheritDoc}
	 * @param pm the backend-{@link PersistenceManager} (the one used for data, if there is a separate index-DB used).
	 */
	@Override
	public void setPersistenceManager(PersistenceManager pm) {
		super.setPersistenceManager(pm);
	}

	public KeyStoreRef getKeyStoreRef(int keyStoreRefID) {
		IntIdentity id = new IntIdentity(KeyStoreRef.class, keyStoreRefID);
		try {
			KeyStoreRef keyStoreRef = (KeyStoreRef) pm.getObjectById(id);
			return keyStoreRef;
		} catch (JDOObjectNotFoundException x) {
			return null;
		}
	}

	public KeyStoreRef getKeyStoreRef(String keyStoreID) {
		if (keyStoreID == null)
			throw new IllegalArgumentException("keyStoreID == null");

		Query q = pm.newNamedQuery(KeyStoreRef.class, "getKeyStoreRefByKeyStoreID");
		return (KeyStoreRef) q.execute(keyStoreID);
		// UNIQUE query does not need to be closed, because there is no result list lingering.
	}

	public KeyStoreRef createKeyStoreRef(String keyStoreID) {
		KeyStoreRef keyStoreRef = getKeyStoreRef(keyStoreID);
		if (keyStoreRef == null) {
			keyStoreRef = pm.makePersistent(new KeyStoreRef(keyStoreID));
			// It does not matter, whether this ID is negative, or not, but we don't
			// need so many IDs and thus we can use solely positive numbers. And as
			// we *require* positive numbers in EncryptionCoordinateSet-IDs, the underlying
			// datastore needs to support starting at 0, anyway, hence we can make this
			// constraint for the sake of beauty (of the raw datastore).
			// In many cases, 0 is the one and only ID, because we seldomly share
			// one underlying database among multiple tenants (= key stores).
			// Additionally, we use 0 as default value which is relevant for updates
			// from older Cumulus4j versions.
			if (keyStoreRef.getKeyStoreRefID() < 0)
				throw new IllegalStateException("keyStoreRefID = " + keyStoreRef.getKeyStoreRefID() + " < 0!!!");
		}
		return keyStoreRef;
	}
}
