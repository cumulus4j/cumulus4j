package org.cumulus4j.test.collection.mappedby;

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
			owner.addElement2(new Element2("aaa", "Element2 1.1"));
			owner.addElement2(new Element2("bbb", "Element2 1.2"));
			owner.addElement2(new Element2("ccc", "Element2 1.3"));
			owner.addElement2(new Element2("ddd", "Element2 1.4"));
			pm.makePersistent(owner);
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 2");
			owner.addElement2(new Element2("aa", "Element2 2.1"));
			owner.addElement2(new Element2("bb", "Element2 2.2"));
			owner.addElement2(new Element2("cc", "Element2 2.3"));
			owner.addElement2(new Element2("dd", "Element2 2.4"));
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 3");
			owner.addElement2(new Element2("a", "Element2 3.1"));
			owner.addElement2(new Element2("b", "Element2 3.2"));
			owner.addElement2(new Element2("c", "Element2 3.3"));
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 4");
			owner.addElement2(new Element2("ccc", "Element2 4.3"));
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 5");
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 6");
			owner.addElement2(new Element2("bb", "Element2 6.2"));
		}
	}

	private static enum KeyValue {
		key, value
	}

	private void executeQueryAndCheckResult(Query q, Object queryParamO, String queryParamS, KeyValue keyValue, boolean indexOf, int expectedResultListSize, boolean negated)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		if (queryParamS != null)
			queryParamO = queryParamS;

		@SuppressWarnings("unchecked")
		List<Element2MapOwner> resultList = (List<Element2MapOwner>) q.execute(queryParamO);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String logMsgPart = indexOf ? "containing at least one " + keyValue + " which contains the part (or whose name property contains) " : "containing the " + keyValue;
		logger.info(testMethodName + ": found " + resultList.size() + " Element2MapOwners " + logMsgPart + " \"" + queryParamO + "\":");
		Assert.assertEquals("Query returned wrong number of results!", expectedResultListSize, resultList.size());
		for (Element2MapOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a Element2MapOwner with the map property being null!", resultElement.getMap());

			boolean resultElementMatches = false;
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, Element2> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a Element2MapOwner whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a Element2MapOwner whose map contains a null value!", mapEntry.getValue());

				Object found = null;
				switch (keyValue) {
					case key:
						found = mapEntry.getKey();
						break;
					case value:
						found = mapEntry.getValue();
						break;
				}

				if (indexOf) {
					if (found instanceof Element2) {
						Element2 e2 = (Element2) found;
						if (negated) {
							if (e2.getName().indexOf(queryParamS) < 0)
								resultElementMatches = true;
						}
						else {
							if (e2.getName().indexOf(queryParamS) >= 0)
								resultElementMatches = true;
						}
					}
					else if (found instanceof String) {
						String s = (String) found;
						if (negated) {
							if (s.indexOf(queryParamS) < 0)
								resultElementMatches = true;
						}
						else {
							if (s.indexOf(queryParamS) >= 0)
								resultElementMatches = true;
						}
					}
				}
				else {
					if (negated) {
						if (!found.equals(queryParamO))
							resultElementMatches = true;
					}
					else {
						if (found.equals(queryParamO))
							resultElementMatches = true;
					}
				}

				if (sb.length() > 0)
					sb.append(", ");

				sb.append(mapEntry.getKey()).append('=').append(mapEntry.getValue());
			}
			if (resultElement.getMap().isEmpty() && negated)
				resultElementMatches = true;

			logger.info(testMethodName + ":   * " + resultElement + ": " + sb);
			Assert.assertTrue(
					"Query returned a Element2MapOwner with the map property not containing the searched " + keyValue + ": " + resultElement.getName(),
					resultElementMatches
			);
		}
	}

	private Element2 getExampleElement()
	{
		Query q = pm.newQuery(Element2.class);
		q.setFilter("this.name == :name");
		q.setUnique(true);
		Element2 element = (Element2) q.execute("Element2 3.2");
		if (element == null)
			throw new IllegalStateException("No matching Element2 found!");

		return element;
	}

	@Test
	public void queryContainsKeyParameter()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, null, "ccc", KeyValue.key, false, 2, false);
	}

	@Test
	public void queryContainsKeyVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, null, "bb", KeyValue.key, true, 3, false);
	}

	@Test
	public void queryContainsValueParameter()
	{
		Element2 element2 = getExampleElement();

		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, element2, null, KeyValue.value, false, 1, false);
	}

	@Test
	public void queryContainsValueVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.name.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, null, "4", KeyValue.value, true, 3, false);
	}

	@Test
	public void queryNotContainsKeyParameter()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, null, "ccc", KeyValue.key, false, 4, true);
	}

	/**
	 * Should behave exactly like {@link #queryContainsKeyVariableAndNotIndexOfMatches()}
	 */
	@Test
	public void queryContainsKeyVariableAndIndexOfNotMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, null, "bb", KeyValue.key, true, 4, true);
	}

	/**
	 * Should behave exactly like {@link #queryContainsKeyVariableAndIndexOfNotMatches()}
	 */
	@Test
	public void queryContainsKeyVariableAndNotIndexOfMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsKey(variable) && !(variable.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, null, "bb", KeyValue.key, true, 4, true);
	}

	@Test
	public void queryNotContainsValueParameter()
	{
		Element2 element2 = getExampleElement();

		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, element2, null, KeyValue.value, false, 5, true);
	}

	/**
	 * Should behave exactly like {@link #queryContainsValueVariableAndIndexOfNotMatches()}
	 */
	@Test
	public void queryContainsValueVariableAndNotIndexOfMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsValue(variable) && !(variable.name.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, null, "4", KeyValue.value, true, 4, true);
	}

	/**
	 * Should behave exactly like {@link #queryContainsValueVariableAndNotIndexOfMatches()}
	 */
	@Test
	public void queryContainsValueVariableAndIndexOfNotMatches()
	{
		Query q = pm.newQuery(Element2MapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.name.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, null, "4", KeyValue.value, true, 4, true);
	}
}
