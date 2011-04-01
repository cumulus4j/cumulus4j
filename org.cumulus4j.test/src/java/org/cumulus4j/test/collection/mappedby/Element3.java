package org.cumulus4j.test.collection.mappedby;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Element3
{
	protected Element3() { }

	public Element3(String value, String name) {
		this.value = value;
		setName(name);
	}

	private String value;

	private String name;

	private Element3MapOwner owner;

	public String getValue() {
		return value;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Element3MapOwner getOwner() {
		return owner;
	}
	protected void setOwner(Element3MapOwner owner) {
		this.owner = owner;
	}
}
