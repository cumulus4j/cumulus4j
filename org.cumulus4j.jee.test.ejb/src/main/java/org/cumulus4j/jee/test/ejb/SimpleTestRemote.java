package org.cumulus4j.jee.test.ejb;

import javax.ejb.Remote;

@Remote
public interface SimpleTestRemote {

	String test(String input);

}
