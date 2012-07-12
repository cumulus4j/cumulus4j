package org.cumulus4j.jee.test.ejb;

import java.util.UUID;

import javax.ejb.Remote;

@Remote
public interface DataNucleusTestRemote {

	void test(UUID id, boolean throwException) throws Exception;

	boolean objectExists(UUID id);

	boolean isAvailable();

}
