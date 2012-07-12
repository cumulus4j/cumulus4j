package org.cumulus4j.jee.test.ejb;

import java.util.UUID;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
public class DataNucleusNewTransactionBean extends AbstractDataNucleusTestBean {

//	public void logInNewTransaction(String logMessage) {
//		PersistenceManager pm = getPersistenceManager();
//		try {
//
//
//
//		} finally {
//			pm.close();
//		}
//	}

	public void test(UUID id, boolean throwException) throws Exception {

		storeObject(id);

		if (throwException)
			throw new TestRollbackException(
					"Legal exception for test purposes. Object "
							+ id.toString() + " should now be deleted!");
	}

}
