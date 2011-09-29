package org.cumulus4j.benchmark.scenario.relation;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.benchmark.framework.Entity;

/**
 *
 * @author jmortensen - jmortensen at nightlabs dot de
 *
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class SimplePerson extends Entity{

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long id;

	@Override
	public long getId() {

		return id;
	}

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
		SimplePerson other = (SimplePerson) obj;
		if (id != other.id)
			return false;
		return true;
	}


}
