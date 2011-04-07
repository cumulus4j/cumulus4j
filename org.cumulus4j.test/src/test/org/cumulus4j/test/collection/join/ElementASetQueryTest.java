package org.cumulus4j.test.collection.join;

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

public class ElementASetQueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(ElementASetQueryTest.class);

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
		if (pm.getExtent(ElementASetOwner.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		{
			ElementASetOwner owner = new ElementASetOwner();
			owner.setName("Owner 1");
			owner.addElementA(new ElementA("ElementA 1.1"));
			owner.addElementA(new ElementA("ElementA 1.2"));
			owner.addElementA(new ElementA("ElementA 1.3"));
			owner.addElementA(new ElementA("ElementA 1.4"));
			pm.makePersistent(owner);
		}

		{
			ElementASetOwner owner = pm.makePersistent(new ElementASetOwner());
			owner.setName("Owner 2");
			owner.addElementA(new ElementA("ElementA 2.1"));
			owner.addElementA(new ElementA("ElementA 2.2"));
			owner.addElementA(new ElementA("ElementA 2.3"));
			owner.addElementA(new ElementA("ElementA 2.4"));
		}

		{
			ElementASetOwner owner = pm.makePersistent(new ElementASetOwner());
			owner.setName("Owner 3");
			owner.addElementA(new ElementA("ElementA 3.1"));
			owner.addElementA(new ElementA("ElementA 3.2"));
			owner.addElementA(new ElementA("ElementA 3.3"));
		}

		{
			ElementASetOwner owner = pm.makePersistent(new ElementASetOwner());
			owner.setName("Owner 4");
			owner.addElementA(new ElementA("ElementA 4.3"));
		}

		{
			ElementASetOwner owner = pm.makePersistent(new ElementASetOwner());
			owner.setName("Owner 5");
		}
	}

	private void executeQueryAndCheckResult(Query q, Object queryParam, String ... expectedOwnerNames)
	{
		String testMethodName = new Exception().getStackTrace()[4].getMethodName();

		@SuppressWarnings("unchecked")
		List<ElementASetOwner> resultList = (List<ElementASetOwner>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceFirst("^.* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " ElementASetOwners for query-filter \"" + f + "\" and param \"" + queryParam + "\":");

		Set<String> expectedOwnerNameSet = new HashSet<String>(Arrays.asList(expectedOwnerNames));

		for (ElementASetOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a ElementASetOwner with the 'name' property being null: " + resultElement, resultElement.getName());
			Assert.assertNotNull("Query returned a ElementASetOwner with the 'set' property being null: " + resultElement, resultElement.getSet());

			boolean expectedElement = expectedOwnerNameSet.remove(resultElement.getName());

			StringBuilder sb = new StringBuilder();
			for (ElementA setElement : resultElement.getSet()) {
				Assert.assertNotNull("Query returned a ElementASetOwner whose set contains a null entry!", setElement);
				Assert.assertNotNull("Query returned a ElementASetOwner whose set contains an element with a null name!", setElement.getName());

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

	private ElementA getExampleElement()
	{
		return getExampleElement("ElementA 3.2");
	}
	private ElementA getExampleElement(String name)
	{
		Query q = pm.newQuery(ElementA.class);
		q.setFilter("this.name == :name");
		q.setUnique(true);
		ElementA element = (ElementA) q.execute(name);
		if (element == null)
			throw new IllegalStateException("No matching ElementA found!");

		return element;
	}

	@Test
	public void queryContainsParameter()
	{
		ElementA element = getExampleElement();
		Query q = pm.newQuery(ElementASetOwner.class);
		q.setFilter("this.set.contains(:element)");
		executeQueryAndCheckResult(q, element, "Owner 3");
	}

	@Test
	public void queryContainsVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(ElementASetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable.name.indexOf(:elementPart) >= 0");
		// Implicit variables are now properly supported => don't need to declare them anymore.
//		q.declareVariables(ElementA.class.getName() + " elementVariable");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 4");
	}

	@Test
	public void queryContainsVariableAndVariableEquals()
	{
		ElementA element = getExampleElement();
		Query q = pm.newQuery(ElementASetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable == :element");
		// Implicit variables are now properly supported => don't need to declare them anymore.
//		q.declareVariables(ElementA.class.getName() + " elementVariable");
		executeQueryAndCheckResult(q, element, "Owner 3");
	}

	@Test
	public void queryNotContainsParameter()
	{
		ElementA element = getExampleElement();
		Query q = pm.newQuery(ElementASetOwner.class);
		q.setFilter("!this.set.contains(:element)");
		executeQueryAndCheckResult(q, element, "Owner 1", "Owner 2", "Owner 4", "Owner 5");
	}

	/**
	 * Should behave exactly like {@link #queryContainsVariableAndNotVariableIndexOf()}
	 */
	@Test
	public void queryContainsVariableAndVariableNotIndexOf()
	{
		Query q = pm.newQuery(ElementASetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && elementVariable.name.indexOf(:elementPart) < 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3");
	}

	/**
	 * Should behave exactly like {@link #queryContainsVariableAndVariableNotIndexOf()}
	 */
	@Test
	public void queryContainsVariableAndNotVariableIndexOf()
	{
		Query q = pm.newQuery(ElementASetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable.name.indexOf(:elementPart) >= 0)");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 3");
	}

	@Test
	public void queryContainsVariableAndNotVariableEquals4()
	{
		ElementA element = getExampleElement();
		Query q = pm.newQuery(ElementASetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable == :element)");
		executeQueryAndCheckResult(q, element, "Owner 1", "Owner 2", "Owner 3", "Owner 4");
	}

	@Test
	public void queryContainsVariableAndNotVariableEquals2()
	{
		ElementA element = getExampleElement("ElementA 4.3");
		Query q = pm.newQuery(ElementASetOwner.class);
		q.setFilter("this.set.contains(elementVariable) && !(elementVariable == :element)");
		executeQueryAndCheckResult(q, element, "Owner 1", "Owner 2", "Owner 3");
	}

	@Test
	public void queryNotContainsVariableAndVariableEquals()
	{
		ElementA element = getExampleElement();
		Query q = pm.newQuery(ElementASetOwner.class);
		q.setFilter("!this.set.contains(elementVariable) && elementVariable == :element");
		executeQueryAndCheckResult(q, element, "Owner 1", "Owner 2", "Owner 4", "Owner 5");
	}

	@Test
	public void queryNotContainsVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(ElementASetOwner.class);
		q.setFilter("!this.set.contains(elementVariable) && elementVariable.name.indexOf(:elementPart) >= 0");
		executeQueryAndCheckResult(q, "4", "Owner 3", "Owner 5");
	}
}
