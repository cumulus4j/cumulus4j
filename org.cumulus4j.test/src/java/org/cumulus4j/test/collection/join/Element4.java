package org.cumulus4j.test.collection.join;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Element4
{
	protected Element4() { }

	public Element4(String name) {
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
