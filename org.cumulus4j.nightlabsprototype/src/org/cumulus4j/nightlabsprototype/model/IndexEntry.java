package org.cumulus4j.nightlabsprototype.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

/**
 * Persistent index information with encrypted pointers to {@link DataEntry} instances.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUBCLASS_TABLE)
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
public abstract class IndexEntry<FieldType>
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long indexEntryID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private FieldMeta fieldMeta;

	private byte[] indexValue;

	protected IndexEntry() { }

	protected IndexEntry(FieldMeta fieldMeta)
	{
		if (fieldMeta == null)
			throw new IllegalArgumentException("fieldMeta == null");

		this.fieldMeta = fieldMeta;
	}

	/**
	 * Get the single primary key field (= object-identifier) of this <code>IndexEntry</code>.
	 *
	 * @return the object-identifier (= primary key).
	 */
	public long getIndexEntryID() {
		return indexEntryID;
	}

	public FieldMeta getFieldMeta() {
		return fieldMeta;
	}

	public abstract FieldType getIndexKey();

	/**
	 * Get the <b>encrypted</b> pointers to {@link DataEntry}. After decrypting
	 * this byte array, you can pass it to {@link IndexValue#IndexValue(byte[])}.
	 *
	 * @return the <b>encrypted</b> pointers to {@link DataEntry}s.
	 */
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
		IndexEntry<?> other = (IndexEntry<?>) obj;
		return this.indexEntryID == other.indexEntryID;
	}
}
