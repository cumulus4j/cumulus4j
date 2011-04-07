package org.cumulus4j.test.collection.join;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.Query;

import org.cumulus4j.test.framework.AbstractTransactionalTest;
import org.cumulus4j.test.framework.CleanupUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElementAMap1QueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(ElementAMap1QueryTest.class);

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
		if (pm.getExtent(ElementAMapOwner1.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		ElementA elementA_1_1 = new ElementA("Element 1.1");

		{
			ElementAMapOwner1 owner = new ElementAMapOwner1();
			owner.setName("Owner 1");
			owner.putMapEntry("aaa", elementA_1_1);
			owner.putMapEntry("bbb", new ElementA("Element 1.2"));
			owner.putMapEntry("ccc", new ElementA("Element 1.3"));
			owner.putMapEntry("ddd", new ElementA("Element 1.4"));
			pm.makePersistent(owner);
		}

		{
			ElementAMapOwner1 owner = pm.makePersistent(new ElementAMapOwner1());
			owner.setName("Owner 2");
			owner.putMapEntry("aa", new ElementA("Element 2.1"));
			owner.putMapEntry("bb", new ElementA("Element 2.2"));
			owner.putMapEntry("cc", new ElementA("Element 2.3"));
			owner.putMapEntry("dd", new ElementA("Element 2.4"));
		}

		{
			ElementAMapOwner1 owner = pm.makePersistent(new ElementAMapOwner1());
			owner.setName("Owner 3");
			owner.putMapEntry("a", elementA_1_1);
			owner.putMapEntry("b", new ElementA("Element 3.2"));
			owner.putMapEntry("c", new ElementA("Element 3.3"));
		}

		{
			ElementAMapOwner1 owner = pm.makePersistent(new ElementAMapOwner1());
			owner.setName("Owner 4");
			owner.putMapEntry("ccc", new ElementA("Element 4.3"));
		}

		{
			ElementAMapOwner1 owner = pm.makePersistent(new ElementAMapOwner1());
			owner.setName("Owner 5");
		}

		{
			ElementAMapOwner1 owner = pm.makePersistent(new ElementAMapOwner1());
			owner.setName("Owner 6");
			owner.putMapEntry("bb", new ElementA("Element 6.2"));
		}
	}

	private void executeQueryAndCheckResult(Query q, Object queryParam, String ... expectedOwnerNames)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<ElementAMapOwner1> resultList = (List<ElementAMapOwner1>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceFirst("^.* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " ElementAMapOwner1s for query-filter \"" + f + "\" and param \"" + queryParam + "\":");

		Set<String> expectedOwnerNameSet = new HashSet<String>(Arrays.asList(expectedOwnerNames));

		for (ElementAMapOwner1 resultElement : resultList) {
			Assert.assertNotNull("Query returned a ElementAMapOwner1 with the 'name' property being null: " + resultElement, resultElement.getName());
			Assert.assertNotNull("Query returned a ElementAMapOwner1 with the 'map' property being null!", resultElement.getMap());

			boolean expectedElement = expectedOwnerNameSet.remove(resultElement.getName());

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, ElementA> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a ElementAMapOwner1 whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a ElementAMapOwner1 whose map contains a null value!", mapEntry.getValue());

				if (sb.length() > 0)
					sb.append(", ");

				sb.append(mapEntry.getKey()).append('=').append(mapEntry.getValue());
			}

			logger.info(testMethodName + ":   * " + resultElement.getName() + ": " + sb);
			Assert.assertTrue(
					"Query returned an unexpected result-element: " + resultElement,
					expectedElement
			);
		}

		if (!expectedOwnerNameSet.isEmpty())
			Assert.fail("Query did not return the following expected result-elements: " + expectedOwnerNameSet);
	}

	private ElementA getExampleElement()
	{
		return getExampleElement("Element 3.2");
	}

	private ElementA getExampleElement(String name)
	{
		Query q = pm.newQuery(ElementA.class);
		q.setFilter("this.name == :name");
		q.setUnique(true);
		ElementA element = (ElementA) q.execute(name);
		if (element == null)
			throw new IllegalStateException("No matching element found!");

		return element;
	}

	@Test
	public void queryContainsKeyParameter()
	{
		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, "ccc", "Owner 1", "Owner 4");
	}

	@Test
	public void queryContainsKeyVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("this.map.containsKey(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "bb", "Owner 1", "Owner 2", "Owner 6");
	}

	@Test
	public void queryContainsValueParameter1()
	{
		ElementA elementA = getExampleElement();

		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 3");
	}

	@Test
	public void queryContainsValueParameter2()
	{
		ElementA elementA = getExampleElement("Element 1.1");

		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 1", "Owner 3");
	}

	@Test
	public void queryContainsValueVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("this.map.containsValue(variable) && variable.name.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 4");
	}

	@Test
	public void queryNotContainsKeyParameter()
	{
		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, "ccc", "Owner 2", "Owner 3", "Owner 5", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsKeyVariableAndNotIndexOfMatches()}
	 */
	@Test
	public void queryContainsKeyVariableAndIndexOfNotMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("this.map.containsKey(variable) && variable.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, "bb", "Owner 1", "Owner 2", "Owner 3", "Owner 4");
	}

	/**
	 * Should behave exactly like {@link #queryContainsKeyVariableAndIndexOfNotMatches()}
	 */
	@Test
	public void queryContainsKeyVariableAndNotIndexOfMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("this.map.containsKey(variable) && !(variable.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, "bb", "Owner 1", "Owner 2", "Owner 3", "Owner 4");
	}

	@Test
	public void queryNotContainsValueParameter1()
	{
		ElementA elementA = getExampleElement();

		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 1", "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	@Test
	public void queryNotContainsValueParameter2()
	{
		ElementA elementA = getExampleElement("Element 1.1");

		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsValueVariableAndIndexOfNotMatches()}
	 */
	@Test
	public void queryContainsValueVariableAndNotIndexOfMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("this.map.containsValue(variable) && !(variable.name.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsValueVariableAndNotIndexOfMatches()}
	 */
	@Test
	public void queryContainsValueVariableAndIndexOfNotMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner1.class);
		q.setFilter("this.map.containsValue(variable) && variable.name.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3", "Owner 6");
	}
}
