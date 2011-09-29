package org.cumulus4j.benchmark.inheritancescenario;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.benchmark.framework.Entity;

/**
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class InheritanceObject0 extends Entity{

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InheritanceObject0 other = (InheritanceObject0) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long id;

	private int i0;

	public InheritanceObject0(int i0){
		this.setI0(i0);
	}

	@Override
	public long getId() {

		return id;
	}

	public void setI0(int i0) {
		this.i0 = i0;
	}

	public int getI0() {
		return i0;
	}
}
