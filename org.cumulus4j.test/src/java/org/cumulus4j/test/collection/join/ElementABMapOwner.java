package org.cumulus4j.test.collection.join;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class ElementABMapOwner
{
	private String name;

	@Join
	private Map<ElementA, ElementB> map = new HashMap<ElementA, ElementB>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<ElementA, ElementB> getMap() {
		return map == null ? null : Collections.unmodifiableMap(map);
	}
	public void addElementA(ElementA elementA, ElementB elementB)
	{
		map.put(elementA, elementB);
	}
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
