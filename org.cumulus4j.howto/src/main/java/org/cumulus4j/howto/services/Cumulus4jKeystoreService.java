package org.cumulus4j.howto.services;

import java.util.Iterator;
import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.howto.BaseService;
import org.cumulus4j.howto.entities.Movie;
import org.cumulus4j.howto.entities.Person;
import org.cumulus4j.howto.entities.Rating;
import org.cumulus4j.store.crypto.CryptoSession;

@Path("Test")
public  class Cumulus4jKeystoreService extends BaseService{

		protected PersistenceManager getPersistenceManager(String cryptoManagerID, String CryptoSessionID) {
			PersistenceManager pm = getPersistenceManagerFactory()
					.getPersistenceManager();

			pm.setProperty("datanucleus.storeManagerType", "cumulus4j");
			pm.setProperty("cumulus4j.cryptoManagerID", "dummy");
			pm.setProperty(
					CryptoSession.PROPERTY_CRYPTO_SESSION_ID,
					"dummyKeyStoreID_" + UUID.randomUUID() + '*'
							+ UUID.randomUUID());

			pm.setProperty("cumulus4j.index.clob.enabled", "false");

			return pm;
		}

		@POST
		@Produces(MediaType.TEXT_PLAIN)
		public String testPost(
				@QueryParam("cryptoManagerID") String cryptoManagerID,
				@QueryParam("cryptoSessionID") String cryptoSessionID) {
			// We enforce a fresh start every time, because we execute this now with
			// different key-servers / embedded key-stores:
			if (pmf != null) {
				pmf.close();
				pmf = null;
			}

			if (cryptoManagerID == null || cryptoManagerID.isEmpty())
				cryptoManagerID = "keyManager";

			StringBuilder resultSB = new StringBuilder();
			PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
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

		@Override
		@GET
		@Produces(MediaType.TEXT_PLAIN)
		public String testGet() {
			return "OK: " + this.getClass().getName()
					+ ": Use POST on the same URL for a real test.";
		}

}
