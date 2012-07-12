package org.cumulus4j.jee.test.ejb;

import java.util.Collection;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.jdo.PersistenceManager;

import org.cumulus4j.store.test.movie.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
public class DataNucleusTestBean extends AbstractDataNucleusTestBean implements
		DataNucleusTestRemote {
	private static final Logger logger = LoggerFactory
			.getLogger(DataNucleusTestBean.class);

	@EJB
	private DataNucleusNewTransactionBean dataNucleusNewTransactionBean;

	@Override
	public boolean isAvailable() {
		return true;
	}

	@Override
	public void test(UUID id, boolean throwException) throws Exception {

		storeObject(id);

		// assert that the 'someEntity' really *is* in the database
		if (throwException)
			throw new TestRollbackException(
					"Legal exception for test purposes. Object "
							+ id.toString() + " should now be deleted!");

	}

	@Override
	public void test(UUID id1, UUID id2,
			boolean throwExceptionInMainTransaction,
			boolean throwExceptionInNestedTransaction) throws Exception {

		storeObject(id1);

		boolean expectedExceptionThrown = false;
		try {
			dataNucleusNewTransactionBean.test(id2,
					throwExceptionInNestedTransaction);
		}
		catch (Exception x) {
//			int index = ExceptionUtils.indexOfThrowable(x,
//					TestRollbackException.class);
//			if (index >= 0)
//				expectedExceptionThrown = true;
//			else
			if(x instanceof TestRollbackException)
				expectedExceptionThrown = true;
			else
				throw x;
		}

//		if(throwExceptionInMainTransaction)
//			Assert.assertTrue("TestRollbackException was not thrown!",
//				expectedExceptionThrown);

		if (throwExceptionInMainTransaction)
			throw new TestRollbackException(
					"Legal exception for test purposes. Object "
							+ id1.toString() + " should now be deleted!");

	}

	@Override
	public boolean objectExists(UUID id) {
		PersistenceManager pm = getPersistenceManager();
		String movieName = id.toString();
		logger.info("Searching for object with id {}", movieName);

		Collection<Movie> movies = getMoviesByName(pm, movieName);
		logger.info(movies.toString());
		return !movies.isEmpty();
	}
}