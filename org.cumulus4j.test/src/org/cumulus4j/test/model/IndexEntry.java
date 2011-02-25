package org.cumulus4j.test.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Index;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION)
public class IndexEntry
{
	@PrimaryKey
	private long indexEntryID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Index(name="mainUniqueKey", unique="true")
	private FieldMeta fieldMeta;

	@Index(name="mainUniqueKey", unique="true")
	private long indexKeyLong;

	@Column(sqlType="TEXT")
	@Index(name="mainUniqueKey", unique="true")
	private String indexKeyString;

	protected IndexEntry() { }

	public IndexEntry(FieldMeta fieldMeta, long indexKeyLong, String indexKeyString)
	{
		if (fieldMeta == null)
			throw new IllegalArgumentException("fieldMeta == null");

		this.fieldMeta = fieldMeta;
		this.indexKeyLong = indexKeyLong;
		this.indexKeyString = indexKeyString;
	}

	public long getIndexEntryID() {
		return indexEntryID;
	}

	public FieldMeta getFieldMeta() {
		return fieldMeta;
	}

	public long getIndexKeyLong() {
		return indexKeyLong;
	}

	public String getIndexKeyString() {
		return indexKeyString;
	}

}
