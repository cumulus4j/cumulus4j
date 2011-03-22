package org.cumulus4j.test.collection.join;

import java.util.List;
import java.util.Map;

import javax.jdo.Query;

import org.cumulus4j.test.framework.AbstractTransactionalTest;
import org.cumulus4j.test.framework.CleanupUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringStringMapQueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(StringStringMapQueryTest.class);

	@BeforeClass
	public static void clearDatabase()
	throws Exception
	{
		logger.info("clearDatabase: Clearing database (dropping all tables).");
		CleanupUtil.dropAllTables();
	}

	@Before
	public void createTestData()
	{
		if (pm.getExtent(StringStringMapOwner.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		StringStringMapOwner stringStringMapOwner = new StringStringMapOwner();
		stringStringMapOwner.getMap().put("first", "Adam");
		stringStringMapOwner.getMap().put("second", "Bert");
		stringStringMapOwner.getMap().put("third", "Claudia");
		pm.makePersistent(stringStringMapOwner);

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
		stringStringMapOwner.getMap().put("first", "Adam");
		stringStringMapOwner.getMap().put("second", "Michael");

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
		stringStringMapOwner.getMap().put("first", "Daniel");
		stringStringMapOwner.getMap().put("second", "Marco");
		stringStringMapOwner.getMap().put("third", "Marc");
		stringStringMapOwner.getMap().put("fourth", "Khaled");

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
		stringStringMapOwner.getMap().put("first", "Marc");

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
		stringStringMapOwner.getMap().put("first", "Marco");

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
	}

	@Test
	public void query0()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(:key)");

		String key = "second";

		@SuppressWarnings("unchecked")
		List<StringStringMapOwner> resultList = (List<StringStringMapOwner>) q.execute(key);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);
		logger.info("query0: found " + resultList.size() + " containing the key \"" + key + "\":");
		Assert.assertEquals("Query returned wrong number of results!", 3, resultList.size());
		for (StringStringMapOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a StringStringMapOwner with the map property being null!", resultElement.getMap());

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a StringStringMapOwner whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a StringStringMapOwner whose map contains a null value!", mapEntry.getValue());

				if (sb.length() > 0)
					sb.append(", ");

				sb.append(mapEntry.getKey()).append('=').append(mapEntry.getValue());
			}

			logger.info("query0:   * " + resultElement.getId() + ": " + sb);
		}
	}

	@Test
	public void query1()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(:value)");

		String value = "Marc";

		@SuppressWarnings("unchecked")
		List<StringStringMapOwner> resultList = (List<StringStringMapOwner>) q.execute(value);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);
		logger.info("query0: found " + resultList.size() + " containing the value \"" + value + "\":");
		Assert.assertEquals("Query returned wrong number of results!", 2, resultList.size());
		for (StringStringMapOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a StringStringMapOwner with the map property being null!", resultElement.getMap());

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a StringStringMapOwner whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a StringStringMapOwner whose map contains a null value!", mapEntry.getValue());

				if (sb.length() > 0)
					sb.append(", ");

				sb.append(mapEntry.getKey()).append('=').append(mapEntry.getValue());
			}

			logger.info("query0:   * " + resultElement.getId() + ": " + sb);
		}
	}
}
