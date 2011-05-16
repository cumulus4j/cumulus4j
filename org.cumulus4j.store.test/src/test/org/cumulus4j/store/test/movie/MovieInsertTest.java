package org.cumulus4j.store.test.movie;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTest;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.cumulus4j.store.test.movie.Movie;
import org.cumulus4j.store.test.movie.Person;
import org.cumulus4j.store.test.movie.Rating;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieInsertTest
extends AbstractJDOTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(MovieInsertTest.class);

	@BeforeClass
	public static void clearDatabase()
	throws Exception
	{
		logger.info("clearDatabase: Clearing database (dropping all tables).");
		CleanupUtil.dropAllTables();
	}

	@Test
	public void createOneMovieWithRating()
	throws Exception
	{
		logger.info("createOneMovieWithRating: entered");
		pm.getExtent(Movie.class);

		Movie movie = new Movie();
		movie.setName("MMM " + System.currentTimeMillis());
		movie = pm.makePersistent(movie);

		Rating rating = new Rating();
		rating.setName("RRR " + System.currentTimeMillis());
		rating = pm.makePersistent(rating);

		movie.setRating(rating);
	}

	@Test
	public void createOneMovieWithOneStarring()
	throws Exception
	{
		logger.info("createOneMovieWithOneStarring: entered");
		pm.getExtent(Movie.class);

		Movie movie = new Movie();
		movie.setName("MMM " + System.currentTimeMillis());
		movie = pm.makePersistent(movie);

		Person person = new Person();
		person.setName("PPP " + System.currentTimeMillis());
		person = pm.makePersistent(person);

		movie.getStarring().add(person);
	}
}
