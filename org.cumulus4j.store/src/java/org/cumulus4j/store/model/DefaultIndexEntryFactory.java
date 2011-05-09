package org.cumulus4j.store.model;

/**
 * Default index entry factory, using the passed in index class for what it generates.
 */
public class DefaultIndexEntryFactory extends IndexEntryFactory {

	final Class<? extends IndexEntry> indexEntryClass;

	public DefaultIndexEntryFactory(Class<? extends IndexEntry> idxClass) {
		this.indexEntryClass = idxClass;
	}

	/* (non-Javadoc)
	 * @see org.cumulus4j.store.model.IndexEntryFactory#getIndexEntryClass()
	 */
	@Override
	public Class<? extends IndexEntry> getIndexEntryClass() {
		return indexEntryClass;
	}
}