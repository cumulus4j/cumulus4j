package org.cumulus4j.store.test.embedded;

import javax.jdo.annotations.Embedded;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
@Inheritance(strategy = InheritanceStrategy.SUBCLASS_TABLE)
public abstract class A {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    private Long id;

    @Persistent
    @Embedded(nullIndicatorColumn = "class_c_id")
    private C instance_of_c = null;
    
    @Persistent
    private String class_a_id;

    public String getClass_a_id() {
        return class_a_id;
    }

    public void setClass_a_id(String class_a_id) {
        this.class_a_id = class_a_id;
    }

    public C getInstance_of_c() {
        return instance_of_c;
    }

    public void setInstance_of_c(C instance_of_c) {
        this.instance_of_c = instance_of_c;
    }

    public Long getId() {
        return id;
    }

}
