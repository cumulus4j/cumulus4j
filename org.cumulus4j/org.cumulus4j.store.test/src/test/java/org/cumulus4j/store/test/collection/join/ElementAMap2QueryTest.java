/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.test.collection.join;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jdo.Query;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElementAMap2QueryTest
extends AbstractJDOTransactionalTestClearingDatabase
{
	private static final Logger logger = LoggerFactory.getLogger(ElementAMap2QueryTest.class);

	@Before
	public void createTestData()
	{
		if (pm.getExtent(ElementAMapOwner2.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		ElementA elementA_1_1 = new ElementA("MM.Element 1.1");

		{
			ElementAMapOwner2 owner = new ElementAMapOwner2();
			owner.setName("Owner 1");
			owner.putMapEntry(elementA_1_1, "aaa");
			owner.putMapEntry(new ElementA("MM.Element 1.2"), "bbb");
			owner.putMapEntry(new ElementA("MM.Element 1.3"), "ccc");
			owner.putMapEntry(new ElementA("MM.Element 1.4"), "ddd");
			pm.makePersistent(owner);
		}

		{
			ElementAMapOwner2 owner = pm.makePersistent(new ElementAMapOwner2());
			owner.setName("Owner 2");
			owner.putMapEntry(new ElementA("MM.Element 2.1"), "aa");
			owner.putMapEntry(new ElementA("MM.Element 2.2"), "bb");
			owner.putMapEntry(new ElementA("MM.Element 2.3"), "cc");
			owner.putMapEntry(new ElementA("MM.Element 2.4"), "dd");
		}

		{
			ElementAMapOwner2 owner = pm.makePersistent(new ElementAMapOwner2());
			owner.setName("Owner 3");
			owner.putMapEntry(elementA_1_1, "a");
			owner.putMapEntry(new ElementA("MM.Element 3.2"), "b");
			owner.putMapEntry(new ElementA("MM.Element 3.3"), "c");
		}

		{
			ElementAMapOwner2 owner = pm.makePersistent(new ElementAMapOwner2());
			owner.setName("Owner 4");
			owner.putMapEntry(new ElementA("MM.Element 4.3"), "ccc");
		}

		{
			ElementAMapOwner2 owner = pm.makePersistent(new ElementAMapOwner2());
			owner.setName("Owner 5");
		}

		{
			ElementAMapOwner2 owner = pm.makePersistent(new ElementAMapOwner2());
			owner.setName("Owner 6");
			owner.putMapEntry(new ElementA("MM.Element 6.2"), "bb");
		}
	}

	private void executeQueryAndCheckResult(Query q, Object queryParam, String ... expectedOwnerNames)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<ElementAMapOwner2> resultList = (List<ElementAMapOwner2>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceFirst("^.* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " ElementAMapOwner2s for query-filter \"" + f + "\" and param \"" + queryParam + "\":");

		Set<String> expectedOwnerNameSet = new HashSet<String>(Arrays.asList(expectedOwnerNames));

		for (ElementAMapOwner2 resultElement : resultList) {
			Assert.assertNotNull("Query returned a ElementAMapOwner2 with the 'name' property being null: " + resultElement, resultElement.getName());
			Assert.assertNotNull("Query returned a ElementAMapOwner2 with the 'map' property being null!", resultElement.getMap());

			boolean expectedElement = expectedOwnerNameSet.remove(resultElement.getName());

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<ElementA, String> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a ElementAMapOwner2 whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a ElementAMapOwner2 whose map contains a null value!", mapEntry.getValue());

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
		return getExampleElement("MM.Element 3.2");
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
	public void queryContainsValueParameter()
	{
		Query q = pm.newQuery(ElementAMapOwner2.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, "ccc", "Owner 1", "Owner 4");
	}

	@Test
	public void queryNotContainsValueParameter()
	{
		Query q = pm.newQuery(ElementAMapOwner2.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, "ccc", "Owner 2", "Owner 3", "Owner 5", "Owner 6");
	}

	@Test
	public void queryContainsValueVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner2.class);
		q.setFilter("this.map.containsValue(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "bb", "Owner 1", "Owner 2", "Owner 6");
	}

	@Test
	public void queryContainsKeyParameter1()
	{
		ElementA elementA = getExampleElement();
		Query q = pm.newQuery(ElementAMapOwner2.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 3");
	}

	@Test
	public void queryContainsKeyParameter2()
	{
		ElementA elementA = getExampleElement("MM.Element 1.1");
		Query q = pm.newQuery(ElementAMapOwner2.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 1", "Owner 3");
	}

	@Test
	public void queryNotContainsKeyParameter1()
	{
		ElementA elementA = getExampleElement();

		Query q = pm.newQuery(ElementAMapOwner2.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 1", "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	@Test
	public void queryNotContainsKeyParameter2()
	{
		ElementA elementA = getExampleElement("MM.Element 1.1");

		Query q = pm.newQuery(ElementAMapOwner2.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, elementA, "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	@Test
	public void queryContainsKeyVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(ElementAMapOwner2.class);
		q.setFilter("this.map.containsKey(variable) && variable.name.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 4");
	}
}
