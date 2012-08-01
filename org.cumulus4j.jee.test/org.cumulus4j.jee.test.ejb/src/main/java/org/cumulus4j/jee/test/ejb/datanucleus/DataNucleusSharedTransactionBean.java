package org.cumulus4j.jee.test.ejb.datanucleus;

import java.util.UUID;

import javax.ejb.Stateless;

import org.cumulus4j.jee.test.ejb.TestRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class DataNucleusSharedTransactionBean extends
		AbstractDataNucleusTestBean {

	private static final Logger logger = LoggerFactory
			.getLogger(DataNucleusSharedTransactionBean.class);

	public void testRollback(UUID id, boolean throwException) throws Exception {

		storeObject(id);

		if (throwException)
			throw new TestRollbackException(
					"Legal exception for test purposes. Object "
							+ id.toString()
							+ " should now be deleted! Also the object from the enclosing transaction should be deleted!");
		else
			logger.info("Nested transaction ended without throwing an exception!");
	}
}
