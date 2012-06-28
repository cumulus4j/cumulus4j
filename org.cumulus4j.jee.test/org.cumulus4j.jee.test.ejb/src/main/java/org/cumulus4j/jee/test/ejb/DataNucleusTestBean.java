package org.cumulus4j.jee.test.ejb;

import javax.ejb.Stateless;
import javax.jdo.PersistenceManager;

import org.cumulus4j.store.test.movie.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class DataNucleusTestBean extends AbstractDataNucleusTestBean implements DataNucleusTestRemote {
	private static final Logger logger = LoggerFactory.getLogger(DataNucleusTestBean.class);
	
	// OTHER TEST BEAN!!!
//	@EJB
//	private DataNucleusNewTransactionBean dataNucleusNewTransactionBean;

	@Override
	public void test(/* boolean throwException */) {
		boolean success = false;
		PersistenceManager pm = getPersistenceManager();
		try {
			
			Movie movie = new Movie();
			movie.setName("MMM " + System.currentTimeMillis());
			
			pm.makePersistent(movie);
			pm.flush();
//			pm.newQuery("");
			// assert that the 'someEntity' really *is* in the database
			//
			// if (throwException)
			//    throw new RuntimeException("Test");
			
			
			
			success = true;
		} finally {
			pm.close();
//			dataNucleusNewTransactionBean.logInNewTransaction(String.valueOf(success));
		}
	}
	
}
