package org.cumulus4j.benchmark.framework;

/**
*
* @author Jan Mortensen - jmortensen at nightlabs dot de
*
*/
public abstract class Entity {

	public abstract long getId();

	@Override
	public String toString(){
		return "PersonAllQueryable id: " + getId();
	}
}
