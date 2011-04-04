package org.cumulus4j.test.collection.mappedby;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Element2
{
	protected Element2() { }

	public Element2(String key, String name) {
		this.key = key;
		setName(name);
	}

	private String key;

	private String name;

	private Element2MapOwner owner;

	public String getKey() {
		return key;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public Element2MapOwner getOwner() {
		return owner;
	}
	protected void setOwner(Element2MapOwner owner) {
		this.owner = owner;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[key=\"" + key + "\", name=\"" + name + "\"" + ", owner=" + owner + ']';
	}
}
