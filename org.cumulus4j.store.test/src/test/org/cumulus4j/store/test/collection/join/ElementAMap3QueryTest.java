package org.cumulus4j.store.test.collection.join;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.Query;

import org.cumulus4j.store.test.collection.join.ElementA;
import org.cumulus4j.store.test.collection.join.ElementAMapOwner3;
import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTest;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElementAMap3QueryTest
extends AbstractJDOTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(ElementAMap3QueryTest.class);

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
		if (pm.getExtent(ElementAMapOwner3.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		ElementA elementA_K1_3 = new ElementA("Element K1.3");
		ElementA elementA_V1_1 = new ElementA("Element V1.1");

		{
			ElementAMapOwner3 owner = new ElementAMapOwner3();
			owner.setName("Owner 1");
			owner.putMapEntry(new ElementA("Element K1.1"), elementA_V1_1);
			owner.putMapEntry(new ElementA("Element K1.2"), new ElementA("Element V1.2"));
			owner.putMapEntry(elementA_K1_3, new ElementA("Element V1.3"));
			owner.putMapEntry(new ElementA("Element K1.4"), new ElementA("Element V1.4"));
			pm.makePersistent(owner);
		}

		{
			ElementAMapOwner3 owner = pm.makePersistent(new ElementAMapOwner3());
			owner.setName("Owner 2");
			owner.putMapEntry(new ElementA("Element K2.1"), new ElementA("Element V2.1"));
			owner.putMapEntry(new ElementA("Element K2.2"), new ElementA("Element V2.2"));
			owner.putMapEntry(elementA_K1_3, new ElementA("Element V2.3"));
			owner.putMapEntry(new ElementA("Element K2.4"), new ElementA("Element V2.4"));
		}

		{
			ElementAMapOwner3 owner = pm.makePersistent(new ElementAMapOwner3());
			owner.setName("Owner 3");
			owner.putMapEntry(new ElementA("Element K3.1"), elementA_V1_1);
			owner.putMapEntry(new ElementA("Element K3.2"), new ElementA("Element V3.2"));
			owner.putMapEntry(new ElementA("Element K3.3"), new ElementA("Element V3.3"));
		}

		{
			ElementAMapOwner3 owner = pm.makePersistent(new ElementAMapOwner3());
			owner.setName("Owner 4");
			owner.putMapEntry(elementA_K1_3, new ElementA("Element V4.3"));
		}

		{
			ElementAMapOwner3 owner = pm.makePersistent(new ElementAMapOwner3());
			owner.setName("Owner 5");
		}

		{
			ElementAMapOwner3 owner = pm.makePersistent(new ElementAMapOwner3());
			owner.setName("Owner 6");
			owner.putMapEntry(new ElementA("Element K6.2"), new ElementA("Element V6.2"));
		}
	}

	private void executeQueryAndCheckResult(Query q, Object queryParam, String ... expectedOwnerNames)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<ElementAMapOwner3> resultList = (List<ElementAMapOwner3>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceFirst("^.* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " ElementAMapOwner3s for query-filter \"" + f + "\" and param \"" + queryParam + "\":");

		Set<String> expectedOwnerNameSet = new HashSet<String>(Arrays.asList(expectedOwnerNames));

		for (ElementAMapOwner3 resultElement : resultList) {
			Assert.assertNotNull("Query returned a ElementAMapOwner3 with the 'name' property being null: " + resultElement, resultElement.getName());
			Assert.assertNotNull("Query returned a ElementAMapOwner3 with the 'map' property being null!", resultElement.getMap());

			boolean expectedElement = expectedOwnerNameSet.remove(resultElement.getName());

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<ElementA, ElementA> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a ElementAMapOwner3 whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a ElementAMapOwner3 whose map contains a null value!", mapEntry.getValue());

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

	private ElementA getExampleElementK()
	{
		return getExampleElement("Element K3.2");
	}

	private ElementA getExampleElementV()
	{
		return getExampleElement("Element V3.2");
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
	public void queryContainsKeyParameter1()
	{
		ElementA elementA = getExampleElementK();

		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 3");
	}

	@Test
	public void queryContainsKeyParameter2()
	{
		ElementA elementA = getExampleElement("Element K1.3");

		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 1", "Owner 2", "Owner 4");
	}

	@Test
	public void queryNotContainsKeyParameter1()
	{
		ElementA elementA = getExampleElementK();

		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 1", "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	@Test
	public void queryNotContainsKeyParameter2()
	{
		ElementA elementA = getExampleElement("Element K1.3");

		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 3", "Owner 5", "Owner 6");
	}

	@Test
	public void queryContainsKeyVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("this.map.containsKey(variable) && variable.name.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "3", "Owner 1", "Owner 2", "Owner 3", "Owner 4");
	}

	@Test
	public void queryContainsValueParameter1()
	{
		ElementA elementA = getExampleElementV();

		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 3");
	}

	@Test
	public void queryContainsValueParameter2()
	{
		ElementA elementA = getExampleElement("Element V1.1");

		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 1", "Owner 3");
	}

	@Test
	public void queryContainsValueVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("this.map.containsValue(variable) && variable.name.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 4");
	}

	/**
	 * Should behave exactly like {@link #queryContainsKeyVariableAndNotIndexOfMatches()}
	 */
	@Test
	public void queryContainsKeyVariableAndIndexOfNotMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("this.map.containsKey(variable) && variable.name.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, "3", "Owner 1", "Owner 2", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsKeyVariableAndIndexOfNotMatches()}
	 */
	@Test
	public void queryContainsKeyVariableAndNotIndexOfMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("this.map.containsKey(variable) && !(variable.name.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, "3", "Owner 1", "Owner 2", "Owner 6");
	}

	@Test
	public void queryNotContainsValueParameter1()
	{
		ElementA elementA = getExampleElementV();

		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 1", "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	@Test
	public void queryNotContainsValueParameter2()
	{
		ElementA elementA = getExampleElement("Element V1.1");

		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsValueVariableAndIndexOfNotMatches()}
	 */
	@Test
	public void queryContainsValueVariableAndNotIndexOfMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("this.map.containsValue(variable) && !(variable.name.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsValueVariableAndNotIndexOfMatches()}
	 */
	@Test
	public void queryContainsValueVariableAndIndexOfNotMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner3.class);
		q.setFilter("this.map.containsValue(variable) && variable.name.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3", "Owner 6");
	}
}
