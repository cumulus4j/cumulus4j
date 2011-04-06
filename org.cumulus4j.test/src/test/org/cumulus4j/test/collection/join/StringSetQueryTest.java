package org.cumulus4j.test.collection.join;

import java.util.List;

import javax.jdo.Query;

import org.cumulus4j.test.framework.AbstractTransactionalTest;
import org.cumulus4j.test.framework.CleanupUtil;
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

	private void executeQueryAndCheckResult(Query q, String queryParam, boolean indexOf, int expectedResultListSize, boolean negated)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<StringSetOwner> resultList = (List<StringSetOwner>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceAll(".* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " StringSetOwners for query-filter \"" + f + "\" and param \"" + queryParam + "\":");
		Assert.assertEquals("Query returned wrong number of results!", expectedResultListSize, resultList.size());
		for (StringSetOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a StringSetOwner with the set property being null!", resultElement.getSet());

			boolean resultElementMatches = false;
			StringBuilder sb = new StringBuilder();
			for (String setElement : resultElement.getSet()) {
				Assert.assertNotNull("Query returned a StringSetOwner whose set contains a null entry!", setElement);
				if (indexOf) {
					if (negated) {
						if (setElement.indexOf(queryParam) < 0)
							resultElementMatches = true;
					}
					else {
						if (setElement.indexOf(queryParam) >= 0)
							resultElementMatches = true;
					}
				}
				else {
					if (negated) {
						if (!setElement.equals(queryParam))
							resultElementMatches = true;
					}
					else {
						if (setElement.equals(queryParam))
							resultElementMatches = true;
					}
				}

				if (sb.length() > 0)
					sb.append(", ");

				sb.append(setElement);
			}
			if (resultElement.getSet().isEmpty() && negated)
				resultElementMatches = true;

			logger.info(testMethodName + ":   * " + resultElement.getId() + ": " + sb);
			Assert.assertTrue(
					"Query returned a StringSetOwner with the set property not containing the searched element: " + resultElement.getId(),
					resultElementMatches
			);
		}
	}

	@Test
	public void queryContainsParameter()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(:element)");
		executeQueryAndCheckResult(q, "Marc", false, 2, false);
	}

	@Test
	public void queryContainsVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable.indexOf(:elementPart) >= 0");
		executeQueryAndCheckResult(q, "Marc", true, 3, false);
	}

	@Test
	public void queryContainsVariableAndVariableEquals()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable == :element");
		executeQueryAndCheckResult(q, "Marc", false, 2, false);
	}

	@Test
	public void queryNotContainsParameter()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("!this.set.contains(:element)");
		executeQueryAndCheckResult(q, "Marc", false, 5, true);
	}

	@Test
	public void queryContainsVariableAndVariableNotIndexOf()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable.indexOf(:elementPart) < 0");
		executeQueryAndCheckResult(q, "Marc", true, 4, true);
	}

	@Test
	public void queryContainsVariableAndNotVariableIndexOf()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable.indexOf(:elementPart) >= 0)");
		executeQueryAndCheckResult(q, "Marc", true, 4, true);
	}

	@Test
	public void queryContainsVariableAndVariableNotEquals()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable != :element");
		executeQueryAndCheckResult(q, "Marc", false, 5, true);
	}

	@Test
	public void queryContainsVariableAndNotVariableEquals()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable == :element)");
		executeQueryAndCheckResult(q, "Marc", false, 5, true);
	}

	@Test
	public void queryNotContainsVariableAndVariableEquals()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("!this.set.contains(elementVariable) && elementVariable == :element");
		executeQueryAndCheckResult(q, "Marc", false, 5, true);
	}
}
