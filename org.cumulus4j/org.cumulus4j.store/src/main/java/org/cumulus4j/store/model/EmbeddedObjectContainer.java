package org.cumulus4j.store.model;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class EmbeddedObjectContainer extends ObjectContainer {
	private static final long serialVersionUID = 1L;

	private long classID;

	public EmbeddedObjectContainer(long classID) {
		this.classID = classID;
	}

	public long getClassID() {
		return classID;
	}
}
