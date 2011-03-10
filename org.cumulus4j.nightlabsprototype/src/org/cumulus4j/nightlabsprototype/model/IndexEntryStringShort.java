package org.cumulus4j.nightlabsprototype.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Unique;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Unique(members={"fieldMeta", "indexKey"})
public class IndexEntryStringShort
extends IndexEntry
{
	@Column(length=255)
	private String indexKey;

	@Override
	public String getIndexKey() {
		return indexKey;
	}

	@Override
	protected void setIndexKey(Object indexKey) {
		this.indexKey = (String) indexKey;
	}
}
