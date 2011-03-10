package org.cumulus4j.core.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Persistent index information with encrypted pointers to {@link DataEntry} instances.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUBCLASS_TABLE)
//@Unique(members={"fieldMeta", "indexKeyDouble", "indexKeyLong", "indexKeyString"})
public abstract class IndexEntry
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long indexEntryID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private FieldMeta fieldMeta;

	private byte[] indexValue;

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

	protected void setFieldMeta(FieldMeta fieldMeta) {
		if (this.fieldMeta != null && !this.fieldMeta.equals(fieldMeta))
			throw new IllegalStateException("The property fieldMeta cannot be modified after being set once!");

		this.fieldMeta = fieldMeta;
	}

	public abstract Object getIndexKey();

	protected abstract void setIndexKey(Object indexKey);

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
		IndexEntry other = (IndexEntry) obj;
		return this.indexEntryID == other.indexEntryID;
	}
}
