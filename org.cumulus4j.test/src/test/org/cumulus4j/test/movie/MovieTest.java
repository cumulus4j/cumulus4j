package org.cumulus4j.test.movie;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.jdo.Query;

import org.cumulus4j.test.core.AbstractTransactionalTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(MovieTest.class);

	@Test
	public void createData()
	throws Exception
	{
		pm.getExtent(Movie.class);

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
				new InputStreamReader(MovieTest.class.getResourceAsStream("data.csv"), "UTF-8")
		);
		String line;
		while ((line = r.readLine()) != null) {
			String[] fields = line.split("\t");
			int fieldNo = -1;
			String movieName = fields.length <= ++fieldNo ? null : fields[fieldNo];
			String starringName = fields.length <= ++fieldNo ? null : fields[fieldNo];
			String[] writtenByNames = fields.length <= ++fieldNo ? null : fields[fieldNo].split(",");
			String[] languageNames = fields.length <= ++fieldNo ? null : fields[fieldNo].split(",");
			String[] directedByNames = fields.length <= ++fieldNo ? null : fields[fieldNo].split(",");
			String[] producedByNames = fields.length <= ++fieldNo ? null : fields[fieldNo].split(",");
			String tagline = fields.length <= ++fieldNo ? null : fields[fieldNo];
			String initialReleaseDate = fields.length <= ++fieldNo ? null : fields[fieldNo];
			String ratingName = fields.length <= ++fieldNo ? null : fields[fieldNo];
			String estimatedBudget = fields.length <= ++fieldNo ? null : fields[fieldNo];
			String sequel = fields.length <= ++fieldNo ? null : fields[fieldNo];
			String prequel = fields.length <= ++fieldNo ? null : fields[fieldNo];

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

			pm.flush();
		}
		r.close();
	}

	@Test
	public void query0() throws IOException
	{
		Rating rating = pm.getExtent(Rating.class).iterator().next();

		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.rating == :rating");

		@SuppressWarnings("unchecked")
		List<Movie> movies = (List<Movie>) q.execute(rating);
		logger.info("QueryDataTransRunnable0.run: found " + movies.size() + " movies with rating \"" + rating.getName() + "\":");
		for (Movie movie : movies) {
			logger.info("QueryDataTransRunnable0.run:   * " + movie.getMovieID() + ": " + movie.getName());
		}
	}

	@Test
	public void query1() throws IOException
	{
		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.rating.name.indexOf(:ratingNamePart) >= 0");

		String ratingNamePart = "G";

		@SuppressWarnings("unchecked")
		List<Movie> movies = (List<Movie>) q.execute(ratingNamePart);
		logger.info("QueryDataTransRunnable0.run: found " + movies.size() + " movies with rating.name containing \"" + ratingNamePart + "\":");
		for (Movie movie : movies) {
			logger.info("QueryDataTransRunnable0.run:   * " + movie.getMovieID() + ": " + movie.getName() + " (" + movie.getRating().getName() + ")");
		}
	}

	@Test
	public void query2() throws IOException
	{
		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.rating.name == :ratingName");

		String ratingName = "PG-13 (USA)";

		@SuppressWarnings("unchecked")
		List<Movie> movies = (List<Movie>) q.execute(ratingName);
		logger.info("QueryDataTransRunnable0.run: found " + movies.size() + " movies with rating.name == \"" + ratingName + "\":");
		for (Movie movie : movies) {
			logger.info("QueryDataTransRunnable0.run:   * " + movie.getMovieID() + ": " + movie.getName() + " (" + movie.getRating().getName() + ")");
		}
	}

}
