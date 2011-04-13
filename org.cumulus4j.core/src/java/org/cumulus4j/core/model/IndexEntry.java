package org.cumulus4j.core.model;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.listener.StoreCallback;

/**
 * <p>
 * Persistent index information with <b>encrypted</b> pointers to {@link DataEntry}s.
 * </p>
 * <p>
 * Since the index is type-specific, there are sub-classes for each data type. One
 * {@link IndexEntry} instance is used for each distinct value of one certain field.
 * Therefore, the field (represented by the property {@link #getFieldMeta() fieldMeta})
 * and the value together form a unique key of <code>IndexEntry</code> - thus the value
 * is represented by the property {@link #getIndexKey() indexKey}.
 * </p>
 * <p>
 * Example:
 * </p>
 * <pre>
 * // persistent class:
 * &#64;PersistenceCapable
 * class Person
 * {
 * 	public Person(String firstName, String lastName) {
 * 		this.firstName = firstName;
 * 		this.lastName = lastName;
 * 	}
 *
 * 	private String firstName;
 * 	private String lastName;
 *
 * 	// ...
 * }
 *
 * class SomeTest
 * {
 * 	&#64;Test
 * 	public void persistPersons()
 * 	{
 * 		pm.makePersistent(new Person("Alice", "Müller"));
 * 		pm.makePersistent(new Person("Alice", "Meier"));
 * 	}
 * }
 * </pre>
 * <p>
 * After running this test, there would be three instances of {@link IndexEntryStringShort} in the database
 * indexing the values "Alice", "Müller" and "Meier". Note, that "Alice" occurs only once in the index, even though
 * there are two <code>Person</code> instances using it. The two persons would be referenced from the one index-entry
 * via {@link #getIndexValue()}.
 * </p>
 *
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.SUBCLASS_TABLE)
@Version(strategy=VersionStrategy.VERSION_NUMBER)
//@Unique(members={"fieldMeta", "indexKeyDouble", "indexKeyLong", "indexKeyString"})
public abstract class IndexEntry
implements StoreCallback
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long indexEntryID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private FieldMeta fieldMeta;

	private long keyID = -1;

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

	public long getKeyID() {
		return keyID;
	}

	public void setKeyID(long keyID)
	{
		if (keyID < 0)
			throw new IllegalArgumentException("keyID < 0");

		this.keyID = keyID;
	}

	/**
	 * Get the value which is indexed by this instance. It serves as 2nd part of the unique key together
	 * with the property {@link #getFieldMeta() fieldMeta}.
	 * @return the key.
	 */
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

	@Override
	public void jdoPreStore()
	{
		// See: DataEntry#jdoPreStore() - the same applies here to 'this.fieldMeta'.
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		Object fieldMetaID = JDOHelper.getObjectId(fieldMeta);
		fieldMeta = (FieldMeta) pm.getObjectById(fieldMetaID);
	}
}
