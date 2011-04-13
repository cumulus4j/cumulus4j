package org.cumulus4j.core.model;

/**
 * 
 */
public class IndexEntryFactoryDate extends IndexEntryFactory {

	@Override
	public Class<? extends IndexEntry> getIndexEntryClass() {
		return IndexEntryDate.class;
	}

}
