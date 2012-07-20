package org.cumulus4j.store.datastoreversion.command;

import org.cumulus4j.store.datastoreversion.DatastoreVersionCommand;

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
