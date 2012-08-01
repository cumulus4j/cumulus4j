package org.cumulus4j.jee.test.ejb.datanucleus;

import java.util.Collection;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.cumulus4j.jee.test.ejb.TestRollbackException;
import org.cumulus4j.store.test.movie.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DataNucleusTestBean extends AbstractDataNucleusTestBean implements
		DataNucleusTestRemote {

	private static final Logger logger = LoggerFactory
			.getLogger(DataNucleusTestBean.class);

	@EJB
	private DataNucleusNewTransactionBean dataNucleusNewTransactionBean;

	@EJB
	private DataNucleusSharedTransactionBean dataNucleusSharedTransactionBean;

	@Override
	public boolean isAvailable() {

		return true;
	}

	@Override
	public void testRollbackOnException(UUID id, boolean throwException)
			throws Exception {

		storeObject(id);

		if (throwException)
			throw new TestRollbackException(
					"Legal exception for test purposes. Object "
							+ id.toString() + " should now be deleted!");
	}

	@Override
	public void testRollbackOnNestedTransactionException(UUID id1, UUID id2,
			boolean throwExceptionInMainBean,
			boolean throwExceptionInNestedBeanCall) throws Exception {

		testRollback(id1, id2, throwExceptionInMainBean,
				throwExceptionInNestedBeanCall, true);
	}

	@Override
	public void testRollbackWithSharedTransaction(UUID id1, UUID id2,
			boolean throwExceptionInMainBean,
			boolean throwExceptionInNestedBeanCall) throws Exception {

		testRollback(id1, id2, throwExceptionInMainBean,
				throwExceptionInNestedBeanCall, false);
	}

	private void testRollback(UUID id1, UUID id2,
			boolean throwExceptionInMainBean,
			boolean throwExceptionInNestedBeanCall, boolean nestedTransaction)
			throws Exception {

		storeObject(id1);

		boolean expectedExceptionThrown = false;
		try {
			if (nestedTransaction)
				dataNucleusNewTransactionBean.testRollback(id2,
						throwExceptionInNestedBeanCall);
			else
				dataNucleusSharedTransactionBean.testRollback(id2,
						throwExceptionInNestedBeanCall);
		} catch (Exception x) {
			int index = ExceptionUtils.indexOfThrowable(x,
					TestRollbackException.class);
			if (index >= 0)
				expectedExceptionThrown = true;
			else
				throw x;
		}

		if (throwExceptionInNestedBeanCall && !expectedExceptionThrown)
			logger.error("TestRollbackException was not thrown!",
					expectedExceptionThrown);

		if (throwExceptionInMainBean)
			throw new TestRollbackException(
					"Legal exception in main bean for test purposes. Object "
							+ id1.toString() + " should now be deleted!");
		else
			logger.info("Main bean ended without throwing an exception!");
	}

	@Override
	public boolean objectExists(UUID id) {
		PersistenceManager pm = getPersistenceManager();
		String movieName = id.toString();
		logger.info("Searching for object with id {}", movieName);

		Collection<Movie> movies = getMoviesByName(pm, movieName);
		for (Movie movie : movies)
			logger.info("Id of movie object in db: {}", movie.getName());

		boolean objectExists = !movies.isEmpty();

		if (!objectExists)
			logger.info("Object with id {} not found", movieName);

		return objectExists;
	}

	@Override
	public void init() {
	}
}