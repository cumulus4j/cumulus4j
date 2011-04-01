package org.cumulus4j.test.collection.mappedby;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.Value;

@PersistenceCapable
public class Element3MapOwner
{
	private String name;

	@Persistent(mappedBy="owner")
	@Value(mappedBy="value")
	private Map<Element3, String> map = new HashMap<Element3, String>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Map<Element3, String> getMap() {
		return Collections.unmodifiableMap(map);
	}
	public void addElement3(Element3 element3)
	{
		element3.setOwner(this);
		map.put(element3, element3.getValue());
	}
}
