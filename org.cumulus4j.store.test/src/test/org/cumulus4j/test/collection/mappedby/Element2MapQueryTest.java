package org.cumulus4j.test.collection.mappedby;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.Query;

import org.cumulus4j.store.test.collection.mappedby.Element2;
import org.cumulus4j.store.test.collection.mappedby.Element2MapOwner;
import org.cumulus4j.store.test.framework.AbstractTransactionalTest;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Element2MapQueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(Element2MapQueryTest.class);

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
		if (pm.getExtent(Element2MapOwner.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		{
			Element2MapOwner owner = new Element2MapOwner();
			owner.setName("Owner 1");
			owner.addElement2(new Element2("aaa", "Element 1.1"));
			owner.addElement2(new Element2("bbb", "Element 1.2"));
			owner.addElement2(new Element2("ccc", "Element 1.3"));
			owner.addElement2(new Element2("ddd", "Element 1.4"));
			pm.makePersistent(owner);
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 2");
			owner.addElement2(new Element2("aa", "Element 2.1"));
			owner.addElement2(new Element2("bb", "Element 2.2"));
			owner.addElement2(new Element2("cc", "Element 2.3"));
			owner.addElement2(new Element2("dd", "Element 2.4"));
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 3");
			owner.addElement2(new Element2("a", "Element 3.1"));
			owner.addElement2(new Element2("b", "Element 3.2"));
			owner.addElement2(new Element2("c", "Element 3.3"));
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 4");
			owner.addElement2(new Element2("ccc", "Element 4.3"));
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 5");
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 6");
			owner.addElement2(new Element2("bb", "Element 6.2"));
		}
	}

	private void executeQueryAndCheckResult(Query q, Object queryParam, String ... expectedOwnerNames)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<Element2MapOwner> resultList = (List<Element2MapOwner>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceFirst("^.* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " Element2MapOwners for query-filter \"" + f + "\" and param \"" + queryParam + "\":");

		Set<String> expectedOwnerNameSet = new HashSet<String>(Arrays.asList(expectedOwnerNames));

		for (Element2MapOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a Element2MapOwner with the 'name' property being null: " + resultElement, resultElement.getName());
			Assert.assertNotNull("Query returned a Element2MapOwner with the 'map' property being null!", resultElement.getMap());

			boolean expectedElement = expectedOwnerNameSet.remove(resultElement.getName());

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, Element2> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a Element2MapOwner whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a Element2MapOwner whose map contains a null value!", mapEntry.getValue());

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

	private Element2 getExampleElement()
	{
		Query q = pm.newQuery(Element2.class);
		q.setFilter("this.name == :name");
		q.setUnique(true);
		Element2 element = (Element2) q.execute("Element 3.2");
		if (element == null)
			throw new IllegalStateException("No matching element found!");

		return element;
	}

	@Test
	public void queryContainsKeyParameter()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, "ccc", "Owner 1", "Owner 4");
	}

	@Test
	public void queryContainsKeyVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "bb", "Owner 1", "Owner 2", "Owner 6");
	}

	@Test
	public void queryContainsValueParameter()
	{
		Element2 element2 = getExampleElement();

		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, element2, "Owner 3");
	}

	@Test
	public void queryContainsValueVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.name.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 4");
	}

	@Test
	public void queryNotContainsKeyParameter()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, "ccc", "Owner 2", "Owner 3", "Owner 5", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsKeyVariableAndNotIndexOfMatches()}
	 */
	@Test
	public void queryContainsKeyVariableAndIndexOfNotMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, "bb", "Owner 1", "Owner 2", "Owner 3", "Owner 4");
	}

	/**
	 * Should behave exactly like {@link #queryContainsKeyVariableAndIndexOfNotMatches()}
	 */
	@Test
	public void queryContainsKeyVariableAndNotIndexOfMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsKey(variable) && !(variable.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, "bb", "Owner 1", "Owner 2", "Owner 3", "Owner 4");
	}

	@Test
	public void queryNotContainsValueParameter()
	{
		Element2 element2 = getExampleElement();

		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, element2, "Owner 1", "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsValueVariableAndIndexOfNotMatches()}
	 */
	@Test
	public void queryContainsValueVariableAndNotIndexOfMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsValue(variable) && !(variable.name.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3", "Owner 6");
	}

	/**
	 * Should behave exactly like {@link #queryContainsValueVariableAndNotIndexOfMatches()}
	 */
	@Test
	public void queryContainsValueVariableAndIndexOfNotMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.name.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3", "Owner 6");
	}
}
