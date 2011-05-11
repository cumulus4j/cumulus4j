package org.cumulus4j.store.test.metadata;

import javax.jdo.annotations.Extension;
import javax.jdo.annotations.PersistenceCapable;

import org.cumulus4j.annotation.NotQueryable;

@PersistenceCapable
public class Entity1
{
	protected Entity1() { }

	public Entity1(String name, String field1, String field2, String field3) {
		setName(name);
		setField1(field1);
		setField2(field2);
		setField3(field3);
	}

	private String name;

	private String field1;

	@NotQueryable
	private String field2;

	@Extension(vendorName="datanucleus", key="queryable", value="false")
	private String field3;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getField1() {
		return field1;
	}
	public void setField1(String field1) {
		this.field1 = field1;
	}
	public String getField2() {
		return field2;
	}
	public void setField2(String field2) {
		this.field2 = field2;
	}
	public String getField3() {
		return field3;
	}
	public void setField3(String field3) {
		this.field3 = field3;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
