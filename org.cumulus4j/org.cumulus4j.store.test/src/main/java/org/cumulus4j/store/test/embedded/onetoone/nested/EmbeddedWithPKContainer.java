package org.cumulus4j.store.test.embedded.onetoone.nested;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class EmbeddedWithPKContainer {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    private Long id0;

	private String name;

	@Embedded(nullIndicatorColumn="id0")
	private EmbeddedWithPK embeddedWithPK;

//	@Embedded
//	private EmbeddedA againEmbeddedA;
//
//	@Embedded
//	private EmbeddedB embeddedB;

	public EmbeddedWithPKContainer() { }

	public Long getId0() {
		return id0;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public EmbeddedWithPK getEmbeddedWithPK() {
		return embeddedWithPK;
	}
	public void setEmbeddedWithPK(EmbeddedWithPK embeddedWithPK) {
		this.embeddedWithPK = embeddedWithPK;
	}
}
