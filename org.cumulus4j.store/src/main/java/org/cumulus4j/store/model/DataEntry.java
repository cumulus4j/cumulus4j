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
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Sequence;
import javax.jdo.annotations.SequenceStrategy;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.listener.StoreCallback;

import org.cumulus4j.store.datastoreversion.command.IntroduceKeyStoreRefID;

/**
 * Persistent container holding an entity's data in <b>encrypted</b> form.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@Sequence(name="DataEntrySequence", datastoreSequence="DataEntrySequence", initialValue=0, strategy=SequenceStrategy.CONTIGUOUS)
@Unique(members={"keyStoreRefID", "classMeta", "objectID"})
@Queries({
	@Query(
			name="getDataEntryByClassMetaAndObjectID",
			value="SELECT UNIQUE WHERE this.keyStoreRefID == :keyStoreRefID && this.classMeta == :classMeta && this.objectID == :objectID"
	),
	@Query(
			name="getDataEntryIDByClassMetaAndObjectID",
			value="SELECT UNIQUE this.dataEntryID WHERE this.keyStoreRefID == :keyStoreRefID && this.classMeta == :classMeta && this.objectID == :objectID"
	),
	@Query(
			name="getDataEntryIDsByClassMetaAndObjectIDNegated",
			value="SELECT this.dataEntryID WHERE this.keyStoreRefID == :keyStoreRefID && this.classMeta == :classMeta && this.objectID != :notThisObjectID"
	)
})
public class DataEntry
implements StoreCallback
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE, sequence="DataEntrySequence")
	private Long dataEntryID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(defaultValue="0")
	private int keyStoreRefID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private ClassMeta classMeta;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String objectID;

	private long keyID = -1;

	private byte[] value;

	/**
	 * Internal constructor. This exists only for JDO and should not be used by application code!
	 */
	protected DataEntry() { }

	/**
	 * Create an instance of <code>DataEntry</code>.
	 * @param classMeta the type of the entity persisted in this container (which must be the entity's concrete type -
	 * <b>not</b> the root-type of the inheritance tree!). See {@link #getClassMeta()} for further details.
	 * @param keyStoreRefID TODO
	 * @param objectID the <code>String</code>-representation of the entity's identifier (aka OID or object-ID).
	 * See {@link #getObjectID()} for further details.
	 */
	public DataEntry(ClassMeta classMeta, int keyStoreRefID, String objectID) {
		this.classMeta = classMeta;
		this.keyStoreRefID = keyStoreRefID;
		this.objectID = objectID;
	}

	/**
	 * Get the single primary key field (= object-identifier) of <code>DataEntry</code>.
	 * @return the object-identifier (= primary key).
	 */
	public long getDataEntryID() {
			if (dataEntryID == null) {
			return -1;
		}
		return dataEntryID;
	}

	/**
	 * <p>
	 * Get the type of the entity persisted in this container.
	 * </p>
	 * <p>
	 * Note, that this is the concrete type of the persisted object and <b>not</b> the root-type of the
	 * persistable hierarchy. For example, if <code>bbb</code> is persisted and <code>bbb</code> is an instance of
	 * class <code>BBB</code> which extends <code>AAA</code>
	 * and both classes are persistable, this will point to class <code>BBB</code> (and <b>not</b> <code>AAA</code>).
	 * </p>
	 * <p>
	 * Therefore, if you want to query all instances of a certain type including subclasses, you have to
	 * ask for the sub-classes via {@link org.datanucleus.store.StoreManager#getSubClassesForClass(String, boolean, org.datanucleus.ClassLoaderResolver)}
	 * first and then query for all these classes individually.
	 * </p>
	 * @return the type of the entity.
	 */
	public ClassMeta getClassMeta() {
		return classMeta;
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
	 * @deprecated Never call this method! It exists only for downward compatibility
	 * (needs to be accessible by {@link IntroduceKeyStoreRefID}).
	 */
	@Deprecated
	public void setKeyStoreRefID(int keyStoreRefID) {
		this.keyStoreRefID = keyStoreRefID;
	}

	/**
	 * <p>
	 * Get the <code>String</code>-representation of the entity's identifier.
	 * </p>
	 * <p>
	 * For JDO, please read the following (and related) documentation:
	 * </p>
	 * <ul>
	 * 	<li><a target="_blank" href="http://www.datanucleus.org/products/accessplatform_3_0/jdo/application_identity.html">JDO Mapping / Identity / Application Identity</a></li>
	 * 	<li><a target="_blank" href="http://www.datanucleus.org/products/accessplatform_3_0/jdo/datastore_identity.html">JDO Mapping / Identity / Datastore Identity</a></li>
	 * </ul>
	 * <p>
	 * For JPA, please read the following (and related) documentation:
	 * </p>
	 * <ul>
	 * 	<li><a target="_blank" href="http://www.datanucleus.org/products/accessplatform_3_0/jpa/application_identity.html">JPA Mapping / Identity / Application Identity</a></li>
	 * 	<li><a target="_blank" href="http://www.datanucleus.org/products/accessplatform_3_0/jpa/datastore_identity.html">JPA Mapping / Identity / Datastore Identity</a></li>
	 * </ul>
	 *
	 * @return the OID in String-form
	 * (e.g. the result of <code><a target="_blank" href="http://db.apache.org/jdo/api30/apidocs/javax/jdo/JDOHelper.html#getObjectId%28java.lang.Object%29">JDOHelper.getObjectId</a>(entity).toString()</code>
	 * when using JDO).
	 */
	public String getObjectID() {
		return objectID;
	}

	/**
	 * Get the identifier of the encryption-key used to encrypt the {@link #getValue() value}.
	 * @return the encryption-key used to encrypt this <code>DataEntry</code>'s contents.
	 * @see #setKeyID(long)
	 */
	public long getKeyID() {
		return keyID;
	}

	/**
	 * Set the identifier of the encryption-key used to encrypt the {@link #getValue() value}.
	 * @param keyID the encryption-key used to encrypt this <code>DataEntry</code>'s contents.
	 * @see #getKeyID()
	 */
	public void setKeyID(long keyID)
	{
		if (keyID < 0)
			throw new IllegalArgumentException("keyID < 0");

		this.keyID = keyID;
	}

	/**
	 * Get the <b>encrypted</b> data of an entity. The entity is transformed ("made flat") into an {@link ObjectContainer}
	 * which is then serialised using Java native serialisation and finally encrypted.
	 * @return the <b>encrypted</b> serialised data of an {@link ObjectContainer} holding the entity's data.
	 * @see #setValue(byte[])
	 */
	public byte[] getValue() {
		return value;
	}

	/**
	 * Set the <b>encrypted</b> data of an entity.
	 * @param value the <b>encrypted</b> serialised data of an {@link ObjectContainer} holding the entity's data.
	 * @see #getValue()
	 */
	public void setValue(byte[] value) {
		this.value = value;
	}

	@Override
	public int hashCode() {
		Long id = dataEntryID;
		if(id == null){
			id = -1L;
		}
		return (int) (id ^ (id >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		DataEntry other = (DataEntry) obj;
		return this.dataEntryID == other.dataEntryID;
	}

	@Override
	public void jdoPreStore()
	{
		// We replace 'this.classMeta' by a persistent version, because it is a detached object
		// which slows down the storing process immensely, as it is unnecessarily attached.
		// Attaching an object is an expensive operation and we neither want nor need to
		// update the ClassMeta object when persisting a new DataEntry.
		PersistenceManager pm = JDOHelper.getPersistenceManager(this);
		Object classMetaID = JDOHelper.getObjectId(classMeta);
		classMeta = (ClassMeta) pm.getObjectById(classMetaID);
	}
}
