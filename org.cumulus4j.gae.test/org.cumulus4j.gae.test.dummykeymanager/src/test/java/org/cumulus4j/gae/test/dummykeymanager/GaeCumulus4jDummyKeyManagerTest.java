package org.cumulus4j.gae.test.dummykeymanager;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.test.datatype.StandardOneToOneTypesEntity;
import org.cumulus4j.store.test.movie.Movie;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class GaeCumulus4jDummyKeyManagerTest {

	private final LocalServiceTestHelper helper =
			new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
				.setEnvIsLoggedIn(true)
				.setEnvAuthDomain("localhost")
				.setEnvEmail("test@localhost");

	private DatastoreService ds;
	private PersistenceManagerFactory pmf;
	private PersistenceManager pm;

	@Before
	public void before() {
		helper.setUp();

		Map<String, String> props = new HashMap<String, String>();
	    props.put("datanucleus.appengine.BigDecimalsEncoding", "String");
	    ds = DatastoreServiceFactory.getDatastoreService();
	    pmf = JDOHelper.getPersistenceManagerFactory(props, "transactions-optional");
	    pm = pmf.getPersistenceManager();

		String keyStoreID = "dummy123";
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, keyStoreID + '_' + UUID.randomUUID() + '*' + UUID.randomUUID());
	}

	@After
	public void after() {
		if (pm != null)
			pm.close();
		pm = null;

		if (pmf != null)
			pmf.close();
		pmf = null;

		helper.tearDown();
	}

	@Test
	public void writeReadStandardOneToOneTypesEntity() {
		{
			StandardOneToOneTypesEntity entity = new StandardOneToOneTypesEntity();
			entity.setUuid(UUID.randomUUID());
			entity.setString("Bla bla trallala");
			entity.setLongPrimitive(123456);
			entity.setDate(new Date());
			pm.makePersistent(entity);
		}

		{
			StandardOneToOneTypesEntity entity = new StandardOneToOneTypesEntity();
			entity.setUuid(UUID.randomUUID());
			entity.setString("Oink oink");
			entity.setLongPrimitive(666);
			entity.setDate(new Date());
			pm.makePersistent(entity);
		}

		pm.flush();

		Query q = pm.newQuery(StandardOneToOneTypesEntity.class);
		@SuppressWarnings("unchecked")
		Collection<StandardOneToOneTypesEntity> c = (Collection<StandardOneToOneTypesEntity>) q.execute();

		Assert.assertNotNull(c);
		boolean entity1found = false;
		boolean entity2found = false;
		for (StandardOneToOneTypesEntity entity : c) {
			System.out.println("******************");
			System.out.println("uuid = " + entity.getUuid());
			System.out.println("string = " + entity.getString());
			System.out.println("longPrimitive = " + entity.getLongPrimitive());
			System.out.println("date = " + entity.getDate());

			if (entity.getLongPrimitive() == 123456) {
				entity1found = true;
				Assert.assertEquals("Bla bla trallala", entity.getString());
			}

			if (entity.getLongPrimitive() == 666) {
				entity2found = true;
				Assert.assertEquals("Oink oink", entity.getString());
			}
		}

		Assert.assertTrue(entity1found);
		Assert.assertTrue(entity2found);
	}

	@Test
	public void writeReadMovie() {
		{
			Movie entity1 = new Movie();
			entity1.setName("Bla bla trallala");
			pm.makePersistent(entity1);

			Movie entity2 = new Movie();
			entity2.setName("Oink oink");
			pm.makePersistent(entity2);

			pm.flush();
		}

		Query q = pm.newQuery(Movie.class);
		@SuppressWarnings("unchecked")
		Collection<Movie> c = (Collection<Movie>) q.execute();

		Assert.assertNotNull(c);
		boolean entity1found = false;
		boolean entity2found = false;
		for (Movie entity : c) {
			System.out.println("******************");
			System.out.println("movieID = " + entity.getMovieID());
			System.out.println("name = " + entity.getName());

			if ("Bla bla trallala".equals(entity.getName())) {
				entity1found = true;
			}

			if ("Oink oink".equals(entity.getName())) {
				entity2found = true;
			}
		}

		Assert.assertTrue(entity1found);
		Assert.assertTrue(entity2found);
	}

	@Test
	public void queryMovie() {
		{
			Movie entity1 = new Movie();
			entity1.setName("Lord of the rings");
			pm.makePersistent(entity1);

			Movie entity2 = new Movie();
			entity2.setName("Loose change");
			pm.makePersistent(entity2);
		}

		pm.flush();

		Query q = pm.newQuery(Movie.class);
		q.setFilter("this.name == :name");

		@SuppressWarnings("unchecked")
		Collection<Movie> c = (Collection<Movie>) q.execute("Loose change");

		Assert.assertNotNull(c);
		boolean entity2found = false;
		int foundQty = 0;
		for (Movie entity : c) {
			++foundQty;
			System.out.println("******************");
			System.out.println("movieID = " + entity.getMovieID());
			System.out.println("name = " + entity.getName());

			if ("Loose change".equals(entity.getName())) {
				entity2found = true;
			}
		}

		Assert.assertTrue(entity2found);
		Assert.assertEquals(1, foundQty);
	}
}

