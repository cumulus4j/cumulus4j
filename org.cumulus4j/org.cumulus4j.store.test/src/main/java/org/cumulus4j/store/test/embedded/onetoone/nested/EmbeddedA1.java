package org.cumulus4j.store.test.embedded.onetoone.nested;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class EmbeddedA1 {

	private String name;

	public EmbeddedA1() { }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
