package org.cumulus4j.jee.test.ejb;

import javax.ejb.Remote;

@Remote
public interface ConnectionTestRemote {

	String test(String input);

}
