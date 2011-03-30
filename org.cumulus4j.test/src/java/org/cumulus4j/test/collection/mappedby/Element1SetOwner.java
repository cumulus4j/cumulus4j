package org.cumulus4j.test.collection.mappedby;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class Element1SetOwner
{
	private String name;

	@Persistent(mappedBy="owner")
	private Set<Element1> set = new HashSet<Element1>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<Element1> getSet() {
		return Collections.unmodifiableSet(set);
	}
	public void addElement1(Element1 element1)
	{
		element1.setOwner(this);
		set.add(element1);
	}
}
