package org.cumulus4j.gae.test.dummykeymanager;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class GaeNativeApiTest {

	private final LocalServiceTestHelper helper =
			new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig())
				.setEnvIsLoggedIn(true)
				.setEnvAuthDomain("localhost")
				.setEnvEmail("test@localhost");

	@Before
	public void before() {
		helper.setUp();
	}

	@After
	public void after() {
		helper.tearDown();
	}

	@Test
	public void writeRead() {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	    Key guestbookKey = KeyFactory.createKey("Guestbook", UUID.randomUUID().toString());
	    {
	    	String content = "Hello world! Bla bla bla. And you should know!";
	    	Date date = new Date();
	    	Entity greeting = new Entity("Greeting", guestbookKey);
	    	greeting.setProperty("user", "me");
	    	greeting.setProperty("date", date);
	    	greeting.setProperty("content", content);

	    	datastore.put(greeting);
	    }

	    Query query = new Query("Greeting", guestbookKey).addSort("date", Query.SortDirection.DESCENDING);
	    List<Entity> greetings = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(5));
	    Assert.assertNotNull(greetings);

	    boolean found = false;
	    for (Entity entity : greetings) {
	    	Date date = (Date) entity.getProperty("date");
	    	String user = (String) entity.getProperty("user");
	    	String content = (String) entity.getProperty("content");

	    	System.out.println("*********************");
	    	System.out.println("date = " + date);
	    	System.out.println("user = " + user);
	    	System.out.println("content = " + content);

	    	if (content.contains("Hello world!"))
	    		found = true;
		}

	    Assert.assertTrue(found);
	}
}
