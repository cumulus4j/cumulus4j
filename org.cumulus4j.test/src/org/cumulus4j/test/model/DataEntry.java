package org.cumulus4j.test.model;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION)
// We use single-field-identity, thus no object-id-class:
//   http://www.datanucleus.org/products/accessplatform_3_0/jdo/primary_key.html
//   http://db.apache.org/jdo/api20/apidocs/javax/jdo/identity/SingleFieldIdentity.html
public class DataEntry
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long dataEntryID = -1;

	private byte[] value;

	public long getDataEntryID() {
		return dataEntryID;
	}

	public byte[] getValue() {
		return value;
	}

	public void setValue(byte[] value) {
		this.value = value;
	}
}
