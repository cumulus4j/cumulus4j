package org.cumulus4j.test.movie;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class Person
{
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long personID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String name;

	public long getPersonID() {
		return personID;
	}

	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		return (int) (personID ^ (personID >>> 32));
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (this.personID < 0) return false;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Person other = (Person) obj;
		if (personID != other.personID)
			return false;
		return true;
	}
}
