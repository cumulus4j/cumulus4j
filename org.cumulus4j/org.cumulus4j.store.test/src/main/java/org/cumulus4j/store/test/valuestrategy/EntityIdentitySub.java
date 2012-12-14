package org.cumulus4j.store.test.valuestrategy;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class EntityIdentitySub extends EntityIdentity {

	private String field1;

	protected EntityIdentitySub() { }

	public EntityIdentitySub(String name, String field1) {
		super(name);
		this.field1 = field1;
	}

	public String getField1() {
		return field1;
	}
	public void setField1(String field1) {
		this.field1 = field1;
	}
}
