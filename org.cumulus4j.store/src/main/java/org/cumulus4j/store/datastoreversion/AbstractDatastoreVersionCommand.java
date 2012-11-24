package org.cumulus4j.store.datastoreversion;


public abstract class AbstractDatastoreVersionCommand implements DatastoreVersionCommand {

	@Override
	public String getCommandID() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean isFinal() {
		return true;
	}

	@Override
	public boolean isKeyStoreDependent() {
		return false;
	}
}
