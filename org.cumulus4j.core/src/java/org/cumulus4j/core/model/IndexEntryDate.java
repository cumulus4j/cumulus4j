package org.cumulus4j.core.model;

import java.util.Date;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Unique;

/**
 * <p>
 * Persistent index information for fields of type {@link Date}.
 * </p>
 * <p>
 * This {@link IndexEntry}-subclass is used to index object-references, too. In this case,
 * the values in {@link #getIndexKey() indexKey} are {@link DataEntry#getDataEntryID() dataEntryID}s.
 * </p>
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Unique(members={"fieldMeta", "indexKey"})
public class IndexEntryDate
extends IndexEntry
{
	private Date indexKey;

	@Override
	public Date getIndexKey() {
		return indexKey;
	}

	@Override
	protected void setIndexKey(Object indexKey) {
		this.indexKey = (Date) indexKey;
	}
}
