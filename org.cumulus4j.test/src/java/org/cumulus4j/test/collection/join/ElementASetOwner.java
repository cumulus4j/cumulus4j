package org.cumulus4j.test.collection.join;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.annotations.Join;
import javax.jdo.annotations.PersistenceCapable;

@PersistenceCapable
public class ElementASetOwner
{
	private String name;

	@Join
	private Set<ElementA> set = new HashSet<ElementA>();

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<ElementA> getSet() {
		return set == null ? null : Collections.unmodifiableSet(set);
	}
	public void addElementA(ElementA elementA)
	{
		set.add(elementA);
	}
	@Override
	public String toString() {
		return getClass().getSimpleName() + "[name=\"" + name + "\"]";
	}
}
