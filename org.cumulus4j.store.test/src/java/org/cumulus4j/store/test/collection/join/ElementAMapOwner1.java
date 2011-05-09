package org.cumulus4j.store.test.collection.join;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class ElementAMapOwner1
{
	private String name;

	@Join
	private Map<String, ElementA> map = new HashMap<String, ElementA>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, ElementA> getMap() {
		return map == null ? null : Collections.unmodifiableMap(map);
	}
	public void putMapEntry(String key, ElementA elementA)
	{
		map.put(key, elementA);
	}
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
