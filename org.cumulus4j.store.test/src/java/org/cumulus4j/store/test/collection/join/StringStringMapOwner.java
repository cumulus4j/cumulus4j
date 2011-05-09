package org.cumulus4j.store.test.collection.join;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class StringStringMapOwner
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long id = -1;

	@Join
	private Map<String, String> map = new HashMap<String, String>();

	public long getId() {
		return id;
	}

	public Map<String, String> getMap() {
		return map;
	}
}
