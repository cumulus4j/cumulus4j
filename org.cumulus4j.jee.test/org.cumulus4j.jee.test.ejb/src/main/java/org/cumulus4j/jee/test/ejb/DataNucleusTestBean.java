package org.cumulus4j.jee.test.ejb;

import java.util.Collection;
import java.util.UUID;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.Transaction;
import javax.resource.spi.IllegalStateException;

import org.cumulus4j.store.test.movie.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DataNucleusTestBean extends AbstractDataNucleusTestBean implements DataNucleusTestRemote {
	private static final Logger logger = LoggerFactory.getLogger(DataNucleusTestBean.class);

	// OTHER TEST BEAN!!!
	@EJB
	private DataNucleusNewTransactionBean dataNucleusNewTransactionBean;

//	@Resource
//	private SessionContext sessionContext;

	@Override
	public boolean isAvailable() {
		return true;
	}

	protected Collection<Movie> getMoviesByName(PersistenceManager pm, String name) {
		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.name == :name");

		@SuppressWarnings("unchecked")
		Collection<Movie> result = (Collection<Movie>) q.execute(name);
		return result;
	}

	@Override
	public void test(UUID id, boolean throwException) throws Exception {
//		if (sessionContext == null)
//			throw new IllegalStateException("sessionContext == null");
//
//		UserTransaction userTransaction = sessionContext.getUserTransaction();
//		int status = userTransaction.getStatus();
//		if (Status.STATUS_ACTIVE == status)
//			logger.info("userTransaction.getStatus() = STATUS_ACTIVE");
//		else if (Status.STATUS_NO_TRANSACTION == status)
//			logger.warn("userTransaction.getStatus() = STATUS_NO_TRANSACTION");
//		else
//			logger.warn("userTransaction.getStatus() = " + status);

		PersistenceManager pm = getPersistenceManager();
		try {
			try {
				Transaction currentTransaction = pm.currentTransaction();
				logger.info("pm.currentTransaction().isActive() = {}", currentTransaction.isActive());
			} catch (Exception x) {
				// We expect that it is not possible to access the current tx via JDO API in a transactional container (=> AS).
				logger.debug("pm.currentTransaction() failed (as expected): {}", x.toString());
			}

			String movieName = id.toString();

			pm.deletePersistentAll(getMoviesByName(pm, movieName));

			Movie movie = new Movie();
			movie.setName(movieName);

			pm.makePersistent(movie);
			pm.flush();

			Collection<Movie> movies = getMoviesByName(pm, movieName);
			if (movies.size() != 1)
				throw new IllegalStateException(String.format("movies.size == %s != 1", movies.size()));

			logger.info("Object {} successfully stored in the database!", movieName);

			// assert that the 'someEntity' really *is* in the database
			 if (throwException)
			    throw new TestRollbackException("Legal exception for test purposes. Object " + movie.getName() + " should now be deleted!");

		} finally {
//			pm.close(); // causes an exception. TODO is it implicitely closed because of being hooked into JTA? verify and ask in the DN forum, if there seems to be a bug.
//			dataNucleusNewTransactionBean.logInNewTransaction(String.valueOf(success));
		}
	}

	@Override
	public boolean objectExists(UUID id) {
		PersistenceManager pm = getPersistenceManager();
		try {
			String movieName = id.toString();
			logger.info("Searching for object with id {}", movieName);

			Collection<Movie> movies = getMoviesByName(pm, movieName);
			return !movies.isEmpty();
		} finally{
//			pm.close(); // causes an exception. TODO is it implicitely closed because of being hooked into JTA? verify and ask in the DN forum, if there seems to be a bug.
		}
	}

}
