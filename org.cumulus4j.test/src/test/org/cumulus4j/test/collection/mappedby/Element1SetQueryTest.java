package org.cumulus4j.test.collection.mappedby;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
			owner.addElement1(new Element1("Element 1.1"));
			owner.addElement1(new Element1("Element 1.2"));
			owner.addElement1(new Element1("Element 1.3"));
			owner.addElement1(new Element1("Element 1.4"));
			pm.makePersistent(owner);
		}

		{
			Element1SetOwner owner = pm.makePersistent(new Element1SetOwner());
			owner.setName("Owner 2");
			owner.addElement1(new Element1("Element 2.1"));
			owner.addElement1(new Element1("Element 2.2"));
			owner.addElement1(new Element1("Element 2.3"));
			owner.addElement1(new Element1("Element 2.4"));
		}

		{
			Element1SetOwner owner = pm.makePersistent(new Element1SetOwner());
			owner.setName("Owner 3");
			owner.addElement1(new Element1("Element 3.1"));
			owner.addElement1(new Element1("Element 3.2"));
			owner.addElement1(new Element1("Element 3.3"));
		}

		{
			Element1SetOwner owner = pm.makePersistent(new Element1SetOwner());
			owner.setName("Owner 4");
			owner.addElement1(new Element1("Element 4.3"));
		}

		{
			Element1SetOwner owner = pm.makePersistent(new Element1SetOwner());
			owner.setName("Owner 5");
		}
	}

	private void executeQueryAndCheckResult(Query q, Object queryParam, String ... expectedOwnerNames)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<Element1SetOwner> resultList = (List<Element1SetOwner>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceFirst("^.* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " Element1SetOwners for query-filter \"" + f + "\" and param \"" + queryParam + "\":");

		Set<String> expectedOwnerNameSet = new HashSet<String>(Arrays.asList(expectedOwnerNames));

		for (Element1SetOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a Element1SetOwner with the 'name' property being null: " + resultElement, resultElement.getName());
			Assert.assertNotNull("Query returned a Element1SetOwner with the 'set' property being null: " + resultElement, resultElement.getSet());

			boolean expectedElement = expectedOwnerNameSet.remove(resultElement.getName());

			StringBuilder sb = new StringBuilder();
			for (Element1 setElement : resultElement.getSet()) {
				Assert.assertNotNull("Query returned a Element1SetOwner whose set contains a null entry!", setElement);
				Assert.assertNotNull("Query returned a Element1SetOwner whose set contains an element with a null name!", setElement.getName());

				if (sb.length() > 0)
					sb.append(", ");

				sb.append(setElement.getName());
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
		executeQueryAndCheckResult(q, element, "Owner 3");
	}

	@Test
	public void queryContainsVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable.name.indexOf(:elementPart) >= 0");
		// Implicit variables are now properly supported => don't need to declare them anymore.
//		q.declareVariables(Element1.class.getName() + " elementVariable");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 4");
	}

	@Test
	public void queryContainsVariableAndVariableEquals()
	{
		Element1 element = getExampleElement();
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable == :element");
		// Implicit variables are now properly supported => don't need to declare them anymore.
//		q.declareVariables(Element1.class.getName() + " elementVariable");
		executeQueryAndCheckResult(q, element, "Owner 3");
	}

	@Test
	public void queryNotContainsParameter()
	{
		Element1 element = getExampleElement();
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("!this.set.contains(:element)");
		executeQueryAndCheckResult(q, element, "Owner 1", "Owner 2", "Owner 4", "Owner 5");
	}

	/**
	 * Should behave exactly like {@link #queryContainsVariableAndNotVariableIndexOf()}
	 */
	@Test
	public void queryContainsVariableAndVariableNotIndexOf()
	{
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable.name.indexOf(:elementPart) < 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3");
	}

	/**
	 * Should behave exactly like {@link #queryContainsVariableAndVariableNotIndexOf()}
	 */
	@Test
	public void queryContainsVariableAndNotVariableIndexOf()
	{
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable.name.indexOf(:elementPart) >= 0)");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3");
	}

	@Test
	public void queryContainsVariableAndNotVariableEquals1()
	{
		Element1 element = getExampleElement();
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable == :element)");
		executeQueryAndCheckResult(q, element, "Owner 1", "Owner 2", "Owner 3", "Owner 4");
	}

	@Test
	public void queryContainsVariableAndNotVariableEquals2()
	{
		Element1 element = getExampleElement("Element1 4.3");
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable == :element)");
		executeQueryAndCheckResult(q, element, "Owner 1", "Owner 2", "Owner 3");
	}

	@Test
	public void queryNotContainsVariableAndVariableEquals()
	{
		Element1 element = getExampleElement();
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("!this.set.contains(elementVariable) && elementVariable == :element");
		executeQueryAndCheckResult(q, element, "Owner 1", "Owner 2", "Owner 4", "Owner 5");
	}

	@Test
	public void queryNotContainsVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(Element1SetOwner.class);
		q.setFilter("!this.set.contains(elementVariable) && elementVariable.name.indexOf(:elementPart) >= 0");
		executeQueryAndCheckResult(q, "4", "Owner 3", "Owner 5");
	}
}
