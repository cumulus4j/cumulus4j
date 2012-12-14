package org.cumulus4j.store.test.embedded;

import javax.jdo.annotations.EmbeddedOnly;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
@EmbeddedOnly
public class E {

    @Persistent
    private String information = "";

    // The following variables are lower-case versions of the upper ones.
    // They are used only inside queries and therefore possess neither getter
    // nor setter
    @Persistent
    private String lc_information = "";

    public String getInformation() {
        return information;
    }

    public void setInformation(String information) {
        this.information = information;
        if (information != null) {
            this.lc_information = information.toLowerCase().trim();
        }
    }
}
