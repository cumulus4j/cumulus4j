package org.cumulus4j.store.model;

import java.util.UUID;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Unique;

/**
 * <p>
 * Persistent index information for fields of type {@link UUID}.
 * </p>
 * <p>
 * This {@link IndexEntry}-subclass is used to index object-references, too. In this case,
 * the values in {@link #getIndexKey() indexKey} are {@link DataEntry#getDataEntryID() dataEntryID}s.
 * </p>
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Unique(members={"fieldMeta", "indexKey"})
public class IndexEntryUUID
extends IndexEntry
{
	@Persistent
	private UUID indexKey;

	@Override
	public UUID getIndexKey() {
		return indexKey;
	}

	@Override
	protected void setIndexKey(Object indexKey) {
		this.indexKey = (UUID) indexKey;
	}
}
