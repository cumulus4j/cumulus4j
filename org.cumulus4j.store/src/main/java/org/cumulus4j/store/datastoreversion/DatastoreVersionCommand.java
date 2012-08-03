package org.cumulus4j.store.datastoreversion;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.model.KeyStoreRef;

/**
 * Command for managing a datastore version (change).
 * <p>
 * A new instance of a class implementing this interface is created for each run of
 * {@link DatastoreVersionManager#applyOnce(org.cumulus4j.store.crypto.CryptoContext)}. Therefore, implementations
 * do not need to be thread-safe and can share data in fields across implementation methods.
 * <p>
 * Implementations should subclass {@link AbstractDatastoreVersionCommand} instead of directly implementing
 * this interface.
 * <p>
 * Implementations must be manually registered in {@link org.cumulus4j.store.datastoreversion.DatastoreVersionManager}!
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface DatastoreVersionCommand {
	String getCommandID();

	/**
	 * Get the version of this command.
	 * <p>
	 * This version must be incremented, if this command is modified!
	 * <p>
	 * Note, that a {@link #isFinal() final} command cannot be modified!!! You can only modify a final command
	 * if this command was newly introduced in the current SNAPSHOT. <b><u>Never</u> modify a command after it
	 * was released!!!</b>
	 * @return the version of this command.
	 */
	int getCommandVersion();

	/**
	 * Perform the upgrade or do whatever this command has to do.
	 * @param commandApplyParam various arguments bundled for better compatibility with future extensions. Never <code>null</code>.
	 */
	void apply(CommandApplyParam commandApplyParam);

	/**
	 * Is this command final, i.e. applied only once, or should this command be applied again, when the commandVersion
	 * was incremented?
	 * @return <code>true</code>, if this command is final; <code>false</code> otherwise.
	 */
	boolean isFinal();

	/**
	 * Is this command dependent on the key-store? If yes, it is applied separately for every key-store, i.e. with different
	 * {@link KeyStoreRef#getKeyStoreRefID() keyStoreRefID}s. If no, it is applied only once
	 * {@link KeyStoreRef#GLOBAL_KEY_STORE_REF_ID globally}.
	 * @return <code>true</code>, if this command should be applied once per key-store (and per underlying {@link PersistenceManager}
	 * [there might be two if data and index is stored separately]); <code>false</code> otherwise.
	 */
	boolean isKeyStoreDependent();
}
