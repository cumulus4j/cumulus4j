package org.cumulus4j.store.model;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class EmbeddedObjectContainer extends ObjectContainer {
	private static final long serialVersionUID = 1L;

//	public static final String ASSOCIATED_VALUE = "embeddedObjectContainer";

	private long classID;

	public EmbeddedObjectContainer(long classID, ObjectContainer objectContainerToBeCopied) {
		this.classID = classID;
		if (objectContainerToBeCopied != null) {
			setVersion(objectContainerToBeCopied.getVersion());
			setValues(objectContainerToBeCopied.getFieldID2value());
		}
	}

	public long getClassID() {
		return classID;
	}

}
