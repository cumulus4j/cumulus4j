package org.cumulus4j.store.test.embedded.onetomany;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class Embedded1ToNElement {

	private String name;

	public Embedded1ToNElement() { }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
