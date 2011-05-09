package org.cumulus4j.test.collection.join;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class ElementAMapOwner3
{
	private String name;

	@Join
	private Map<ElementA, ElementA> map = new HashMap<ElementA, ElementA>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<ElementA, ElementA> getMap() {
		return map == null ? null : Collections.unmodifiableMap(map);
	}
	public void putMapEntry(ElementA elementAKey, ElementA elementAValue)
	{
		map.put(elementAKey, elementAValue);
	}
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
