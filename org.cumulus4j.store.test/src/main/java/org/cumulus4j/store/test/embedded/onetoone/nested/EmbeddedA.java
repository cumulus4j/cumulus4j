package org.cumulus4j.store.test.embedded.onetoone.nested;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class EmbeddedA {

	private String name;

	@Embedded
	private EmbeddedA0 embeddedA0;

	@Embedded
	private EmbeddedA1 embeddedA1;

	public EmbeddedA() { }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public EmbeddedA0 getEmbeddedA0() {
		return embeddedA0;
	}
	public void setEmbeddedA0(EmbeddedA0 embeddedA0) {
		this.embeddedA0 = embeddedA0;
	}

	public EmbeddedA1 getEmbeddedA1() {
		return embeddedA1;
	}
	public void setEmbeddedA1(EmbeddedA1 embeddedA1) {
		this.embeddedA1 = embeddedA1;
	}

}
