package org.cumulus4j.nightlabsprototype.model;

import javax.jdo.PersistenceManager;

public abstract class IndexEntryFactory<FieldType>
{
	public abstract IndexEntry getIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, FieldType indexKey);
}
