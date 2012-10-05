package org.cumulus4j.howto.services;

import java.util.Iterator;
import java.util.Properties;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.howto.BaseService;
import org.cumulus4j.howto.entities.Movie;
import org.cumulus4j.howto.entities.Person;
import org.cumulus4j.howto.entities.Rating;
import org.cumulus4j.store.crypto.CryptoSession;

@Path("DummyKeyManagerService")
public class DummyKeyManagerService extends BaseService {

	protected static synchronized PersistenceManagerFactory getPersistenceManagerFactory() {
		if (pmf == null) {

			Properties props = loadProperties("datanucleus.properties");
			props.putAll(loadProperties("cumulus4j.properties"));

			props.put("cumulus4j.cryptoManagerID", "dummy");

			pmf = JDOHelper
					.getPersistenceManagerFactory(props);
		}

		return pmf;
	}

	private PersistenceManager getPersistenceManager() {
		PersistenceManager pm = getPersistenceManagerFactory()
				.getPersistenceManager();

		pm.setProperty(
				CryptoSession.PROPERTY_CRYPTO_SESSION_ID,
				"dummyKeyStoreID_" + UUID.randomUUID() + '*'
						+ UUID.randomUUID());

		return pm;
	}

	@POST
	@Produces(MediaType.TEXT_PLAIN)
	public String testPost() {
		// We enforce a fresh start every time, because we execute this now with
		// different key-servers / embedded key-stores:
		if (pmf != null) {
			pmf.close();
			pmf = null;
		}

		StringBuilder resultSB = new StringBuilder();
		PersistenceManager pm = getPersistenceManager();
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

			pm = getPersistenceManager();

			// tx2: read some data
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
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}
	}
}
