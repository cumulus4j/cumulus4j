package org.cumulus4j.test.collection.mappedby;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Element1
{
	protected Element1() { }

	public Element1(String name) {
		setName(name);
	}

	private String name;

	private Element1SetOwner owner;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Element1SetOwner getOwner() {
		return owner;
	}
	protected void setOwner(Element1SetOwner owner) {
		this.owner = owner;
	}
}
