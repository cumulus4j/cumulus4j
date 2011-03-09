package org.cumulus4j.datanucleus.test2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.apache.log4j.Logger;
import org.cumulus4j.datanucleus.test.CleanupUtil;

public class Main2 {
	private static final Logger logger = Logger.getLogger(Main2.class);

	private PersistenceManagerFactory persistenceManagerFactory;

	private PersistenceManager createPersistenceManager()
	{
		if (persistenceManagerFactory == null)
			persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory("datanucleus.properties");

		return persistenceManagerFactory.getPersistenceManager();
	}

	public void closePersistenceManagerFactory()
	{
		if (this.persistenceManagerFactory != null) {
			this.persistenceManagerFactory.close();
			this.persistenceManagerFactory = null;
		}
	}

	public static interface TransRunnable {
		public void run(PersistenceManager pm) throws Exception;
	}

	public void executeInTransaction(TransRunnable runner) throws Exception {
		PersistenceManager pm = createPersistenceManager();
		try {
			try {
				pm.currentTransaction().begin();

				runner.run(pm);

				pm.currentTransaction().commit();
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();
			}
		} finally {
			pm.close();
		}
	}

	private static class InitialiseMetaDataTransRunnable implements TransRunnable
	{
		public void run(PersistenceManager pm)
		throws Exception
		{
			pm.getExtent(Movie.class);
			pm.getExtent(Person.class);
			pm.getExtent(Rating.class);
			pm.getExtent(Language.class);
		}
	}

	private static class DeleteEntitiesTransRunnable implements TransRunnable
	{
		public void run(PersistenceManager pm)
		throws Exception
		{
			{
				Iterator<?> it = pm.getExtent(Movie.class).iterator();
				while (it.hasNext())
					pm.deletePersistent(it.next());
			}

			{
				Iterator<?> it = pm.getExtent(Person.class).iterator();
				while (it.hasNext())
					pm.deletePersistent(it.next());
			}

			{
				Iterator<?> it = pm.getExtent(Rating.class).iterator();
				while (it.hasNext())
					pm.deletePersistent(it.next());
			}

			{
				Iterator<?> it = pm.getExtent(Language.class).iterator();
				while (it.hasNext())
					pm.deletePersistent(it.next());
			}
		}
	}

	private static class CreateDataTransRunnable implements TransRunnable
	{
		public void run(PersistenceManager pm)
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
					new InputStreamReader(Main2.class.getResourceAsStream("data.csv"), "UTF-8")
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

//				System.out.println("movieName = " + movieName);

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


			}
			r.close();
		}
	}

	private static class QueryDataTransRunnable0 implements TransRunnable
	{
		public void run(PersistenceManager pm) throws IOException
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
	}

//	private static class UpdateDataTransRunnable0 implements TransRunnable
//	{
//		public void run(PersistenceManager pm) throws IOException
//		{
//			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
//			localAccountantDelegate.setName("Test 0000");
//			localAccountantDelegate.setDescription(
//					"This is a very long description bla bla bla trallalala tröt tröt. And " +
//					"I don't know exactly what I should write here, bla bla bla, but it should " +
//					"be really really long! Very likely this is sufficient now, but I'd better " +
//					"add some more words.\n\n" +
//					"\n" +
//					"Freude, schöner Götterfunken,\n" +
//					"Tochter aus Elisium,\n" +
//					"Wir betreten feuertrunken\n" +
//					"Himmlische, dein Heiligthum.\n" +
//					"Deine Zauber binden wieder,\n" +
//					"was der Mode Schwerd getheilt;\n" +
//					"Bettler werden Fürstenbrüder,\n" +
//					"wo dein sanfter Flügel weilt.\n" +
//					"\n" +
//					"Seid umschlungen, Millionen!\n" +
//					"Diesen Kuß der ganzen Welt!\n" +
//					"Brüder – überm Sternenzelt\n" +
//					"muß ein lieber Vater wohnen.\n" +
//					"\n" +
//					"Wem der große Wurf gelungen,\n" +
//					"eines Freundes Freund zu seyn;\n" +
//					"wer ein holdes Weib errungen,\n" +
//					"mische seinen Jubel ein!\n" +
//					"Ja – wer auch nur eine Seele\n" +
//					"sein nennt auf dem Erdenrund!\n" +
//					"Und wer’s nie gekonnt, der stehle\n" +
//					"weinend sich aus diesem Bund!\n"
//			);
//		}
//	}
//
//	private static class UpdateDataTransRunnable1 implements TransRunnable
//	{
//		public void run(PersistenceManager pm) throws IOException
//		{
//			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
//			localAccountantDelegate.setAccount("CHF", new Account(ACCOUNT_ID_1));
//			localAccountantDelegate.setName("New test bla bla bla.");
//			localAccountantDelegate.setDescription("description");
//		}
//	}
//
//	private static class QueryDataTransRunnable1 implements TransRunnable
//	{
//		public void run(PersistenceManager pm) throws IOException
//		{
//			LocalAccountantDelegate localAccountantDelegate = pm.getExtent(LocalAccountantDelegate.class).iterator().next();
//			localAccountantDelegate.test();
//		}
//	}
//
//	private static class QueryDataTransRunnable2 implements TransRunnable
//	{
//		public void run(PersistenceManager pm) throws IOException
//		{
//			Query q = pm.newQuery(LocalAccountantDelegate.class);
////			q.setFilter("this.name.indexOf(:needle) >= 0");
//			q.setFilter("this.name == :name");
//
//			@SuppressWarnings("unchecked")
//			List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("New test bla bla bla.");
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println("result.size=" + result.size());
//			for (LocalAccountantDelegate localAccountantDelegate : result) {
//				System.out.println("  * " + localAccountantDelegate);
//			}
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println();
//		}
//	}
//
//	private static class QueryDataTransRunnable3 implements TransRunnable
//	{
//		public void run(PersistenceManager pm) throws IOException
//		{
//			Query q = pm.newQuery(LocalAccountantDelegate.class);
//			q.setFilter("this.name == :pName && this.description == :pDesc");
//
//			@SuppressWarnings("unchecked")
//			List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute(
//					"New test bla bla bla.", "description"
//			);
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println("result.size=" + result.size());
//			for (LocalAccountantDelegate localAccountantDelegate : result) {
//				System.out.println("  * " + localAccountantDelegate);
//			}
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println();
//		}
//	}
//
//	private static class QueryDataTransRunnable4 implements TransRunnable
//	{
//		public void run(PersistenceManager pm) throws IOException
//		{
//			Query q = pm.newQuery(LocalAccountantDelegate.class);
//			q.setFilter("this.name.indexOf(:needle) >= 0");
//
//			@SuppressWarnings("unchecked")
//			List<LocalAccountantDelegate> result = (List<LocalAccountantDelegate>) q.execute("bla");
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println("result.size=" + result.size());
//			for (LocalAccountantDelegate localAccountantDelegate : result) {
//				System.out.println("  * " + localAccountantDelegate);
//			}
//			System.out.println();
//			System.out.println();
//			System.out.println();
//			System.out.println();
//		}
//	}
//
//	private static class DeleteDataTransRunnable implements TransRunnable
//	{
//		public void run(PersistenceManager pm) throws IOException
//		{
//			LocalAccountantDelegate localAccountantDelegate = (LocalAccountantDelegate) pm.getObjectById(LOCAL_ACCOUNTANT_DELEGATE_ID_0);
//			pm.deletePersistent(localAccountantDelegate);
//
//			Account account = (Account) pm.getObjectById(ACCOUNT_ID_0);
//			pm.deletePersistent(account);
//
//			account = (Account) pm.getObjectById(ACCOUNT_ID_1);
//			pm.deletePersistent(account);
//		}
//	}

	public static void main(String[] args)
	{
		try {
			Main2 test = new Main2();

			Enumeration<URL> resources = test.getClass().getClassLoader().getResources("plugin.xml");
			while (resources.hasMoreElements()) {
				URL url = resources.nextElement();
				logger.info(url);
			}

			long startTimestamp;

			// Ensure we have a completely empty database, before we start up DataNucleus.
			if (true)
				CleanupUtil.dropAllTables();

			startTimestamp = System.currentTimeMillis();
			InitialiseMetaDataTransRunnable initialiseMetaDataTransRunnable = new InitialiseMetaDataTransRunnable();
			test.executeInTransaction(initialiseMetaDataTransRunnable);
			logger.info("*** Initialising meta-data took " + (System.currentTimeMillis() - startTimestamp) + " msec ***");

			startTimestamp = System.currentTimeMillis();
			DeleteEntitiesTransRunnable deleteEntitiesTransRunnable = new DeleteEntitiesTransRunnable();
			test.executeInTransaction(deleteEntitiesTransRunnable);
			logger.info("*** Deleting data took " + (System.currentTimeMillis() - startTimestamp) + " msec ***");

			// Create the data required for our test.
			startTimestamp = System.currentTimeMillis();
			CreateDataTransRunnable createDataTransRunnable = new CreateDataTransRunnable();
			test.executeInTransaction(createDataTransRunnable);
			logger.info("*** Creating data took " + (System.currentTimeMillis() - startTimestamp) + " msec ***");

			logger.info("*** Executing query 0 ***");
			QueryDataTransRunnable0 queryDataTransRunnable0 = new QueryDataTransRunnable0();
			test.executeInTransaction(queryDataTransRunnable0);
			logger.info("*** Successfully executed query 0 ***");

//			logger.info("*** Update data 0 ***");
//			UpdateDataTransRunnable0 updateDataTransRunnable0 = new UpdateDataTransRunnable0();
//			test.executeInTransaction(updateDataTransRunnable0);
//			logger.info("*** Successfully updated data 0 ***");
//
//			logger.info("*** Update data 1 ***");
//			UpdateDataTransRunnable1 updateDataTransRunnable1 = new UpdateDataTransRunnable1();
//			test.executeInTransaction(updateDataTransRunnable1);
//			logger.info("*** Successfully updated data 1 ***");
//
//			logger.info("*** Executing query 1 ***");
//			QueryDataTransRunnable1 queryDataTransRunnable1 = new QueryDataTransRunnable1();
//			test.executeInTransaction(queryDataTransRunnable1);
//			logger.info("*** Successfully executed query 1 ***");
//
//			logger.info("*** Executing query 2 ***");
//			QueryDataTransRunnable2 queryDataTransRunnable2 = new QueryDataTransRunnable2();
//			test.executeInTransaction(queryDataTransRunnable2);
//			logger.info("*** Successfully executed query 2 ***");
//
////			logger.info("*** Executing query 3 ***");
////			QueryDataTransRunnable3 queryDataTransRunnable3 = new QueryDataTransRunnable3();
////			test.executeInTransaction(queryDataTransRunnable3);
////			logger.info("*** Successfully executed query 3 ***");
//
//			logger.info("*** Executing query 4 ***");
//			QueryDataTransRunnable4 queryDataTransRunnable4 = new QueryDataTransRunnable4();
//			test.executeInTransaction(queryDataTransRunnable4);
//			logger.info("*** Successfully executed query 4 ***");
//
//			logger.info("*** Deleting data ***");
//			DeleteDataTransRunnable deleteDataTransRunnable = new DeleteDataTransRunnable();
//			test.executeInTransaction(deleteDataTransRunnable);
//			logger.info("*** Successfully deleted data ***");
		} catch (Throwable e) {
			logger.error("main: " + e.getLocalizedMessage(), e);
		}
	}
}
