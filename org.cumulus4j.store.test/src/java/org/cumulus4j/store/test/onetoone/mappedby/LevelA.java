package org.cumulus4j.store.test.onetoone.mappedby;

import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class LevelA
{
	private Root root;

	private LevelB levelB;

	private String name;

	public Root getRoot() {
		return root;
	}

	protected void setRoot(Root root) {
		this.root = root;
	}

	public void setLevelB(LevelB levelB) {
		if (levelB != null)
			levelB.setLevelA(this);

		this.levelB = levelB;
	}

	public LevelB getLevelB() {
		return levelB;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
