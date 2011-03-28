package org.cumulus4j.test.onetoone.mappedby;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class LevelB
{
	@Persistent(mappedBy="levelB")
	private LevelA levelA;

	private String name;

	public LevelA getLevelA() {
		return levelA;
	}
	protected void setLevelA(LevelA levelA) {
		this.levelA = levelA;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
