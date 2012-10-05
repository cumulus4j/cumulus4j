package org.cumulus4j.howto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public abstract class BaseService {

	protected static PersistenceManagerFactory pmf;

	protected static synchronized PersistenceManagerFactory getPersistenceManagerFactory() {
		if (pmf == null) {
			pmf = JDOHelper
					.getPersistenceManagerFactory(loadProperties("datanucleus.properties"));
		}

		return pmf;
	}

	private static Properties loadProperties(String fileName) {
		Properties result = new Properties();

		try {
			InputStream in = BaseService.class.getClassLoader().getResourceAsStream(fileName);
			result.load(in);
			in.close();

		} catch (IOException x) {
			throw new RuntimeException(x);
		}

		return result;
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testGet() {
		return "OK: " + this.getClass().getName()
				+ ": Use POST on the same URL for a real test.";
	}

//	private PersistenceManager getPersistenceManager(String cryptoManagerID, String cryptoSessionID) {
//		PersistenceManager pm = getPersistenceManagerFactory()
//				.getPersistenceManager();
////		pm.setProperty(
////				CryptoSession.PROPERTY_CRYPTO_SESSION_ID,
////				"dummyKeyStoreID_" + UUID.randomUUID() + '*'
////						+ UUID.randomUUID());
//		if (cryptoManagerID == null)
//			throw new IllegalArgumentException("cryptoManagerID == null");
//
//		if (cryptoSessionID == null)
//			throw new IllegalArgumentException("cryptoSessionID == null");
//
//		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, cryptoManagerID);
//		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, cryptoSessionID);
//
//		return pm;
//	}
//
//	@POST
//	@Produces(MediaType.TEXT_PLAIN)
//	public String testPost(
//			@QueryParam("cryptoManagerID") String cryptoManagerID,
//			@QueryParam("cryptoSessionID") String cryptoSessionID) {
//		// We enforce a fresh start every time, because we execute this now with
//		// different key-servers / embedded key-stores:
//		if (pmf != null) {
//			pmf.close();
//			pmf = null;
//		}
//
//		if (cryptoManagerID == null || cryptoManagerID.isEmpty())
//			cryptoManagerID = "keyManager";
//
//		StringBuilder resultSB = new StringBuilder();
//		PersistenceManager pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
//		try {
//			// tx1: persist some data
//			pm.currentTransaction().begin();
//
//			pm.getExtent(Movie.class);
//			{
//				Movie movie = new Movie();
//				movie.setName("MMM " + System.currentTimeMillis());
//				movie = pm.makePersistent(movie);
//
//				Rating rating = new Rating();
//				rating.setName("RRR " + System.currentTimeMillis());
//				rating = pm.makePersistent(rating);
//
//				movie.setRating(rating);
//			}
//
//			{
//				Movie movie = new Movie();
//				movie.setName("MMM " + System.currentTimeMillis());
//				movie = pm.makePersistent(movie);
//
//				Person person = new Person();
//				person.setName("PPP " + System.currentTimeMillis());
//				person = pm.makePersistent(person);
//
//				movie.getStarring().add(person);
//				pm.currentTransaction().commit();
//			}
//
//			pm = getPersistenceManager(cryptoManagerID, cryptoSessionID);
//
//			// tx2: read some data
//			pm.currentTransaction().begin();
//
//			for (Iterator<Movie> it = pm.getExtent(Movie.class).iterator(); it
//					.hasNext();) {
//				Movie movie = it.next();
//				resultSB.append(" * ").append(movie.getName()).append('\n');
//			}
//
//			pm.currentTransaction().commit();
//			return "OK: " + this.getClass().getName() + "\n\nSome movies:\n"
//					+ resultSB;
//		} finally {
//			if (pm.currentTransaction().isActive())
//				pm.currentTransaction().rollback();
//
//			pm.close();
//		}
//	}
//
//	@GET
//	@Produces(MediaType.TEXT_PLAIN)
//	public String testGet() {
//		return "OK: " + this.getClass().getName()
//				+ ": Use POST on the same URL for a real test.";
//	}

}
