/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.model;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.NotPersistent;
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
@PersistenceCapable(identityType=IdentityType.APPLICATION)
@Inheritance(strategy=InheritanceStrategy.SUBCLASS_TABLE)
@Version(strategy=VersionStrategy.VERSION_NUMBER)
//@Unique(members={"keyStoreRefID", "fieldMeta_fieldID", "classMeta_classID", "indexKey"})
public abstract class IndexEntry
implements StoreCallback
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE, sequence="IndexEntrySequence")
	private Long indexEntryID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(defaultValue="0")
	private int keyStoreRefID;

	@NotPersistent
	private FieldMeta fieldMeta;

	@Column(name="fieldMeta_fieldID_oid") // for downward-compatibility
	private long fieldMeta_fieldID = -1;

	@NotPersistent
	private ClassMeta classMeta;

	@Column(name="classMeta_classID_oid", // not necessary for downward-compatibility (new field), but for the sake of consistency
			defaultValue="-1")
	private long classMeta_classID = -1;

	private long keyID = -1;

	/** DataEntryIDs for this indexed key. */
	private byte[] indexValue;

	/**
	 * Get the single primary key field (= object-identifier) of this <code>IndexEntry</code>.
	 *
	 * @return the object-identifier (= primary key).
	 */
	public long getIndexEntryID() {
		return indexEntryID == null ? -1 : indexEntryID;
	}

	/**
	 * <p>
	 * Get the descriptor of the indexed field.
	 * </p>
	 * <p>
	 * Every <code>IndexEntry</code> instance belongs to one field or a part of the field (e.g. a <code>Map</code>'s key).
	 * </p>
	 * @return the descriptor of the indexed field.
	 */
	public FieldMeta getFieldMeta() {
		if (fieldMeta == null) {
			if (fieldMeta_fieldID < 0)
				return null;

			fieldMeta = new FieldMetaDAO(getPersistenceManager()).getFieldMeta(fieldMeta_fieldID, true);
		}
		return fieldMeta;
	}

	protected void setFieldMeta(FieldMeta fieldMeta) {
		if (this.fieldMeta != null && !this.fieldMeta.equals(fieldMeta))
			throw new IllegalStateException("The property fieldMeta cannot be modified after being set once!");

		this.fieldMeta = fieldMeta;
		this.fieldMeta_fieldID = fieldMeta == null ? -1 : fieldMeta.getFieldID();
		if (this.fieldMeta_fieldID < 0)
			throw new IllegalStateException("fieldMeta not persisted yet: " + fieldMeta);
	}

	/**
	 * Get the {@link ClassMeta} of the concrete type of the instance containing the field.
	 * <p>
	 * If a field is declared in a super-class, all sub-classes have it, too. But when querying
	 * instances of a sub-class (either as candidate-class or in a relation (as concrete type of
	 * the field/property), only this given sub-class and its sub-classes should be found.
	 * <p>
	 * The <code>ClassMeta</code> here is either the same as {@link FieldMeta#getClassMeta() fieldMeta.classMeta}
	 * (if it is an instance of the class declaring the field) or a <code>ClassMeta</code> of a sub-class of
	 * <code>fieldMeta.classMeta</code>.
	 * @return the {@link ClassMeta} of the concrete type of the instance containing the field.
	 */
	public ClassMeta getClassMeta() {
		if (classMeta == null) {
			if (classMeta_classID < 0)
				return null;

			classMeta = new ClassMetaDAO(getPersistenceManager()).getClassMeta(classMeta_classID, true);
		}
		return classMeta;
	}

	protected PersistenceManager getPersistenceManager() {
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		if (pm == null) {
			throw new IllegalStateException("JDOHelper.getPersistenceManager(this) returned null! " + this);
		}
		return pm;
	}

	public void setClassMeta(ClassMeta classMeta) {
		if (this.classMeta != null && !this.classMeta.equals(classMeta))
			throw new IllegalStateException("The property classMeta cannot be modified after being set once!");

		this.classMeta = classMeta;
		this.classMeta_classID = classMeta == null ? -1 : classMeta.getClassID();
		if (this.classMeta_classID < 0)
			throw new IllegalStateException("classMeta not persisted yet: " + classMeta);
	}

	/**
	 * Get the numeric identifier of the key store. The key store's String-ID is mapped to this numeric ID
	 * via {@link KeyStoreRef} instances.
	 * @return the numeric identifier of the key store.
	 */
	public int getKeyStoreRefID() {
		return keyStoreRefID;
	}

	/**
	 * Set the numeric identifier of the key store.
	 * @param keyStoreRefID the numeric identifier of the key store.
	 */
	public void setKeyStoreRefID(int keyStoreRefID) {
		this.keyStoreRefID = keyStoreRefID;
	}

	/**
	 * Get the value which is indexed by this instance. It serves as 2nd part of the unique key together
	 * with the property {@link #getFieldMeta() fieldMeta}.
	 * @return the key.
	 */
	public abstract Object getIndexKey();

	protected abstract void setIndexKey(Object indexKey);

	/**
	 * Get the identifier of the encryption-key used to encrypt the {@link #getIndexValue() indexValue}.
	 * @return the encryption-key used to encrypt this <code>IndexEntry</code>'s contents.
	 * @see #setKeyID(long)
	 */
	public long getKeyID() {
		return keyID;
	}

	/**
	 * Set the identifier of the encryption-key used to encrypt the {@link #getIndexValue() indexValue}.
	 * @param keyID the encryption-key used to encrypt this <code>IndexEntry</code>'s contents.
	 * @see #getKeyID()
	 */
	public void setKeyID(long keyID)
	{
		if (keyID < 0)
			throw new IllegalArgumentException("keyID < 0");

		this.keyID = keyID;
	}

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
		long indexEntryID = getIndexEntryID();
		return (int) (indexEntryID ^ (indexEntryID >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		IndexEntry other = (IndexEntry) obj;
		return this.getIndexEntryID() < 0 ? false : this.getIndexEntryID() == other.getIndexEntryID();
	}

	@Override
	public void jdoPreStore()
	{
		if (fieldMeta_fieldID < 0)
			throw new IllegalStateException("fieldMeta_fieldID < 0");

		// Must allow this for updatability. At least temporarily. TODO find a better solution.
		if (classMeta_classID < 0)
			throw new IllegalStateException("classMeta_classID < 0");
	}
}
