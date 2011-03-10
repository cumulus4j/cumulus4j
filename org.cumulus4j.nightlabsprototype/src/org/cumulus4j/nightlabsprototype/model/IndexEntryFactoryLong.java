package org.cumulus4j.nightlabsprototype.model;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class IndexEntryFactoryLong extends IndexEntryFactory {

	@Override
	public Class<? extends IndexEntry> getIndexEntryClass() {
		return IndexEntryLong.class;
	}

}
