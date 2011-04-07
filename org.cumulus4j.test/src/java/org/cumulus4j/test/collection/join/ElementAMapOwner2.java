package org.cumulus4j.test.collection.join;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class ElementAMapOwner2
{
	private String name;

	@Join
	private Map<ElementA, String> map = new HashMap<ElementA, String>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<ElementA, String> getMap() {
		return map == null ? null : Collections.unmodifiableMap(map);
	}
	public void putMapEntry(ElementA elementA, String value)
	{
		map.put(elementA, value);
	}
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
