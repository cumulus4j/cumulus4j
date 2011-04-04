package org.cumulus4j.test.collection.mappedby;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.Key;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class Element2MapOwner
{
	private String name;

	@Persistent(mappedBy="owner")
	@Key(mappedBy="key")
	private Map<String, Element2> map = new HashMap<String, Element2>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<String, Element2> getMap() {
		return Collections.unmodifiableMap(map);
	}
	public void addElement2(Element2 element2)
	{
		element2.setOwner(this);
		map.put(element2.getKey(), element2);
	}
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
