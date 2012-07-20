package org.cumulus4j.store.datastoreversion;



public interface DatastoreVersionCommand {
	String getDatastoreVersionID();

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
}
