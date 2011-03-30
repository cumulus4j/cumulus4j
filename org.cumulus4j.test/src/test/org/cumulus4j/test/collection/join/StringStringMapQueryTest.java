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

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
		stringStringMapOwner.getMap().put("fourth", "Khaled");
	}

	private static enum KeyValue {
		key, value
	}

	private void executeQueryAndCheckResult(Query q, String queryParam, KeyValue keyValue, boolean indexOf, int expectedResultListSize)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<StringStringMapOwner> resultList = (List<StringStringMapOwner>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String logMsgPart = indexOf ? "containing at least one " + keyValue + " which contains the part " : "containing the " + keyValue;
		logger.info(testMethodName + ": found " + resultList.size() + " StringStringMapOwners " + logMsgPart + " \"" + queryParam + "\":");
		Assert.assertEquals("Query returned wrong number of results!", expectedResultListSize, resultList.size());
		for (StringStringMapOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a StringStringMapOwner with the map property being null!", resultElement.getMap());

			boolean resultElementMatches = false;
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a StringStringMapOwner whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a StringStringMapOwner whose map contains a null value!", mapEntry.getValue());

				String found = null;
				switch (keyValue) {
					case key:
						found = mapEntry.getKey();
						break;
					case value:
						found = mapEntry.getValue();
						break;
				}
				if (indexOf) {
					if (found.indexOf(queryParam) >= 0)
						resultElementMatches = true;
				}
				else {
					if (found.equals(queryParam))
						resultElementMatches = true;
				}


				if (sb.length() > 0)
					sb.append(", ");

				sb.append(mapEntry.getKey()).append('=').append(mapEntry.getValue());
			}

			logger.info(testMethodName + ":   * " + resultElement.getId() + ": " + sb);
			Assert.assertTrue(
					"Query returned a StringStringMapOwner with the map property not containing the searched " + keyValue + ": " + resultElement.getId(),
					resultElementMatches
			);
		}
	}

	@Test
	public void queryContainsKeyParameter()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, "second", KeyValue.key, false, 3);
	}

	@Test
	public void queryContainsValueParameter()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, "Marc", KeyValue.value, false, 2);
	}

	@Test
	public void queryContainsKeyVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "h", KeyValue.key, true, 3);
	}

	@Test
	public void queryContainsValueVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "Marc", KeyValue.value, true, 3);
	}

	@Test
	public void queryContainsKeyVariableAndVariableEquals()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable == :queryParam");
		executeQueryAndCheckResult(q, "second", KeyValue.key, false, 3);
	}

	@Test
	public void queryContainsValueVariableAndVariableEquals()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable == :queryParam");
		executeQueryAndCheckResult(q, "Marc", KeyValue.value, false, 2);
	}
}
