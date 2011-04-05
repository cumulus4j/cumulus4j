package org.cumulus4j.test.onetoone.mappedby;

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

public class OneToOneMappedByQueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(OneToOneMappedByQueryTest.class);

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
}
