package org.cumulus4j.nightlabsprototype.model;

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
	)
//	,
//	@Query(
//			name="getObjectIDsByClassMetas",
//			value="SELECT this.objectID WHERE :classMetas.contains(this.classMeta) PARAMETERS java.util.Set classMetas"
//	)
})
public class DataEntry
{
	public static DataEntry getDataEntry(PersistenceManager pm, ClassMeta classMeta, String objectID)
	{
		javax.jdo.Query q = pm.newNamedQuery(DataEntry.class, "getDataEntryByClassMetaAndObjectID");
		return (DataEntry) q.execute(classMeta, objectID);
	}

//	public static Collection<String> getObjectIDs(PersistenceManager pm, Set<ClassMeta> classMetas)
//	{
//		javax.jdo.Query q = pm.newNamedQuery(DataEntry.class, "getDataEntriesByClassMetas");
//		@SuppressWarnings("unchecked")
//		Collection<String> c = (Collection<String>) q.execute(classMetas);
//		c = new ArrayList<String>(c);
//		q.closeAll();
//		return c;
//	}

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
	 * Get the type of the entity persisted in this container.
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
	 */
	public byte[] getValue() {
		return value;
	}

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
}
