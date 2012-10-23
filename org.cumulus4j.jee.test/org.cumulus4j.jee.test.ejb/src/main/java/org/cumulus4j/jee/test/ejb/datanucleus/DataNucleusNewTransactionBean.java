package org.cumulus4j.jee.test.ejb.datanucleus;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.cumulus4j.jee.test.ejb.TestRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class DataNucleusNewTransactionBean extends AbstractDataNucleusTestBean {

	private static final Logger logger = LoggerFactory.getLogger(DataNucleusNewTransactionBean.class);

	public void testRollback(UUID id, boolean throwException) throws Exception {

	    PersistenceManager pm = getPersistenceManager();
		storeObject(pm, id);

		if (throwException)
			throw new TestRollbackException(
					"Legal exception for test purposes. Object "
							+ id.toString() + " should now be deleted!");
		else
			logger.info("Nested transaction ended without throwing an exception!");
	}
}
