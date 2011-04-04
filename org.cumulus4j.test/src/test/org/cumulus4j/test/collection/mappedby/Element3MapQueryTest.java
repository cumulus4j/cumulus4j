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

public class Element3MapQueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(Element3MapQueryTest.class);

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
		if (pm.getExtent(Element3MapOwner.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		{
			Element3MapOwner owner = new Element3MapOwner();
			owner.setName("Owner 1");
			owner.addElement3(new Element3("aaa", "Element3 1.1"));
			owner.addElement3(new Element3("bbb", "Element3 1.2"));
			owner.addElement3(new Element3("ccc", "Element3 1.3"));
			owner.addElement3(new Element3("ddd", "Element3 1.4"));
			pm.makePersistent(owner);
		}

		{
			Element3MapOwner owner = pm.makePersistent(new Element3MapOwner());
			owner.setName("Owner 2");
			owner.addElement3(new Element3("aa", "Element3 2.1"));
			owner.addElement3(new Element3("bb", "Element3 2.2"));
			owner.addElement3(new Element3("cc", "Element3 2.3"));
			owner.addElement3(new Element3("dd", "Element3 2.4"));
		}

		{
			Element3MapOwner owner = pm.makePersistent(new Element3MapOwner());
			owner.setName("Owner 3");
			owner.addElement3(new Element3("a", "Element3 3.1"));
			owner.addElement3(new Element3("b", "Element3 3.2"));
			owner.addElement3(new Element3("c", "Element3 3.3"));
		}

		{
			Element3MapOwner owner = pm.makePersistent(new Element3MapOwner());
			owner.setName("Owner 4");
			owner.addElement3(new Element3("ccc", "Element3 4.3"));
		}
	}

	private static enum KeyValue {
		key, value
	}

	private void executeQueryAndCheckResult(Query q, Object queryParamO, String queryParamS, KeyValue keyValue, boolean indexOf, int expectedResultListSize)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		if (queryParamS != null)
			queryParamO = queryParamS;

		@SuppressWarnings("unchecked")
		List<Element3MapOwner> resultList = (List<Element3MapOwner>) q.execute(queryParamO);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String logMsgPart = indexOf ? "containing at least one " + keyValue + " which contains the part (or whose name property contains) " : "containing the " + keyValue;
		logger.info(testMethodName + ": found " + resultList.size() + " Element3MapOwners " + logMsgPart + " \"" + queryParamO + "\":");
		Assert.assertEquals("Query returned wrong number of results!", expectedResultListSize, resultList.size());
		for (Element3MapOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a Element3MapOwner with the map property being null!", resultElement.getMap());

			boolean resultElementMatches = false;
			StringBuilder sb = new StringBuilder();
			for (Map.Entry<Element3, String> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a Element3MapOwner whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a Element3MapOwner whose map contains a null value!", mapEntry.getValue());

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
					if (found instanceof Element3) {
						Element3 e2 = (Element3) found;
						if (e2.getName().indexOf(queryParamS) >= 0)
							resultElementMatches = true;
					}
					else if (found instanceof String) {
						String s = (String) found;
						if (s.indexOf(queryParamS) >= 0)
							resultElementMatches = true;
					}
				}
				else {
					if (found.equals(queryParamO))
						resultElementMatches = true;
				}


				if (sb.length() > 0)
					sb.append(", ");

				sb.append(mapEntry.getKey()).append('=').append(mapEntry.getValue());
			}

			logger.info(testMethodName + ":   * " + sb);
			Assert.assertTrue(
					"Query returned a Element3MapOwner with the map property not containing the searched " + keyValue + ": " + resultElement.getName(),
					resultElementMatches
			);
		}
	}

	private Element3 getExampleElement()
	{
		Query q = pm.newQuery(Element3.class);
		q.setFilter("this.name == :name");
		q.setUnique(true);
		Element3 element = (Element3) q.execute("Element3 3.2");
		if (element == null)
			throw new IllegalStateException("No matching Element3 found!");

		return element;
	}

	@Test
	public void queryContainsValueParameter()
	{
		Query q = pm.newQuery(Element3MapOwner.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, null, "ccc", KeyValue.value, false, 2);
	}

	@Test
	public void queryContainsValueVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(Element3MapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, null, "bb", KeyValue.value, true, 2);
	}

	@Test
	public void queryContainsKeyParameter()
	{
		Element3 element3 = getExampleElement();

		Query q = pm.newQuery(Element3MapOwner.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, element3, null, KeyValue.key, false, 1);
	}

	@Test
	public void queryContainsKeyVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(Element3MapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.name.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, null, "4", KeyValue.key, true, 3);
	}
}
