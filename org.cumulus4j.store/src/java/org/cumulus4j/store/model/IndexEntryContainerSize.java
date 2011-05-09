package org.cumulus4j.store.model;

import java.util.Collection;
import java.util.Map;

import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Unique;

/**
 * <p>
 * Persistent index information for the size of fields of type {@link Collection}/{@link Map}.
 * </p>
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Unique(members={"fieldMeta", "indexKey"})
public class IndexEntryContainerSize
extends IndexEntry
{
	private Long indexKey;

	@Override
	public Long getIndexKey() {
		return indexKey;
	}

	@Override
	protected void setIndexKey(Object indexKey) {
		this.indexKey = (Long) indexKey;
	}
}
