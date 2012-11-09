package org.cumulus4j.store.test.inheritance.sources;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class TestSubClassA extends TestSuperClass {

    @Persistent
    private String f1_text;

    @Persistent
    private TermsDBO terms;

    public void setF1Text(String f1_text) {
        this.f1_text = f1_text;
    }

    public String getF1Text() {
        return f1_text;
    }

}
