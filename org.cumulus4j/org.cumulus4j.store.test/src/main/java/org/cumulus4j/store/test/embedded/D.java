package org.cumulus4j.store.test.embedded;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class D {
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Long id;
	
	@Persistent
    @Embedded
    private E instance_of_e = null;
	
	@Persistent
    private String furtherDetails;
	
	@Persistent
    private String class_d_id;

    public String getClass_d_id() {
        return class_d_id;
    }

    public void setClass_d_id(String class_d_id) {
        this.class_d_id = class_d_id;
    }

	public String getFurtherDetails() {
		return furtherDetails;
	}

	public void setFurtherDetails(String furtherDetails) {
		this.furtherDetails = furtherDetails;
	}
	
	public E getInstance_of_e() {
        return instance_of_e;
    }

    public void setInstance_of_e(E instance_of_e) {
        this.instance_of_e = instance_of_e;
    }
	
	public Long getId() {
        return id;
    }
	
}