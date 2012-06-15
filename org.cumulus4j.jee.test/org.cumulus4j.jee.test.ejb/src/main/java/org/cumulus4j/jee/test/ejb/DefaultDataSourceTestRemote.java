package org.cumulus4j.jee.test.ejb;

import javax.ejb.Remote;

@Remote
public interface DefaultDataSourceTestRemote {

	void test();

}
