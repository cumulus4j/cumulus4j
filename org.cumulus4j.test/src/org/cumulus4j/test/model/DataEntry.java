package org.cumulus4j.test.model;

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
})
public class DataEntry
{
	public static DataEntry getDataEntry(PersistenceManager pm, ClassMeta classMeta, String objectID)
	{
		javax.jdo.Query q = pm.newNamedQuery(DataEntry.class, "getDataEntryByClassMetaAndObjectID");
		return (DataEntry) q.execute(classMeta, objectID);
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

	public DataEntry(ClassMeta classMeta, String objectID, byte[] value) {
		this.classMeta = classMeta;
		this.objectID = objectID;
		setValue(value);
	}

	public long getDataEntryID() {
		return dataEntryID;
	}

	public ClassMeta getClassMeta() {
		return classMeta;
	}

	public String getObjectID() {
		return objectID;
	}

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
