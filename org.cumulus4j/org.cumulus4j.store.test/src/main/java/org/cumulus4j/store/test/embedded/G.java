package org.cumulus4j.store.test.embedded;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class G {

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private Long id;

	@Persistent
	private String class_g_id;

	@Persistent
	private int  counter;

	public String getClass_g_id() {
		return class_g_id;
	}

	public void setClass_g_id(String class_f_id) {
		this.class_g_id = class_f_id;
	}

	public int getCounter() {
		return counter;
	}

	public void setCounter(int counter) {
		this.counter = counter;
	}

}
