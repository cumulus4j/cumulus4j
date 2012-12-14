package org.cumulus4j.integrationtest.gwt.server;

import java.util.Iterator;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.integrationtest.gwt.client.MovieService;
import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.cumulus4j.store.test.framework.TestUtil;
import org.cumulus4j.store.test.movie.Movie;
import org.cumulus4j.store.test.movie.Person;
import org.cumulus4j.store.test.movie.Rating;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server side implementation of the RPC service. Implementation copied from
 * {@link TestService}
 * 
 * @author Jan Morlock - jan dot morlock at googlemail dot com
 */
@SuppressWarnings("serial")
public class MovieServiceImpl extends RemoteServiceServlet implements
		MovieService {

	/**
	 * Persistence manager factory instance.
	 */
	private static PersistenceManagerFactory pmf;

	/**
	 * Setup of the persistence manager factory.
	 * 
	 * @return Persistence manager factory instance
	 */
	protected static synchronized PersistenceManagerFactory getPersistenceManagerFactory() {
		if (pmf == null) {
			try {
				CleanupUtil.dropAllTables();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			pmf = JDOHelper.getPersistenceManagerFactory(TestUtil
					.loadProperties("cumulus4j-test-datanucleus.properties"));
		}

		return pmf;
	}

	/**
	 * Get the persistence manager for cumulus4j.
	 * 
	 * @param cryptoManagerID
	 *            Crypto manager id
	 * @param cryptoSessionID
	 *            Crypto session id
	 * @return New persistence manager
	 */
	protected final PersistenceManager getPersistenceManager(
			final String cryptoManagerID, final String cryptoSessionID) {
		if (cryptoManagerID == null) {
			throw new IllegalArgumentException("cryptoManagerID == null");
		}

		if (cryptoSessionID == null) {
			throw new IllegalArgumentException("cryptoSessionID == null");
		}

		PersistenceManager pm = getPersistenceManagerFactory()
				.getPersistenceManager();
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID,
				cryptoManagerID);
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID,
				cryptoSessionID);
		return pm;
	}

	@Override
	public final String fetchSomeMovies(final String cryptoSessionID) {

		String cryptoManagerID = null;

		// We enforce a fresh start every time, because we execute this now with
		// different key-servers / embedded key-stores:
		if (pmf != null) {
			pmf.close();
			pmf = null;
		}

		if (cryptoManagerID == null || cryptoManagerID.isEmpty()) {
			cryptoManagerID = "keyManager";
		}

		StringBuilder resultSB = new StringBuilder();
		PersistenceManager pm = getPersistenceManager(cryptoManagerID,
				cryptoSessionID);
		try {
			// tx1: persist some data
			pm.currentTransaction().begin();

			pm.getExtent(Movie.class);
			{
				Movie movie = new Movie();
				movie.setName("MMM " + System.currentTimeMillis());
				movie = pm.makePersistent(movie);

				Rating rating = new Rating();
				rating.setName("RRR " + System.currentTimeMillis());
				rating = pm.makePersistent(rating);

				movie.setRating(rating);
			}

			{
				Movie movie = new Movie();
				movie.setName("MMM " + System.currentTimeMillis());
				movie = pm.makePersistent(movie);

				Person person = new Person();
				person.setName("PPP " + System.currentTimeMillis());
				person = pm.makePersistent(person);

				movie.getStarring().add(person);
				pm.currentTransaction().commit();
			}

			pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);

			pm.currentTransaction().begin();

			for (Iterator<Movie> it = pm.getExtent(Movie.class).iterator(); it
					.hasNext();) {
				Movie movie = it.next();
				resultSB.append(" * ").append(movie.getName()).append('\n');
			}

			pm.currentTransaction().commit();
			return "OK: " + this.getClass().getName() + "\n\nSome movies:\n"
					+ resultSB;
		} finally {
			if (pm.currentTransaction().isActive()) {
				pm.currentTransaction().rollback();
			}

			pm.close();
		}
	}
}
