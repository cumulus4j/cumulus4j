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

public class StringStringMapQueryTest
extends AbstractJDOTransactionalTestClearingDatabase
{
	private static final Logger logger = LoggerFactory.getLogger(StringStringMapQueryTest.class);

	@Before
	public void createTestData()
	{
		if (pm.getExtent(StringStringMapOwner.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		StringStringMapOwner stringStringMapOwner = new StringStringMapOwner();
		stringStringMapOwner.getMap().put("first", "Adam");
		stringStringMapOwner.getMap().put("second", "Bert");
		stringStringMapOwner.getMap().put("third", "Claudia");
		pm.makePersistent(stringStringMapOwner);

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
		stringStringMapOwner.getMap().put("first", "Adam");
		stringStringMapOwner.getMap().put("second", "Michael");

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
		stringStringMapOwner.getMap().put("first", "Daniel");
		stringStringMapOwner.getMap().put("second", "Marco");
		stringStringMapOwner.getMap().put("third", "Marc");
		stringStringMapOwner.getMap().put("fourth", "Khaled");

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
		stringStringMapOwner.getMap().put("first", "Marc");

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
		stringStringMapOwner.getMap().put("first", "Marco");

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());

		stringStringMapOwner = pm.makePersistent(new StringStringMapOwner());
		stringStringMapOwner.getMap().put("fourth", "Khaled");
	}

	private void executeQueryAndCheckResult(Query q, String queryParam, long ... expectedIDs)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<StringStringMapOwner> resultList = (List<StringStringMapOwner>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		String f = q.toString().replaceFirst("^.* WHERE ", "");
		logger.info(testMethodName + ": found " + resultList.size() + " StringStringMapOwners for query-filter \"" + f + "\" and param \"" + queryParam + "\":");

		Set<Long> expectedIDSet = new HashSet<Long>(expectedIDs.length);
		for (long id : expectedIDs)
			expectedIDSet.add(id);

		for (StringStringMapOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a StringStringMapOwner with the map property being null!", resultElement.getMap());
			boolean expectedElement = expectedIDSet.remove(resultElement.getId());

			StringBuilder sb = new StringBuilder();
			for (Map.Entry<String, String> mapEntry : resultElement.getMap().entrySet()) {
				Assert.assertNotNull("Query returned a StringStringMapOwner whose map contains a null key!", mapEntry.getKey());
				Assert.assertNotNull("Query returned a StringStringMapOwner whose map contains a null value!", mapEntry.getValue());

				if (sb.length() > 0)
					sb.append(", ");

				sb.append(mapEntry.getKey()).append('=').append(mapEntry.getValue());
			}

			logger.info(testMethodName + ":   * " + resultElement.getId() + ": " + sb);
			Assert.assertTrue(
					"Query returned an unexpected result-element: " + resultElement.getId(),
					expectedElement
			);
		}

		if (!expectedIDSet.isEmpty())
			Assert.fail("Query did not return the following expected result-elements: " + expectedIDSet);
	}

	@Test
	public void queryContainsKeyParameter()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, "second", 1, 2, 3);
	}

	@Test
	public void queryNotContainsKeyParameter()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("!this.map.containsKey(:queryParam)");
		executeQueryAndCheckResult(q, "second", 4, 5, 6, 7);
	}

	@Test
	public void queryContainsValueParameter()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, "Marc", 3, 4);
	}

	@Test
	public void queryNotContainsValueParameter()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("!this.map.containsValue(:queryParam)");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 5, 6, 7);
	}

	@Test
	public void queryContainsKeyVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "h", 1, 3, 7);
	}

	@Test
	public void queryNotContainsKeyVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("!this.map.containsKey(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "h", 2, 4, 5, 6);
	}

	@Test
	public void queryContainsKeyVariableAndVariableNotIndexOf()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, "h", 1, 2, 3, 4, 5);
	}

	@Test
	public void queryContainsKeyVariableAndNotVariableIndexOf()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && !(variable.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, "h", 1, 2, 3, 4, 5);
	}

	@Test
	public void queryContainsValueVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "Marc", 3, 4, 5);
	}

	@Test
	public void queryNotContainsValueVariableAndVariableIndexOf()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("!this.map.containsValue(variable) && variable.indexOf(:queryParam) >= 0");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 6, 7);
	}

	@Test
	public void queryContainsValueVariableAndVariableNotIndexOf()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable.indexOf(:queryParam) < 0");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 3, 7);
	}

	@Test
	public void queryContainsValueVariableAndNotVariableIndexOf()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && !(variable.indexOf(:queryParam) >= 0)");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 3, 7);
	}

	@Test
	public void queryContainsKeyVariableAndVariableEquals()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable == :queryParam");
		executeQueryAndCheckResult(q, "second", 1, 2, 3);
	}

	@Test
	public void queryContainsKeyVariableAndVariableNotEquals()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && variable != :queryParam");
		executeQueryAndCheckResult(q, "second", 1, 2, 3, 4, 5, 7);
	}

	@Test
	public void queryContainsKeyVariableAndNotVariableEquals()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsKey(variable) && !(variable == :queryParam)");
		executeQueryAndCheckResult(q, "second", 1, 2, 3, 4, 5, 7);
	}

	@Test
	public void queryContainsValueVariableAndVariableEquals()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable == :queryParam");
		executeQueryAndCheckResult(q, "Marc", 3, 4);
	}

	@Test
	public void queryContainsValueVariableAndVariableNotEquals()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && variable != :queryParam");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 3, 5, 7);
	}

	@Test
	public void queryContainsValueVariableAndNotVariableEquals()
	{
		Query q = pm.newQuery(StringStringMapOwner.class);
		q.setFilter("this.map.containsValue(variable) && !(variable == :queryParam)");
		executeQueryAndCheckResult(q, "Marc", 1, 2, 3, 5, 7);
	}
}
