package org.cumulus4j.core.model;

import javax.jdo.JDOHelper;
import javax.jdo.JDOObjectNotFoundException;
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
import javax.jdo.annotations.Unique;
import javax.jdo.identity.LongIdentity;
import javax.jdo.listener.StoreCallback;

/**
 * Persistent container holding an entity's data in <b>encrypted</b> form.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
// We use single-field-identity, thus no object-id-class:
//   http://www.datanucleus.org/products/accessplatform_3_0/jdo/primary_key.html
//   http://db.apache.org/jdo/api20/apidocs/javax/jdo/identity/SingleFieldIdentity.html
@Unique(name="DataEntry_classMeta_objectID", members={"classMeta", "objectID"})
@Queries({
	@Query(
			name="getDataEntryByClassMetaAndObjectID",
			value="SELECT UNIQUE WHERE this.classMeta == :classMeta && this.objectID == :objectID"
	),
	@Query(
			name="getDataEntryIDByClassMetaAndObjectID",
			value="SELECT UNIQUE this.dataEntryID WHERE this.classMeta == :classMeta && this.objectID == :objectID"
	)
})
public class DataEntry
implements StoreCallback
{
	public static DataEntry getDataEntry(PersistenceManager pm, long dataEntryID)
	{
		DataEntry dataEntry;
		try {
			dataEntry = (DataEntry) pm.getObjectById(new LongIdentity(DataEntry.class, dataEntryID));
		} catch (JDOObjectNotFoundException x) {
			dataEntry = null;
		}
		return dataEntry;
	}

	public static DataEntry getDataEntry(PersistenceManager pm, ClassMeta classMeta, String objectID)
	{
		javax.jdo.Query q = pm.newNamedQuery(DataEntry.class, "getDataEntryByClassMetaAndObjectID");
		return (DataEntry) q.execute(classMeta, objectID);
	}

	public static Long getDataEntryID(PersistenceManager pm, ClassMeta classMeta, String objectID)
	{
		javax.jdo.Query q = pm.newNamedQuery(DataEntry.class, "getDataEntryIDByClassMetaAndObjectID");
		return (Long) q.execute(classMeta, objectID);
	}

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long dataEntryID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private ClassMeta classMeta;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Column(length=255)
	private String objectID;

	private byte[] value;

	protected DataEntry() { }

	public DataEntry(ClassMeta classMeta, String objectID) {
		this.classMeta = classMeta;
		this.objectID = objectID;
	}

	/**
	 * Get the single primary key field (= object-identifier) of <code>DataEntry</code>.
	 * @return the object-identifier (= primary key).
	 */
	public long getDataEntryID() {
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
	 * Get the String-representation of the entity's identifier.
	 * @return the OID in String-form.
	 */
	public String getObjectID() {
		return objectID;
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
		return (int) (dataEntryID ^ (dataEntryID >>> 32));
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
