package org.cumulus4j.test.collection.join;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class Element4SetOwner
{
	private String name;

	@Persistent(mappedBy="owner")
	private Set<Element4> set = new HashSet<Element4>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<Element4> getSet() {
		return set == null ? null : Collections.unmodifiableSet(set);
	}
	public void addElement4(Element4 element4)
	{
		set.add(element4);
	}
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
