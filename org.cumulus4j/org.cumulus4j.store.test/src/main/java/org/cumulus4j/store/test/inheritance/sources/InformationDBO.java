package org.cumulus4j.store.test.inheritance.sources;

import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class InformationDBO {

    @Persistent
    private String additionalInformation;

    @Persistent
    private String lc_additionalInformation;

    public String getAdditionalInformation() {
        return additionalInformation;
    }

    public void setAdditionalInformation(String additionalInformation) {
        this.additionalInformation = additionalInformation;
        if (additionalInformation != null) {
            this.lc_additionalInformation = additionalInformation.toLowerCase().trim();
        }
    }

}