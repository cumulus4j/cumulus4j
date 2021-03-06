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
package org.cumulus4j.store.test.collection.mappedby;

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

public class Element3MapQueryTest
extends AbstractJDOTransactionalTestClearingDatabase
{
	private static final Logger logger = LoggerFactory.getLogger(Element3MapQueryTest.class);

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
			owner.addElement3(new Element3("aaa", "Element 1.1"));
			owner.addElement3(new Element3("bbb", "Element 1.2"));
			owner.addElement3(new Element3("ccc", "Element 1.3"));
			owner.addElement3(new Element3("ddd", "Element 1.4"));
			pm.makePersistent(owner);
		}

		{
			Element3MapOwner owner = pm.makePersistent(new Element3MapOwner());
			owner.setName("Owner 2");
			owner.addElement3(new Element3("aa", "Element 2.1"));
			owner.addElement3(new Element3("bb", "Element 2.2"));
			owner.addElement3(new Element3("cc", "Element 2.3"));
			owner.addElement3(new Element3("dd", "Element 2.4"));
		}

		{
			Element3MapOwner owner = pm.makePersistent(new Element3MapOwner());
			owner.setName("Owner 3");
			owner.addElement3(new Element3("a", "Element 3.1"));
			owner.addElement3(new Element3("b", "Element 3.2"));
			owner.addElement3(new Element3("c", "Element 3.3"));
		}

		{
			Element3MapOwner owner = pm.makePersistent(new Element3MapOwner());
			owner.setName("Owner 4");
			owner.addElement3(new Element3("ccc", "Element 4.3"));
		}

		{
			Element3MapOwner owner = pm.makePersistent(new Element3MapOwner());
			owner.setName("Owner 5");
		}

		{
			Element3MapOwner owner = pm.makePersistent(new Element3MapOwner());
			owner.setName("Owner 6");
			owner.addElement3(new Element3("bb", "Element 6.2"));
		}
	}

	private void executeQueryAndCheckResult(Query q, Object queryParam, String ... expectedOwnerNames)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<Element3MapOwner> resultList = (List<Element3MapOwner>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceFirst("^.* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " Element3MapOwners for query-filter \"" + f + "\" and param \"" + queryParam + "\":");

		Set<String> expectedOwnerNameSet = new HashSet<String>(Arrays.asList(expectedOwnerNames));

		for (Element3MapOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a Element3MapOwner with the 'name' property being null: " + resultElement, resultElement.getName());
			Assert.assertNotNull("Query returned a Element3MapOwner with the 'map' property being null!", resultElement.getMap());

			boolean expectedElement = expectedOwnerNameSet.remove(resultElement.getName());

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<Element3, String> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a Element3MapOwner whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a Element3MapOwner whose map contains a null value!", mapEntry.getValue());

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

	private Element3 getExampleElement()
	{
		Query q = pm.newQuery(Element3.class);
		q.setFilter("this.name == :name");
		q.setUnique(true);
		Element3 element = (Element3) q.execute("Element 3.2");
		if (element == null)
			throw new IllegalStateException("No matching element found!");

		return element;
	}

	@Test
	public void queryContainsValueParameter()
	{
		Query q = pm.newQuery(Element3MapOwner.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, "ccc", "Owner 1", "Owner 4");
	}

	@Test
	public void queryNotContainsValueParameter()
	{
		Query q = pm.newQuery(Element3MapOwner.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, "ccc", "Owner 2", "Owner 3", "Owner 5", "Owner 6");
	}

	@Test
	public void queryContainsValueVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(Element3MapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "bb", "Owner 1", "Owner 2", "Owner 6");
	}

	@Test
	public void queryContainsKeyParameter()
	{
		Element3 element3 = getExampleElement();

		Query q = pm.newQuery(Element3MapOwner.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, element3, "Owner 3");
	}

	@Test
	public void queryNotContainsKeyParameter()
	{
		Element3 element3 = getExampleElement();

		Query q = pm.newQuery(Element3MapOwner.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, element3, "Owner 1", "Owner 2", "Owner 4", "Owner 5", "Owner 6");
	}

	@Test
	public void queryContainsKeyVariableAndIndexOfMatches()
	{
		Query q = pm.newQuery(Element3MapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.name.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "4", "Owner 1", "Owner 2", "Owner 4");
	}
}
