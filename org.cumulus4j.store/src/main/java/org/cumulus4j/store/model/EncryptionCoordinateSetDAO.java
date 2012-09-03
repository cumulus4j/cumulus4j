package org.cumulus4j.store.model;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.identity.IntIdentity;

public class EncryptionCoordinateSetDAO extends AbstractDAO {

	public EncryptionCoordinateSetDAO() { }

	/**
	 * @param pm the backend-{@link PersistenceManager} (the one used for data, if there is a separate index-DB used).
	 */
	public EncryptionCoordinateSetDAO(PersistenceManager pm) {
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

	/**
	 * Get an existing <code>EncryptionCoordinateSet</code> identified by its {@link #getEncryptionCoordinateSetID() encryptionCoordinateSetID}.
	 *
	 * @param encryptionCoordinateSetID the {@link #getEncryptionCoordinateSetID() identifier} of the searched instance.
	 * @return the <code>EncryptionCoordinateSet</code> identified by the given <code>encryptionCoordinateSetID</code> or
	 * <code>null</code>, if no such instance exists in the datastore.
	 */
	public EncryptionCoordinateSet getEncryptionCoordinateSet(int encryptionCoordinateSetID)
	{
		IntIdentity id = new IntIdentity(EncryptionCoordinateSet.class, encryptionCoordinateSetID);
		try {
			EncryptionCoordinateSet encryptionCoordinateSet = (EncryptionCoordinateSet) pm.getObjectById(id);
			return encryptionCoordinateSet;
		} catch (JDOObjectNotFoundException x) {
			return null;
		}
	}

	/**
	 * <p>
	 * Get an existing <code>EncryptionCoordinateSet</code> identified by its unique properties.
	 * </p>
	 * <p>
	 * As each <code>EncryptionCoordinateSet</code> maps all encryption settings to an ID, all
	 * properties of this class except for the ID form a unique index together. At the moment,
	 * these are: {@link #getCipherTransformation() cipher-transformation} and {@link #getMACAlgorithm() MAC-algorithm}.
	 * </p>
	 *
	 * @param cipherTransformation the {@link #getCipherTransformation() cipher-transformation} of the searched instance.
	 * Must not be <code>null</code>.
	 * @param macAlgorithm the {@link #getMACAlgorithm()} of the searched instance. Must not be <code>null</code>
	 * (use {@value #MAC_ALGORITHM_NONE} for no MAC).
	 * @return the <code>EncryptionCoordinateSet</code> identified by the given properties or
	 * <code>null</code>, if no such instance exists in the datastore.
	 * @see #createEncryptionCoordinateSet(PersistenceManager, String, String)
	 */
	public EncryptionCoordinateSet getEncryptionCoordinateSet(String cipherTransformation, String macAlgorithm)
	{
		if (cipherTransformation == null)
			throw new IllegalArgumentException("cipherTransformation == null");

		if (macAlgorithm == null)
			throw new IllegalArgumentException("macAlgorithm == null");

		javax.jdo.Query q = pm.newNamedQuery(EncryptionCoordinateSet.class, "getEncryptionCoordinateSetByAllAlgorithms");
		return (EncryptionCoordinateSet) q.execute(cipherTransformation, macAlgorithm);
		// UNIQUE query does not need to be closed, because there is no result list lingering.
	}

	/**
	 * <p>
	 * Get an existing <code>EncryptionCoordinateSet</code> identified by its unique properties or create one
	 * if necessary.
	 * </p>
	 * <p>
	 * This method is similar to {@link #getEncryptionCoordinateSet(PersistenceManager, String, String)}, but
	 * creates a new <code>EncryptionCoordinateSet</code> instead of returning <code>null</code>, if there is
	 * no existing instance, yet.
	 * </p>
	 *
	 * @param cipherTransformation the {@link #getCipherTransformation() cipher-transformation} of the searched instance.
	 * Must not be <code>null</code>.
	 * @param macAlgorithm the {@link #getMACAlgorithm()} of the searched instance. Must not be <code>null</code>
	 * (use {@value #MAC_ALGORITHM_NONE} for no MAC).
	 * @return the <code>EncryptionCoordinateSet</code> identified by the given properties. This method never returns
	 * <code>null</code>, but instead creates and persists a new instance if needed.
	 * @see #getEncryptionCoordinateSet(PersistenceManager, String, String)
	 */
	public EncryptionCoordinateSet createEncryptionCoordinateSet(String cipherTransformation, String macAlgorithm)
	{
		EncryptionCoordinateSet encryptionCoordinateSet = getEncryptionCoordinateSet(cipherTransformation, macAlgorithm);
		if (encryptionCoordinateSet == null) {
			encryptionCoordinateSet = pm.makePersistent(new EncryptionCoordinateSet(cipherTransformation, macAlgorithm));
			// It is essential that the first ID is 0 (and never a negative value), because
			// we encode this ID into the binary data assuming that it is positive or 0!
			// Hence, we check here, already.
			if (encryptionCoordinateSet.getEncryptionCoordinateSetID() < 0)
				throw new IllegalStateException("encryptionCoordinateSetID = " + encryptionCoordinateSet.getEncryptionCoordinateSetID() + " < 0!!!");
		}

		return encryptionCoordinateSet;
	}

}
