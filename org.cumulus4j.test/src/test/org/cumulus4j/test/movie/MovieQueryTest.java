package org.cumulus4j.test.movie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.jdo.Extent;
import javax.jdo.Query;

import org.cumulus4j.test.framework.AbstractTransactionalTest;
import org.cumulus4j.test.framework.CleanupUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieQueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(MovieQueryTest.class);

	@BeforeClass
	public static void clearDatabase()
	throws Exception
	{
		logger.info("clearDatabase: Clearing database (dropping all tables).");
		CleanupUtil.dropAllTables();
	}

	private static String safeTrim(String s)
	{
		if (s == null)
			return null;

		return s.trim();
	}

	private static String[] safeTrim(String[] ss)
	{
		if (ss == null)
			return null;

		for (int i = 0; i < ss.length; i++)
			ss[i] = safeTrim(ss[i]);

		return ss;
	}

	@Before
	public void importDataCsv()
	throws Exception
	{
		logger.info("importDataCsv: entered");
		Extent<Movie> movieExtent = pm.getExtent(Movie.class);
		Assert.assertNotNull("pm.getExtent(Movie.class) returned null!", movieExtent);
		if (movieExtent.iterator().hasNext()) {
			logger.info("importDataCsv: already imported before => skipping.");
			return;
		}
		logger.info("importDataCsv: nothing imported before => importing now.");

		Query queryMovieByName = pm.newQuery(Movie.class);
		queryMovieByName.setFilter("this.name == :name");
		queryMovieByName.setUnique(true);

		Query queryPersonByName = pm.newQuery(Person.class);
		queryPersonByName.setFilter("this.name == :name");
		queryPersonByName.setUnique(true);

		Query queryLanguageByName = pm.newQuery(Language.class);
		queryLanguageByName.setFilter("this.name == :name");
		queryLanguageByName.setUnique(true);

		Query queryRatingByName = pm.newQuery(Rating.class);
		queryRatingByName.setFilter("this.name == :name");
		queryRatingByName.setUnique(true);

		BufferedReader r = new BufferedReader(
				new InputStreamReader(MovieQueryTest.class.getResourceAsStream("data.csv"), "UTF-8")
		);
		String line;
		while ((line = r.readLine()) != null) {
			String[] fields = line.split("\t");
			int fieldNo = -1;
			String movieName = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo]);
			String starringName = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo]);
			String[] writtenByNames = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo].split(","));
			String[] languageNames = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo].split(","));
			String[] directedByNames = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo].split(","));
			String[] producedByNames = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo].split(","));
			String tagline = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo]);
			String initialReleaseDate = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo]);
			String ratingName = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo]);
			String estimatedBudget = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo]);
			String sequel = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo]);
			String prequel = safeTrim(fields.length <= ++fieldNo ? null : fields[fieldNo]);

//			System.out.println("movieName = " + movieName);

			Movie movie = (Movie) queryMovieByName.execute(movieName);
			if (movie == null) {
				movie = new Movie();
				movie.setName(movieName);
				movie = pm.makePersistent(movie);
			}

			if (starringName != null) {
				Person person = (Person) queryPersonByName.execute(starringName);
				if (person == null) {
					person = new Person();
					person.setName(starringName);
					person = pm.makePersistent(person);
				}
				if (!movie.getStarring().contains(person)) // TODO open an issue in DN issue tracker - this contains should not be necessary - the add(...) should not do an INSERT if it is already in the set!
					movie.getStarring().add(person);
			}

			if (languageNames != null) {
				for (String languageName : languageNames) {
					Language language = (Language) queryLanguageByName.execute(languageName);
					if (language == null) {
						language = new Language();
						language.setName(languageName);
						language = pm.makePersistent(language);
					}
					if (!movie.getLanguages().contains(language)) // TODO open DataNucleus issue - add should not do an INSERT without checking - this contains should not be necessary!
						movie.getLanguages().add(language);
				}
			}

			if (writtenByNames != null) {
				for (String writtenByName : writtenByNames) {
					Person person = (Person) queryPersonByName.execute(writtenByName);
					if (person == null) {
						person = new Person();
						person.setName(writtenByName);
						person = pm.makePersistent(person);
					}
					if (!movie.getWrittenBy().contains(person))
						movie.getWrittenBy().add(person);
				}
			}

			if (directedByNames != null) {
				for (String directedByName : directedByNames) {
					Person person = (Person) queryPersonByName.execute(directedByName);
					if (person == null) {
						person = new Person();
						person.setName(directedByName);
						person = pm.makePersistent(person);
					}
					if (!movie.getDirectedBy().contains(person))
						movie.getDirectedBy().add(person);
				}
			}

			if (producedByNames != null) {
				for (String producedByName : producedByNames) {
					Person person = (Person) queryPersonByName.execute(producedByName);
					if (person == null) {
						person = new Person();
						person.setName(producedByName);
						person = pm.makePersistent(person);
					}
					if (!movie.getProducedBy().contains(person))
						movie.getProducedBy().add(person);
				}
			}

			movie.setTagLine(tagline);

			if (ratingName != null) {
				Rating rating = (Rating) queryRatingByName.execute(ratingName);
				if (rating == null) {
					rating = new Rating();
					rating.setName(ratingName);
					rating = pm.makePersistent(rating);
				}
				movie.setRating(rating);
			}
			else
				movie.setRating(null);

//			pm.flush();
		}
		r.close();
	}

	@Test
	public void query0() throws IOException
	{
		logger.info("query0: entered");
		Rating rating;
		{
			String ratingName = "R (USA)";

			Query q = pm.newQuery(Rating.class);
			q.setFilter("this.name == :name");
			q.setUnique(true);
			rating = (Rating) q.execute(ratingName);
			Assert.assertNotNull("No rating found with rating.name==\"" + ratingName + "\"!", rating);
		}

		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.rating == :rating");

		@SuppressWarnings("unchecked")
		List<Movie> movies = (List<Movie>) q.execute(rating);
		Assert.assertNotNull("Query returned null as result when a List was expected!", movies);
		logger.info("query0: found " + movies.size() + " movies with rating \"" + rating.getName() + "\":");
		Assert.assertEquals("Query returned wrong number of results!", 39, movies.size());
		for (Movie movie : movies) {
			Assert.assertNotNull("Query returned a movie whose rating is null!", movie.getRating());
			Assert.assertTrue("Query returned a movie whose rating is not \"" + rating.getName() + "\"!", rating.equals(movie.getRating()));
			logger.info("query0:   * " + movie.getMovieID() + ": " + movie.getName());
		}
	}

	@Test
	public void query1() throws IOException
	{
		logger.info("query1: entered");
		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.rating.name.indexOf(:ratingNamePart) >= 0");

		String ratingNamePart = "G";

		@SuppressWarnings("unchecked")
		List<Movie> movies = (List<Movie>) q.execute(ratingNamePart);
		Assert.assertNotNull("Query returned null as result when a List was expected!", movies);
		logger.info("query1: found " + movies.size() + " movies with rating.name containing \"" + ratingNamePart + "\":");
		Assert.assertEquals("Query returned wrong number of results!", 34, movies.size());
		for (Movie movie : movies) {
			Assert.assertNotNull("Query returned a movie whose rating is null!", movie.getRating());
			Assert.assertNotNull("Query returned a movie whose rating.name is null!", movie.getRating().getName());
			Assert.assertTrue("Query returned a movie whose rating.name does not contain \"" + ratingNamePart + "\"!", movie.getRating().getName().contains(ratingNamePart));
			logger.info("query1:   * " + movie.getMovieID() + ": " + movie.getName() + " (" + movie.getRating().getName() + ")");
		}
	}

	@Test
	public void query2() throws IOException
	{
		logger.info("query2: entered");
		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.rating.name == :ratingName");

		String ratingName = "PG-13 (USA)";

		@SuppressWarnings("unchecked")
		List<Movie> movies = (List<Movie>) q.execute(ratingName);
		Assert.assertNotNull("Query returned null as result when a List was expected!", movies);
		logger.info("query2: found " + movies.size() + " movies with rating.name == \"" + ratingName + "\":");
		Assert.assertEquals("Query returned wrong number of results!", 18, movies.size());
		for (Movie movie : movies) {
			Assert.assertNotNull("Query returned a movie whose rating is null!", movie.getRating());
			Assert.assertEquals("Query returned a movie whose rating.name is not equal to \"" + ratingName + "\"!", ratingName, movie.getRating().getName());
			logger.info("query2:   * " + movie.getMovieID() + ": " + movie.getName() + " (" + movie.getRating().getName() + ")");
		}
	}

	@Test
	public void query3() throws IOException
	{
		Person person;
		{
			String personName = "Katharine Hepburn";

			Query q = pm.newQuery(Person.class);
			q.setFilter("this.name == :name");
			q.setUnique(true);
			person = (Person) q.execute(personName);
			Assert.assertNotNull("No person found with person.name==\"" + personName + "\"!", person);
		}

		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.starring.contains(:person)");
		@SuppressWarnings("unchecked")
		List<Movie> movies = (List<Movie>) q.execute(person);
		Assert.assertNotNull("Query returned null as result when a List was expected!", movies);
		logger.info("query3: found " + movies.size() + " movies with starring.contains(\"" + person.getName() + "\"):");
		Assert.assertEquals("Query returned wrong number of results!", 2, movies.size());
		for (Movie movie : movies) {
			Assert.assertNotNull("Query returned a movie whose starring is null!", movie.getStarring());
			Assert.assertFalse("Query returned a movie whose starring is empty!", movie.getStarring().isEmpty());

			StringBuilder starringSB = new StringBuilder();
			for (Person p : movie.getStarring()) {
				Assert.assertNotNull("Query returned a movie whose starring contains a null entry!", p);

				if (starringSB.length() > 0)
					starringSB.append(", ");

				starringSB.append(p.getName());
			}

			logger.info("query3:   * " + movie.getMovieID() + ": " + movie.getName() + " (" + starringSB + ")");
		}
	}

	@Test
	public void query4() throws IOException
	{
		String personName = "Katharine Hepburn";

		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.starring.contains(personVariable) && personVariable.name == :personName");
		q.declareVariables(Person.class.getName() + " personVariable");

		@SuppressWarnings("unchecked")
		List<Movie> movies = (List<Movie>) q.execute(personName);
		Assert.assertNotNull("Query returned null as result when a List was expected!", movies);
		logger.info("query4: found " + movies.size() + " movies query=\"" + q + "\" and personName=\"" + personName + "\"):");
		Assert.assertEquals("Query returned wrong number of results!", 2, movies.size());
		for (Movie movie : movies) {
			Assert.assertNotNull("Query returned a movie whose starring is null!", movie.getStarring());
			Assert.assertFalse("Query returned a movie whose starring is empty!", movie.getStarring().isEmpty());

			StringBuilder starringSB = new StringBuilder();
			for (Person p : movie.getStarring()) {
				Assert.assertNotNull("Query returned a movie whose starring contains a null entry!", p);

				if (starringSB.length() > 0)
					starringSB.append(", ");

				starringSB.append(p.getName());
			}

			logger.info("query4:   * " + movie.getMovieID() + ": " + movie.getName() + " (" + starringSB + ")");
		}
	}

	@Test
	public void query5() throws IOException
	{
		String personNamePart = "Kat";

		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.starring.contains(personVariable) && personVariable.name.indexOf(:personNamePart) >= 0");
		q.declareVariables(Person.class.getName() + " personVariable");

		@SuppressWarnings("unchecked")
		List<Movie> movies = (List<Movie>) q.execute(personNamePart);
		Assert.assertNotNull("Query returned null as result when a List was expected!", movies);
		logger.info("query5: found " + movies.size() + " movies query=\"" + q + "\" and personNamePart=\"" + personNamePart + "\"):");
		Assert.assertEquals("Query returned wrong number of results!", 16, movies.size());
		for (Movie movie : movies) {
			Assert.assertNotNull("Query returned a movie whose starring is null!", movie.getStarring());
			Assert.assertFalse("Query returned a movie whose starring is empty!", movie.getStarring().isEmpty());

			StringBuilder starringSB = new StringBuilder();
			for (Person p : movie.getStarring()) {
				Assert.assertNotNull("Query returned a movie whose starring contains a null entry!", p);

				if (starringSB.length() > 0)
					starringSB.append(", ");

				starringSB.append(p.getName());
			}

			logger.info("query5:   * " + movie.getMovieID() + ": " + movie.getName() + " (" + starringSB + ")");
		}
	}
}
