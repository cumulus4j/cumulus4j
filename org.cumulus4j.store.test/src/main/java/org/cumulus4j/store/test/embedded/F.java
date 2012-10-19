package org.cumulus4j.store.test.embedded;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.cumulus4j.store.test.movie.Movie;

@PersistenceCapable
public class F {

    @PrimaryKey
    @Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
    private Long id;
    
    @Persistent
    private String class_f_id;

    @Persistent
    private int  counter;

	public String getClass_f_id() {
		return class_f_id;
	}

	public void setClass_f_id(String class_f_id) {
		this.class_f_id = class_f_id;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}
	
}
