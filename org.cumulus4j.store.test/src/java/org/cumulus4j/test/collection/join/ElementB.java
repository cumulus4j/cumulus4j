package org.cumulus4j.test.collection.join;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class ElementB
{
	protected ElementB() { }

	public ElementB(String name) {
		setName(name);
	}

	private String name;

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
