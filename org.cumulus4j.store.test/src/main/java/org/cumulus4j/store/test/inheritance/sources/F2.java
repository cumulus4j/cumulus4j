package org.cumulus4j.store.test.inheritance.sources;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class F2 extends E {

    @Persistent
    private String f2_text;

    public void setF2Text(String f2_text) {
        this.f2_text = f2_text;
    }

    public String getF2Text() {
        return f2_text;
    }

}
