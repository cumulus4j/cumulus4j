package org.cumulus4j.test.collection.join;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.jdo.Query;

import org.cumulus4j.test.framework.AbstractTransactionalTest;
import org.cumulus4j.test.framework.CleanupUtil;
import org.datanucleus.util.NucleusLogger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringSetQueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(StringSetQueryTest.class);

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
		if (pm.getExtent(StringSetOwner.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		StringSetOwner stringSetOwner = new StringSetOwner();
		stringSetOwner.getSet().add("Adam");
		stringSetOwner.getSet().add("Bert");
		stringSetOwner.getSet().add("Claudia");
		pm.makePersistent(stringSetOwner);

		stringSetOwner = pm.makePersistent(new StringSetOwner());
		stringSetOwner.getSet().add("Adam");
		stringSetOwner.getSet().add("Michael");

		stringSetOwner = pm.makePersistent(new StringSetOwner());
		stringSetOwner.getSet().add("Daniel");
		stringSetOwner.getSet().add("Marco");
		stringSetOwner.getSet().add("Marc");
		stringSetOwner.getSet().add("Khaled");

		stringSetOwner = pm.makePersistent(new StringSetOwner());
		stringSetOwner.getSet().add("Marc");

		stringSetOwner = pm.makePersistent(new StringSetOwner());
		stringSetOwner.getSet().add("Marco");

		stringSetOwner = pm.makePersistent(new StringSetOwner());

		stringSetOwner = pm.makePersistent(new StringSetOwner());
		stringSetOwner.getSet().add("David");
	}

	private void executeQueryAndCheckResult(Query q, String queryParam, long ... expectedIDs)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<StringSetOwner> resultList = (List<StringSetOwner>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceFirst("^.* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " StringSetOwners for query-filter \"" + f + "\" and param \"" + queryParam + "\":");

		Set<Long> expectedIDSet = new HashSet<Long>(expectedIDs.length);
		for (long id : expectedIDs)
			expectedIDSet.add(id);

		for (StringSetOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a StringSetOwner with the set property being null!", resultElement.getSet());
			boolean expectedElement = expectedIDSet.remove(resultElement.getId());

			StringBuilder sb = new StringBuilder();
			for (String setElement : resultElement.getSet()) {
				Assert.assertNotNull("Query returned a StringSetOwner whose set contains a null entry!", setElement);

				if (sb.length() > 0)
					sb.append(", ");

				sb.append(setElement);
			}

			logger.info(testMethodName + ":   * " + resultElement.getId() + ": " + sb);
			Assert.assertTrue(
					"Query returned an unexpected result-element: " + resultElement.getId(),
					expectedElement
			);
		}

		if (!expectedIDSet.isEmpty())
			Assert.fail("Query did not return the following expected result-elements: " + expectedIDSet);
	}

	@Test
	public void queryContainsParameter()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(:element)");
		executeQueryAndCheckResult(q, "Marc", 3, 4);
	}

	@Test
	public void queryContainsVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable.indexOf(:elementPart) >= 0");
		executeQueryAndCheckResult(q, "Marc", 3, 4, 5);
	}

	@Test
	public void queryContainsVariableAndVariableEquals()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable == :element");
		executeQueryAndCheckResult(q, "Marc", 3, 4);
	}

	@Test
	public void queryNotContainsParameter()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("!this.set.contains(:element)");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 5, 6, 7);
	}

	@Test
	public void queryContainsVariableAndVariableNotIndexOf()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable.indexOf(:elementPart) < 0");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 3, 7);
	}

	@Test
	public void queryContainsVariableAndNotVariableIndexOf()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable.indexOf(:elementPart) >= 0)");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 3, 7);
	}

	@Test
	public void queryContainsVariableAndVariableNotEquals()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable != :element");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 3, 5, 7);
	}

	@Test
	public void queryContainsVariableAndNotVariableEquals()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable == :element)");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 3, 5, 7);
	}

	@Test
	public void queryNotContainsVariableAndVariableEquals()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("!this.set.contains(elementVariable) && elementVariable == :element");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 5, 6, 7);
	}

	@Test
	public void queryIsEmpty()
	{
		NucleusLogger.GENERAL.info(">> queryIsEmpty");
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.isEmpty()");

		List<StringSetOwner> resultList = (List<StringSetOwner>) q.execute();
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);
		Assert.assertEquals("Number of results is incorrect", 1, resultList.size());
	}
}
