package org.cumulus4j.store.test.inheritance.sources;

import java.io.Serializable;
import java.math.BigDecimal;

import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

import org.cumulus4j.store.test.inheritance.sources.Terms.Options;

@PersistenceCapable
public class TermsDBO implements Serializable {

    private static final long serialVersionUID = 1931143776207228660L;

    @Persistent
    private Options option;


    public Options getOption() {
        return option;
    }

    public void setOption(Options option) {
        this.option = option;
    }

}
