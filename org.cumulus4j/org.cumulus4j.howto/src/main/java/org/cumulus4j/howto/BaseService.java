package org.cumulus4j.howto;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Properties;

import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.cumulus4j.howto.entities.Movie;
import org.cumulus4j.howto.entities.Person;
import org.cumulus4j.howto.entities.Rating;

@Consumes({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
@Produces({ MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON })
public abstract class BaseService {

	protected static PersistenceManagerFactory pmf;

	protected static Properties loadProperties(String fileName) {
		Properties result = new Properties();

		try {
			InputStream in = BaseService.class.getClassLoader().getResourceAsStream(fileName);
			result.load(in);
			in.close();

		} catch (IOException x) {
			throw new RuntimeException(x);
		}

		String tmpKey = "";
		String tmpValue = "";
		for(Object key : result.keySet()){
			String value = ((String)result.get(key));
			if(value.contains("${java.io.tmpdir}")){
				tmpKey = (String)key;
				tmpValue = value.replace("${java.io.tmpdir}", System.getProperty("java.io.tmpdir"));
			}
		}
		result.put(tmpKey, tmpValue);

		return result;
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String testGet() {
		return "OK: " + this.getClass().getName()
				+ ": Use POST on the same URL for a real test.";
	}

	// The actual test to be performed. Without Cumulus4j, with a dummy key manager and
	// with a real key manager always this test is performed. 
	protected String storeEntities(PersistenceManager pm){

		StringBuilder resultSB = new StringBuilder();

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
			}

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
