/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
