package org.cumulus4j.test.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION)
//@Unique(members={"fieldMeta", "indexKeyLong", "indexKeyString"})
public class IndexEntry
{
	@PrimaryKey
	private long indexEntryID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private FieldMeta fieldMeta;

	private long indexKeyLong;

	@Column(jdbcType="CLOB")
	private String indexKeyString;

	private byte[] indexValue;

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

	public byte[] getIndexValue() {
		return indexValue;
	}

	public void setIndexValue(byte[] indexValue) {
		this.indexValue = indexValue;
	}
}
