package org.cumulus4j.store.test.embedded.onetoone.nested;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class EmbeddedContainer {

	private String name;

	@Embedded(nullIndicatorColumn="name")
	private EmbeddedA embeddedA;

	@Embedded
	private EmbeddedA againEmbeddedA;

	@Embedded
	private EmbeddedB embeddedB;

	public EmbeddedContainer() { }

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public EmbeddedA getEmbeddedA() {
		return embeddedA;
	}
	public void setEmbeddedA(EmbeddedA embeddedA) {
		this.embeddedA = embeddedA;
	}

	public EmbeddedB getEmbeddedB() {
		return embeddedB;
	}
	public void setEmbeddedB(EmbeddedB embeddedB) {
		this.embeddedB = embeddedB;
	}

	public EmbeddedA getAgainEmbeddedA() {
		return againEmbeddedA;
	}
	public void setAgainEmbeddedA(EmbeddedA againEmbeddedA) {
		this.againEmbeddedA = againEmbeddedA;
	}
}
