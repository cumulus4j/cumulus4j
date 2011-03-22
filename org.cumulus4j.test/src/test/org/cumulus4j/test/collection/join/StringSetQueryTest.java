package org.cumulus4j.test.collection.join;

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

public class StringSetQueryTest
extends AbstractTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(StringSetQueryTest.class);

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
		if (pm.getExtent(StringSetOwner.class).iterator().hasNext()) {
			logger.info("createTestData: There is already test data. Skipping creation.");
			return;
		}

		StringSetOwner stringSetOwner = new StringSetOwner();
		stringSetOwner.getSet().add("Adam");
		stringSetOwner.getSet().add("Bert");
		stringSetOwner.getSet().add("Claudia");
		pm.makePersistent(stringSetOwner);

		stringSetOwner = pm.makePersistent(new StringSetOwner());
		stringSetOwner.getSet().add("Adam");
		stringSetOwner.getSet().add("Michael");

		stringSetOwner = pm.makePersistent(new StringSetOwner());
		stringSetOwner.getSet().add("Daniel");
		stringSetOwner.getSet().add("Marco");
		stringSetOwner.getSet().add("Marc");
		stringSetOwner.getSet().add("Khaled");

		stringSetOwner = pm.makePersistent(new StringSetOwner());
		stringSetOwner.getSet().add("Marc");

		stringSetOwner = pm.makePersistent(new StringSetOwner());
		stringSetOwner.getSet().add("Marco");

		stringSetOwner = pm.makePersistent(new StringSetOwner());
	}

	@Test
	public void query0()
	{
		Query q = pm.newQuery(StringSetOwner.class);
		q.setFilter("this.set.contains(:element)");

		String element = "Marc";

		@SuppressWarnings("unchecked")
		List<StringSetOwner> resultList = (List<StringSetOwner>) q.execute(element);
		Assert.assertNotNull("Query returned null as result when a List was expected!", resultList);
		logger.info("query0: found " + resultList.size() + " containing the element \"" + element + "\":");
		Assert.assertEquals("Query returned wrong number of results!", 2, resultList.size());
		for (StringSetOwner resultElement : resultList) {
			Assert.assertNotNull("Query returned a StringSetOwner with the set property being null!", resultElement.getSet());


			StringBuilder sb = new StringBuilder();
			for (String setElement : resultElement.getSet()) {
				Assert.assertNotNull("Query returned a StringSetOwner whose set contains a null entry!", setElement);

				if (sb.length() > 0)
					sb.append(", ");

				sb.append(setElement);
			}

			logger.info("query0:   * " + resultElement.getId() + ": " + sb);
		}
	}
}
