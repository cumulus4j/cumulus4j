package org.cumulus4j.test.collection.mappedby;

import org.cumulus4j.test.framework.AbstractTransactionalTest;
import org.cumulus4j.test.framework.CleanupUtil;
import org.junit.Before;
import org.junit.BeforeClass;
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
			owner.addElement2(new Element2("aaa", "Element2 2.1"));
			owner.addElement2(new Element2("bbb", "Element2 2.2"));
			owner.addElement2(new Element2("ccc", "Element2 2.3"));
			owner.addElement2(new Element2("ddd", "Element2 2.4"));
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 3");
			owner.addElement2(new Element2("aaa", "Element2 3.1"));
			owner.addElement2(new Element2("bbb", "Element2 3.2"));
			owner.addElement2(new Element2("ccc", "Element2 3.3"));
		}

		{
			Element2MapOwner owner = pm.makePersistent(new Element2MapOwner());
			owner.setName("Owner 4");
			owner.addElement2(new Element2("ccc", "Element2 4.3"));
		}
	}

// TODO uncomment all the following and continue implementing.
//	private static enum KeyValue {
//		key, value
//	}
//
//	private void executeQueryAndCheckResult(Query q, Object queryParamO, String queryParamS, KeyValue keyValue, boolean indexOf, int expectedResultListSize)
//	{
//		String testMethodName = new Exception().getStackTrace()[1].getMethodName();
//
//		@SuppressWarnings("unchecked")
//		List<Element2MapOwner> resultList = (List<Element2MapOwner>) q.execute(queryParam);
//		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);
//
//		String logMsgPart = indexOf ? "containing at least one " + keyValue + " which contains the part " : "containing the " + keyValue;
//		logger.info(testMethodName + ": found " + resultList.size() + " StringStringMapOwners " + logMsgPart + " \"" + queryParam + "\":");
//		Assert.assertEquals("Query returned wrong number of results!", expectedResultListSize, resultList.size());
//		for (StringStringMapOwner resultElement : resultList) {
//			Assert.assertNotNull("Query returned a StringStringMapOwner with the map property being null!", resultElement.getMap());
//
//			boolean resultElementMatches = false;
//			StringBuilder sb = new StringBuilder();
//			for (Map.Entry<String, String> mapEntry : resultElement.getMap().entrySet()) {
//				Assert.assertNotNull("Query returned a StringStringMapOwner whose map contains a null key!", mapEntry.getKey());
//				Assert.assertNotNull("Query returned a StringStringMapOwner whose map contains a null value!", mapEntry.getValue());
//
//				String found = null;
//				switch (keyValue) {
//					case key:
//						found = mapEntry.getKey();
//						break;
//					case value:
//						found = mapEntry.getValue();
//						break;
//				}
//				if (indexOf) {
//					if (found.indexOf(queryParam) >= 0)
//						resultElementMatches = true;
//				}
//				else {
//					if (found.equals(queryParam))
//						resultElementMatches = true;
//				}
//
//
//				if (sb.length() > 0)
//					sb.append(", ");
//
//				sb.append(mapEntry.getKey()).append('=').append(mapEntry.getValue());
//			}
//
//			logger.info(testMethodName + ":   * " + resultElement.getId() + ": " + sb);
//			Assert.assertTrue(
//					"Query returned a StringStringMapOwner with the map property not containing the searched " + keyValue + ": " + resultElement.getId(),
//					resultElementMatches
//			);
//		}
//	}
//
//	private Element2 getExampleElement()
//	{
//		Query q = pm.newQuery(Element2.class);
//		q.setFilter("this.name == :name");
//		q.setUnique(true);
//		Element2 element = (Element2) q.execute("Element2 3.2");
//		if (element == null)
//			throw new IllegalStateException("No matching Element2 found!");
//
//		return element;
//	}
//
//	@Test
//	public void queryContainsKeyParameter()
//	{
//		Query q = pm.newQuery(StringStringMapOwner.class);
//		q.setFilter("this.map.containsKey(:queryParam)");
//		executeQueryAndCheckResult(q, "second", KeyValue.key, false, 3);
//	}

}
