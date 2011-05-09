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

public class ElementABMapQueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(ElementABMapQueryTest.class);

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
		if (pm.getExtent(ElementABMapOwner.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		ElementA elementA_K1_3 = new ElementA("Element K1.3");
		ElementB elementB_V1_1 = new ElementB("Element V1.1");

		{
			ElementABMapOwner owner = new ElementABMapOwner();
			owner.setName("Owner 1");
			owner.putMapEntry(new ElementA("Element K1.1"), elementB_V1_1);
			owner.putMapEntry(new ElementA("Element K1.2"), new ElementB("Element V1.2"));
			owner.putMapEntry(elementA_K1_3, new ElementB("Element V1.3"));
			owner.putMapEntry(new ElementA("Element K1.4"), new ElementB("Element V1.4"));
			pm.makePersistent(owner);
		}

		{
			ElementABMapOwner owner = pm.makePersistent(new ElementABMapOwner());
			owner.setName("Owner 2");
			owner.putMapEntry(new ElementA("Element K2.1"), new ElementB("Element V2.1"));
			owner.putMapEntry(new ElementA("Element K2.2"), new ElementB("Element V2.2"));
			owner.putMapEntry(elementA_K1_3, new ElementB("Element V2.3"));
			owner.putMapEntry(new ElementA("Element K2.4"), new ElementB("Element V2.4"));
		}

		{
			ElementABMapOwner owner = pm.makePersistent(new ElementABMapOwner());
			owner.setName("Owner 3");
			owner.putMapEntry(new ElementA("Element K3.1"), elementB_V1_1);
			owner.putMapEntry(new ElementA("Element K3.2"), new ElementB("Element V3.2"));
			owner.putMapEntry(new ElementA("Element K3.3"), new ElementB("Element V3.3"));
		}

		{
			ElementABMapOwner owner = pm.makePersistent(new ElementABMapOwner());
			owner.setName("Owner 4");
			owner.putMapEntry(elementA_K1_3, new ElementB("Element V4.3"));
		}

		{
			ElementABMapOwner owner = pm.makePersistent(new ElementABMapOwner());
			owner.setName("Owner 5");
		}

		{
			ElementABMapOwner owner = pm.makePersistent(new ElementABMapOwner());
			owner.setName("Owner 6");
			owner.putMapEntry(new ElementA("Element K6.2"), new ElementB("Element V6.2"));
		}
	}

	private void executeQueryAndCheckResult(Query q, Object queryParam, String ... expectedOwnerNames)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<ElementABMapOwner> resultList = (List<ElementABMapOwner>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceFirst("^.* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " ElementABMapOwners for query-filter \"" + f + "\" and param \"" + queryParam + "\":");

		Set<String> expectedOwnerNameSet = new HashSet<String>(Arrays.asList(expectedOwnerNames));

		for (ElementABMapOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a ElementABMapOwner with the 'name' property being null: " + resultElement, resultElement.getName());
			Assert.assertNotNull("Query returned a ElementABMapOwner with the 'map' property being null!", resultElement.getMap());

			boolean expectedElement = expectedOwnerNameSet.remove(resultElement.getName());

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<ElementA, ElementB> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a ElementABMapOwner whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a ElementABMapOwner whose map contains a null value!", mapEntry.getValue());

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
		return getExampleElementA("Element K3.2");
	}

	private ElementB getExampleElementV()
	{
		return getExampleElementB("Element V3.2");
	}

	private ElementA getExampleElementA(String name)
	{
		Query q = pm.newQuery(ElementA.class);
		q.setFilter("this.name == :name");
		q.setUnique(true);
		ElementA element = (ElementA) q.execute(name);
		if (element == null)
			throw new IllegalStateException("No matching element found!");

		return element;
	}

	private ElementB getExampleElementB(String name)
	{
		Query q = pm.newQuery(ElementB.class);
		q.setFilter("this.name == :name");
		q.setUnique(true);
		ElementB element = (ElementB) q.execute(name);
		if (element == null)
			throw new IllegalStateException("No matching element found!");

		return element;
	}

	@Test
	public void queryContainsKeyParameter1()
	{
		ElementA elementA = getExampleElementK();

		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 3");
	}

	@Test
	public void queryContainsKeyParameter2()
	{
		ElementA elementA = getExampleElementA("Element K1.3");

		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 1", "Owner 2", "Owner 4");
	}

	@Test
	public void queryNotContainsKeyParameter1()
	{
		ElementA elementA = getExampleElementK();

		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 1", "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	@Test
	public void queryNotContainsKeyParameter2()
	{
		ElementA elementA = getExampleElementA("Element K1.3");

		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 3", "Owner 5", "Owner 6");
	}

	@Test
	public void queryContainsKeyVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.name.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "3", "Owner 1", "Owner 2", "Owner 3", "Owner 4");
	}

	@Test
	public void queryContainsValueParameter1()
	{
		ElementB elementB = getExampleElementV();

		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementB, "Owner 3");
	}

	@Test
	public void queryContainsValueParameter2()
	{
		ElementB elementB = getExampleElementB("Element V1.1");

		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementB, "Owner 1", "Owner 3");
	}

	@Test
	public void queryContainsValueVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.name.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 4");
	}

	/**
	 * Should behave exactly like {@link #queryContainsKeyVariableAndNotIndexOfMatches()}
	 */
	@Test
	public void queryContainsKeyVariableAndIndexOfNotMatches()
	{
		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.name.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, "3", "Owner 1", "Owner 2", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsKeyVariableAndIndexOfNotMatches()}
	 */
	@Test
	public void queryContainsKeyVariableAndNotIndexOfMatches()
	{
		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && !(variable.name.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, "3", "Owner 1", "Owner 2", "Owner 6");
	}

	@Test
	public void queryNotContainsValueParameter1()
	{
		ElementB elementB = getExampleElementV();

		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementB, "Owner 1", "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	@Test
	public void queryNotContainsValueParameter2()
	{
		ElementB elementB = getExampleElementB("Element V1.1");

		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, elementB, "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsValueVariableAndIndexOfNotMatches()}
	 */
	@Test
	public void queryContainsValueVariableAndNotIndexOfMatches()
	{
		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && !(variable.name.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsValueVariableAndNotIndexOfMatches()}
	 */
	@Test
	public void queryContainsValueVariableAndIndexOfNotMatches()
	{
		Query q = pm.newQuery(ElementABMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.name.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3", "Owner 6");
	}
}
