package org.cumulus4j.store.test.embedded;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class C {

    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.NATIVE)
    private long c_id;

    /**
     * This field is used in A as nullIndicatorColumn and is therefore initialized as null
     */
    @Persistent
    private String class_c_id = null;

    // The following variables are lower-case versions of the upper ones.
    // They are used only inside queries and therefore possess neither getter
    // nor setter
    @Persistent
    private String lc_class_c_id = "";

    public String getClass_c_id() {
        return class_c_id;
    }

    public void setClass_c_id(String class_c_id) {
        this.class_c_id = class_c_id;
        this.lc_class_c_id = class_c_id.toLowerCase().trim();
    }

}
