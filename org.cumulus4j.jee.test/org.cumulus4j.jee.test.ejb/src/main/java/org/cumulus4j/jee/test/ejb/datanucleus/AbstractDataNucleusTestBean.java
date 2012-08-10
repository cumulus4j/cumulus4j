package org.cumulus4j.jee.test.ejb.datanucleus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;
import javax.resource.spi.IllegalStateException;

import org.cumulus4j.store.test.movie.Movie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractDataNucleusTestBean {

	private static final Logger logger = LoggerFactory.getLogger(AbstractDataNucleusTestBean.class);

	private static PersistenceManagerFactory pmf = null;

	protected static synchronized void resetPMF() {
		PersistenceManagerFactory _pmf = pmf;
		pmf = null;
		if (_pmf != null) {
			try {
				_pmf.close();
			} catch (Exception x) {
				logger.info("resetPMF: pmf.close() failed: " + x, x);
			}
		}
	}

	protected PersistenceManager getPersistenceManager() {
	    logger.info("getPersistenceManager: Entered.");
		return getPersistenceManagerFactory().getPersistenceManager();
	}

	protected Properties getProperties() {
		InputStream propertiesStream = AbstractDataNucleusTestBean.class.getResourceAsStream("/org/cumulus4j/jee/test/ejb/datanucleus/datanucleus.properties");
		Properties propsFile = new Properties();
		try {
			propsFile.load(propertiesStream);
			propertiesStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return propsFile;
	}

	private PersistenceManagerFactory getPersistenceManagerFactory() {
		synchronized (AbstractDataNucleusTestBean.class) {
			if (pmf == null)
				pmf = JDOHelper.getPersistenceManagerFactory(getProperties());

			return pmf;
		}
	}

	protected void storeObject(PersistenceManager pm, UUID id) throws Exception {
	    logger.info("storeObject: Entered.");

//		try {
//			Transaction currentTransaction = pm.currentTransaction();
//			logger.info("pm.currentTransaction().isActive() = {}",
//					currentTransaction.isActive());
//			logger.info("pm.currentTransaction() {}", currentTransaction);
//		} catch (Exception x) {
//			// We expect that it is not possible to access the current tx via
//			// JDO API in a transactional container (=> AS).
//			logger.debug("pm.currentTransaction() failed (as expected): {}",
//					x.toString());
//		}

		String movieName = id.toString();

		Collection<Movie> moviesBefore = getMoviesByName(pm, movieName);
		if (!moviesBefore.isEmpty())
			throw new IllegalStateException(
					"There is already a Movie with this ID! ID should be unique!!! ID="
							+ movieName);

		Movie movie = new Movie();
		movie.setName(movieName);

		pm.makePersistent(movie);
		pm.flush();

		// assert that the 'someEntity' really *is* in the database
		Collection<Movie> movies = getMoviesByName(pm, movieName);
		if (movies.size() != 1)
			throw new IllegalStateException(String.format(
					"movies.size == %s != 1", movies.size()));

		logger.info("storeObject: Object {} successfully stored in the database!", movieName);
	}

	protected Collection<Movie> getMoviesByName(PersistenceManager pm,
			String name) {
		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.name == :name");

		@SuppressWarnings("unchecked")
		Collection<Movie> result = (Collection<Movie>) q.execute(name);
		return result;
	}
}
