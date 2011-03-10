package org.cumulus4j.core.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
// TODO The following "@Unique" does not work at all in Derby and only with special syntax in MySQL
// (note the length "(255)" behind the "indexkey"):
//    ALTER TABLE indexentrystringlong ADD CONSTRAINT UNIQUE KEY (indexkey(255), fieldmeta_fieldid_oid)
// => file a bug in DataNucleus' issue tracker (should log a warning, if the underlying DB doesn't support
// it and should use RDBMS-specific syntax, if necessary).
//@Unique(members={"fieldMeta", "indexKey"})
public class IndexEntryStringLong
extends IndexEntry
{
	@Column(jdbcType="CLOB")
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
