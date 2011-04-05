package org.cumulus4j.test.collection.mappedby;

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

public class Element1SetQueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(Element1SetQueryTest.class);

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
		if (pm.getExtent(Element1SetOwner.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		{
			Element1SetOwner owner = new Element1SetOwner();
			owner.setName("Owner 1");
			owner.addElement1(new Element1("Element1 1.1"));
			owner.addElement1(new Element1("Element1 1.2"));
			owner.addElement1(new Element1("Element1 1.3"));
			owner.addElement1(new Element1("Element1 1.4"));
			pm.makePersistent(owner);
		}

		{
			Element1SetOwner owner = pm.makePersistent(new Element1SetOwner());
			owner.setName("Owner 2");
			owner.addElement1(new Element1("Element1 2.1"));
			owner.addElement1(new Element1("Element1 2.2"));
			owner.addElement1(new Element1("Element1 2.3"));
			owner.addElement1(new Element1("Element1 2.4"));
		}

		{
			Element1SetOwner owner = pm.makePersistent(new Element1SetOwner());
			owner.setName("Owner 3");
			owner.addElement1(new Element1("Element1 3.1"));
			owner.addElement1(new Element1("Element1 3.2"));
			owner.addElement1(new Element1("Element1 3.3"));
		}

		{
			Element1SetOwner owner = pm.makePersistent(new Element1SetOwner());
			owner.setName("Owner 4");
			owner.addElement1(new Element1("Element1 4.3"));
		}

		{
			Element1SetOwner owner = pm.makePersistent(new Element1SetOwner());
			owner.setName("Owner 5");
		}
	}

	private void executeQueryAndCheckResult(Query q, Object queryParamO, String queryParamS, boolean indexOf, int expectedResultListSize, boolean negated)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		if (queryParamS != null)
			queryParamO = queryParamS;
		else {
			if (queryParamO == null)
				throw new IllegalArgumentException("Only one of queryParamO and queryParamS can be null! But both are null!");

			if (indexOf)
				throw new IllegalArgumentException("There is no String-queryParam, but indexOf is true! indexOf must be false with object-QueryParam!");
		}

		@SuppressWarnings("unchecked")
		List<Element1SetOwner> resultList = (List<Element1SetOwner>) q.execute(queryParamO);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String logMsgPart = indexOf ? "containing at least one element which contains the part" : "containing the element";
		logger.info(testMethodName + ": found " + resultList.size() + " Element1SetOwners " + logMsgPart + " \"" + queryParamO + "\":");
		Assert.assertEquals("Query returned wrong number of results!", expectedResultListSize, resultList.size());
		for (Element1SetOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a Element1SetOwner with the set property being null!", resultElement.getSet());

			boolean resultElementMatches = false;
			StringBuilder sb = new StringBuilder();
			for (Element1 setElement : resultElement.getSet()) {
				Assert.assertNotNull("Query returned a Element1SetOwner whose set contains a null entry!", setElement);
				Assert.assertNotNull("Query returned a Element1SetOwner whose set contains an element with a null name!", setElement.getName());

				if (queryParamS != null) {
					if (indexOf) {
						if (negated) {
							if (setElement.getName().indexOf(queryParamS) < 0)
								resultElementMatches = true;
						}
						else {
							if (setElement.getName().indexOf(queryParamS) >= 0)
								resultElementMatches = true;
						}
					}
					else {
						if (negated) {
							if (!setElement.getName().equals(queryParamS))
								resultElementMatches = true;
						}
						else {
							if (setElement.getName().equals(queryParamS))
								resultElementMatches = true;
						}
					}
				}
				else {
					if (negated) {
						if (!queryParamO.equals(setElement))
							resultElementMatches = true;
					}
					else {
						if (queryParamO.equals(setElement))
							resultElementMatches = true;
					}
				}

				if (sb.length() > 0)
					sb.append(", ");

				sb.append(setElement.getName());
			}
			if (resultElement.getSet().isEmpty() && negated)
				resultElementMatches = true;

			logger.info(testMethodName + ":   * " + resultElement.getName() + ": " + sb);
			Assert.assertTrue(
					"Query returned a Element1SetOwner with the set property not containing the searched element: " + resultElement.getName(),
					resultElementMatches
			);
		}
	}

	private Element1 getExampleElement()
	{
		return getExampleElement("Element1 3.2");
	}
	private Element1 getExampleElement(String name)
	{
		Query q = pm.newQuery(Element1.class);
		q.setFilter("this.name == :name");
		q.setUnique(true);
		Element1 element = (Element1) q.execute(name);
		if (element == null)
			throw new IllegalStateException("No matching Element1 found!");

		return element;
	}

	@Test
	public void queryContainsParameter()
	{
		Element1 element = getExampleElement();
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(:element)");
		executeQueryAndCheckResult(q, element, null, false, 1, false);
	}

	@Test
	public void queryContainsVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable.name.indexOf(:elementPart) >= 0");
		// Implicit variables are now properly supported => don't need to declare them anymore.
//		q.declareVariables(Element1.class.getName() + " elementVariable");
		executeQueryAndCheckResult(q, null, "4", true, 3, false);
	}

	@Test
	public void queryContainsVariableAndVariableEquals()
	{
		Element1 element = getExampleElement();
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable == :element");
		// Implicit variables are now properly supported => don't need to declare them anymore.
//		q.declareVariables(Element1.class.getName() + " elementVariable");
		executeQueryAndCheckResult(q, element, null, false, 1, false);
	}

	@Test
	public void queryNotContainsParameter()
	{
		Element1 element = getExampleElement();
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("!this.set.contains(:element)");
		executeQueryAndCheckResult(q, element, null, false, 4, true);
	}

	/**
	 * Should behave exactly like #queryContainsVariableAndNotVariableIndexOf()
	 */
	@Test
	public void queryContainsVariableAndVariableNotIndexOf()
	{
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable.name.indexOf(:elementPart) < 0");
		executeQueryAndCheckResult(q, null, "4", true, 3, true);
	}

	/**
	 * Should behave exactly like #queryContainsVariableAndVariableNotIndexOf()
	 */
	@Test
	public void queryContainsVariableAndNotVariableIndexOf()
	{
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable.name.indexOf(:elementPart) >= 0)");
		executeQueryAndCheckResult(q, null, "4", true, 3, true);
	}

	@Test
	public void queryContainsVariableAndNotVariableEquals1()
	{
		Element1 element = getExampleElement();
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable == :element)");
		executeQueryAndCheckResult(q, element, null, false, 4, true);
	}

	@Test
	public void queryContainsVariableAndNotVariableEquals2()
	{
		Element1 element = getExampleElement("Element1 4.3");
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable == :element)");
		executeQueryAndCheckResult(q, element, null, false, 3, true);
	}
}
