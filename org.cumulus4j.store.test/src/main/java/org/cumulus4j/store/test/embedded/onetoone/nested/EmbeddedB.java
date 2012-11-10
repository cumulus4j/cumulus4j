package org.cumulus4j.store.test.embedded.onetoone.nested;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class EmbeddedB {

	private String name;

	public EmbeddedB() { }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

}
