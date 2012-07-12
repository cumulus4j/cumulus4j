package org.cumulus4j.jee.test.ejb;

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

}
