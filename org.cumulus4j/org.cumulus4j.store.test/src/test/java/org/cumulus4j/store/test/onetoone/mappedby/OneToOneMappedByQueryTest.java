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
package org.cumulus4j.store.test.onetoone.mappedby;

import java.util.List;

import javax.jdo.Query;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OneToOneMappedByQueryTest
extends AbstractJDOTransactionalTestClearingDatabase
{
	private static final Logger logger = LoggerFactory.getLogger(OneToOneMappedByQueryTest.class);

	@Before
	public void createTestData()
	{
		if (pm.getExtent(Root.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		Root r1 = new Root();
		r1.setName("r 1");

		LevelA a1 = new LevelA();
		a1.setName("a 1");
		r1.setLevelA(a1);

		LevelB b1 = new LevelB();
		b1.setName("b 1");
		a1.setLevelB(b1);

		pm.makePersistent(r1);


		Root r2 = pm.makePersistent(new Root());
		r2.setName("r 2");

		LevelA a2 = new LevelA();
		a2.setName("a 2");
		r2.setLevelA(a2);

		LevelB b2 = new LevelB();
		b2.setName("b 2");
		a2.setLevelB(b2);


		Root r3 = pm.makePersistent(new Root());
		r3.setName("r 3");

		LevelA a3 = new LevelA();
		a3.setName("a 3");
		r3.setLevelA(a3);

		LevelB b3 = new LevelB();
		b3.setName("b 3");
		a3.setLevelB(b3);


		Root r4 = pm.makePersistent(new Root());
		r4.setName("r 4");

		LevelA a4 = new LevelA();
		a4.setName("a 4");
		r4.setLevelA(a4);

		LevelB b4 = new LevelB();
		b4.setName("b 4");
		a4.setLevelB(b4);


		Root r5 = pm.makePersistent(new Root());
		r5.setName("r 5");

		LevelA a5 = new LevelA();
		a5.setName("a 5");
		r5.setLevelA(a5);

		LevelB b5 = new LevelB();
		b5.setName("b 5");
		a5.setLevelB(b5);
	}

	private void executeQueryAndCheckResult(Query q, String queryParam, int expectedResultListSize, boolean notEquals)
	{
		String testMethodName = new Exception().getStackTrace()[1].getMethodName();

		@SuppressWarnings("unchecked")
		List<Root> resultList = (List<Root>) q.execute(queryParam);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);

		logger.info(testMethodName + ": found " + resultList.size() + " objects:");
		Assert.assertEquals("Query returned wrong number of results!", expectedResultListSize, resultList.size());
		for (Root root : resultList) {
			Assert.assertNotNull("Query returned instance of Root with root.levelA being null!", root.getLevelA());
			Assert.assertNotNull("Query returned instance of Root with root.levelA.levelB being null!", root.getLevelA().getLevelB());

			if (notEquals)
				Assert.assertFalse("Query returned an object that did not match the criteria: " + root, queryParam.equals(root.getLevelA().getLevelB().getName()));
			else
				Assert.assertEquals(queryParam, root.getLevelA().getLevelB().getName());

			logger.info(testMethodName + ":  * " + root.getName() + " (root.levelA.name=\"" + root.getLevelA().getName() + "\" root.levelA.levelB.name=\"" + root.getLevelA().getLevelB().getName() + "\")");
		}
	}

	@Test
	public void queryLevelBNameEquals()
	{
		Query q = pm.newQuery(Root.class);
		q.setFilter("this.levelA.levelB.name == :name");
		executeQueryAndCheckResult(q, "b 1", 1, false);
	}

	@Test
	public void queryLevelBNameNotEquals()
	{
		Query q = pm.newQuery(Root.class);
		q.setFilter("this.levelA.levelB.name != :name");
		executeQueryAndCheckResult(q, "b 1", 4, true);
	}

	@Test
	public void queryNotLevelBNameEquals()
	{
		Query q = pm.newQuery(Root.class);
		q.setFilter("!(this.levelA.levelB.name == :name)");
		executeQueryAndCheckResult(q, "b 1", 4, true);
	}

	@Test
	public void queryNotLevelBNameNotEquals()
	{
		Query q = pm.newQuery(Root.class);
		q.setFilter("!(this.levelA.levelB.name != :name)");
		executeQueryAndCheckResult(q, "b 1", 1, false);
	}

	@Test
	public void rightToLeft_queryLevelBNameEquals()
	{
		Query q = pm.newQuery(Root.class);
		q.setFilter(":name == this.levelA.levelB.name");
		executeQueryAndCheckResult(q, "b 1", 1, false);
	}

	@Test
	public void rightToLeft_queryLevelBNameNotEquals()
	{
		Query q = pm.newQuery(Root.class);
		q.setFilter(":name != this.levelA.levelB.name");
		executeQueryAndCheckResult(q, "b 1", 4, true);
	}

	@Test
	public void rightToLeft_queryNotLevelBNameEquals()
	{
		Query q = pm.newQuery(Root.class);
		q.setFilter("!(:name == this.levelA.levelB.name)");
		executeQueryAndCheckResult(q, "b 1", 4, true);
	}

	@Test
	public void rightToLeft_queryNotLevelBNameNotEquals()
	{
		Query q = pm.newQuery(Root.class);
		q.setFilter("!(:name != this.levelA.levelB.name)");
		executeQueryAndCheckResult(q, "b 1", 1, false);
	}

}
