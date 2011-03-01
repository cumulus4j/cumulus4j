package org.cumulus4j.nightlabsprototype.model;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
//@Unique(members={"fieldMeta", "indexKeyDouble", "indexKeyLong", "indexKeyString"})
@Queries({
	@Query(
			name="getIndexEntryByUniqueKeyFields",
			value="SELECT UNIQUE WHERE " +
					"this.fieldMeta == :fieldMeta && " +
					"this.indexKeyDouble == :indexKeyDouble && " +
					"this.indexKeyLong == :indexKeyLong && " +
					"this.indexKeyString == :indexKeyString"
	)
})
public class IndexEntry
{
	private static IndexEntry getIndexEntry(
			PersistenceManager pm,
			FieldMeta fieldMeta,
			Double indexKeyDouble, Long indexKeyLong, String indexKeyString
	)
	{
		javax.jdo.Query q = pm.newNamedQuery(IndexEntry.class, "getIndexEntryByUniqueKeyFields");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("fieldMeta", fieldMeta);
		params.put("indexKeyDouble", indexKeyDouble);
		params.put("indexKeyLong", indexKeyLong);
		params.put("indexKeyString", indexKeyString);
		return (IndexEntry) q.executeWithMap(params);
	}

	public static IndexEntry getIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, Double indexKeyDouble)
	{
		return getIndexEntry(pm, fieldMeta, indexKeyDouble, null, null);
	}

	public static IndexEntry getIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, Long indexKeyLong)
	{
		return getIndexEntry(pm, fieldMeta, null, indexKeyLong, null);
	}

	public static IndexEntry getIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, String indexKeyString)
	{
		return getIndexEntry(pm, fieldMeta, null, null, indexKeyString);
	}

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long indexEntryID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private FieldMeta fieldMeta;

	private Double indexKeyDouble;

	private Long indexKeyLong;

	@Column(jdbcType="CLOB")
	private String indexKeyString;

	private byte[] indexValue;

	protected IndexEntry() { }

	public IndexEntry(FieldMeta fieldMeta, Double indexKeyDouble)
	{
		this(fieldMeta, indexKeyDouble, null, null);
	}

	public IndexEntry(FieldMeta fieldMeta, Long indexKeyLong)
	{
		this(fieldMeta, null, indexKeyLong, null);
	}

	public IndexEntry(FieldMeta fieldMeta, String indexKeyString)
	{
		this(fieldMeta, null, null, indexKeyString);
	}

	private IndexEntry(FieldMeta fieldMeta, Double indexKeyDouble, Long indexKeyLong, String indexKeyString)
	{
		if (fieldMeta == null)
			throw new IllegalArgumentException("fieldMeta == null");

		this.fieldMeta = fieldMeta;
		this.indexKeyDouble = indexKeyDouble;
		this.indexKeyLong = indexKeyLong;
		this.indexKeyString = indexKeyString;
	}

	public long getIndexEntryID() {
		return indexEntryID;
	}

	public FieldMeta getFieldMeta() {
		return fieldMeta;
	}

	public Double getIndexKeyDouble() {
		return indexKeyDouble;
	}

	public Long getIndexKeyLong() {
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

	@Override
	public int hashCode() {
		return (int) (indexEntryID ^ (indexEntryID >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		IndexEntry other = (IndexEntry) obj;
		return this.indexEntryID == other.indexEntryID;
	}
}
