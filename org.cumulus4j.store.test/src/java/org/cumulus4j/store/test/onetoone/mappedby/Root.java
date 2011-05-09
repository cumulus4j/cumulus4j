package org.cumulus4j.store.test.onetoone.mappedby;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class Root
{
	@Persistent(mappedBy="root")
	private LevelA levelA;

	private String name;

	public LevelA getLevelA() {
		return levelA;
	}

	public void setLevelA(LevelA levelA)
	{
		if (levelA != null)
			levelA.setRoot(this);

		this.levelA = levelA;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
