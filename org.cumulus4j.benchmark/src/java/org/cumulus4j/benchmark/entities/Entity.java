package org.cumulus4j.benchmark.entities;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * 
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
public class Entity {
	
	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private long id;
	
	public Entity(){
		this(0);
	}
	
	public Entity(long id){
		this.id = id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

}
