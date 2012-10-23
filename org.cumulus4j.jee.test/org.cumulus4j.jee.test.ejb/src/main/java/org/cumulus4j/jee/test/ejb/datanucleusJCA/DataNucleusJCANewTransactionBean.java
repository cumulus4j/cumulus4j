package org.cumulus4j.jee.test.ejb.datanucleusJCA;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

import org.cumulus4j.jee.test.ejb.TestRollbackException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class DataNucleusJCANewTransactionBean extends AbstractDataNucleusJCATestBean {

	private static final Logger logger = LoggerFactory.getLogger(DataNucleusJCANewTransactionBean.class);

	public void testRollback(UUID id, boolean throwException) throws Exception {

		storeObject(id);

		if (throwException)
			throw new TestRollbackException(
					"Legal exception for test purposes. Object "
							+ id.toString() + " should now be deleted!");
		else
			logger.info("Nested transaction ended without throwing an exception!");
	}
}
