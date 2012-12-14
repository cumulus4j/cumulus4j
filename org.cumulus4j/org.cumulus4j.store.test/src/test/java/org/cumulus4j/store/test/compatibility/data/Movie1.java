package org.cumulus4j.store.test.compatibility.data;

import javax.jdo.Query;

import junit.framework.Assert;

import org.cumulus4j.store.test.compatibility.CompatibilityTestData;
import org.cumulus4j.store.test.movie.Movie;
import org.cumulus4j.store.test.movie.Rating;

public class Movie1 extends CompatibilityTestData {

	@Override
	public String getSinceVersion() {
		return VERSION_1_0_0;
	}

	@Override
	public void create() {
		Movie movie1 = new Movie();
		movie1.setName("Movie1 1");
		Rating rating1 = new Rating();
		rating1.setName("Movie1.Rating 1");
		movie1.setRating(rating1);
		pm.makePersistent(movie1);

		Movie movie2 = new Movie();
		movie2.setName("Movie1 2");
		movie2.setRating(rating1);
		pm.makePersistent(movie2);

		Movie movie3 = new Movie();
		movie3.setName("Movie1 3");
		Rating rating2 = new Rating();
		rating2.setName("Movie1.Rating 2");
		movie3.setRating(rating2);
		pm.makePersistent(movie3);
	}

	@Override
	public void verify() {
		Movie movie1 = getMovieByName("Movie1 1");
		Assert.assertNotNull(movie1);
		Assert.assertNotNull(movie1.getRating());

		Movie movie2 = getMovieByName("Movie1 2");
		Assert.assertNotNull(movie2);
		Assert.assertNotNull(movie2.getRating());

		Movie movie3 = getMovieByName("Movie1 3");
		Assert.assertNotNull(movie3);
		Assert.assertNotNull(movie3.getRating());

		Assert.assertEquals("Movie1.Rating 1", movie1.getRating().getName());
		Assert.assertEquals("Movie1.Rating 1", movie2.getRating().getName());
		Assert.assertEquals("Movie1.Rating 2", movie3.getRating().getName());
		Assert.assertEquals(movie1.getRating(), movie2.getRating());
	}

	protected Movie getMovieByName(String name) {
		Query query = pm.newQuery(Movie.class);
		query.setFilter("this.name == :name");
		query.setUnique(true);
		return (Movie) query.execute(name);
	}

}
