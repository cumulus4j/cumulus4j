package org.cumulus4j.core.model;

import javax.jdo.PersistenceManager;

public class IndexEntryOneToOneRelationHelper
{
	public static IndexEntry getIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, Object indexedDataEntryID)
	{
		return new IndexEntryFactoryLong().getIndexEntry(pm, fieldMeta, indexedDataEntryID);
	}

	public static IndexEntry createIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, Object indexedDataEntryID)
	{
		return new IndexEntryFactoryLong().createIndexEntry(pm, fieldMeta, indexedDataEntryID);
	}
}
