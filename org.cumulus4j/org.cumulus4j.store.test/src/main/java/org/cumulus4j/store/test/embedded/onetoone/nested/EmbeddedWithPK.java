package org.cumulus4j.store.test.embedded.onetoone.nested;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class EmbeddedWithPK {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    private Long id1;

	private String name;

	@Embedded
	private EmbeddedWithPKSub embeddedWithPKSub;

	public EmbeddedWithPK() { }

	public Long getId1() {
		return id1;
	}
	public void setId1(Long id1) {
		this.id1 = id1;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public EmbeddedWithPKSub getEmbeddedWithPKSub() {
		return embeddedWithPKSub;
	}
	public void setEmbeddedWithPKSub(EmbeddedWithPKSub embeddedWithPKSub) {
		this.embeddedWithPKSub = embeddedWithPKSub;
	}
}
