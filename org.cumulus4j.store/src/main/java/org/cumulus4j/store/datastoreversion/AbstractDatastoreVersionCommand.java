package org.cumulus4j.store.datastoreversion;


public abstract class AbstractDatastoreVersionCommand implements DatastoreVersionCommand {

	@Override
	public String getDatastoreVersionID() {
		return this.getClass().getSimpleName();
	}

	@Override
	public boolean isFinal() {
		return true;
	}

}
