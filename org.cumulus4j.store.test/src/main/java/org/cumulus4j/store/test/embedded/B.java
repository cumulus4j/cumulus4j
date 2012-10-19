package org.cumulus4j.store.test.embedded;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;

@PersistenceCapable
public class B extends A {
	
	@Persistent
    private String furtherDetails;

	public String getFurtherDetails() {
		return furtherDetails;
	}

	public void setFurtherDetails(String furtherDetails) {
		this.furtherDetails = furtherDetails;
	}
	
}
