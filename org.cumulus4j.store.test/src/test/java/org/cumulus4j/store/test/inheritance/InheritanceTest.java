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
package org.cumulus4j.store.test.inheritance;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.Query;

import junit.framework.Assert;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTest;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InheritanceTest
extends AbstractJDOTransactionalTest
{
	private static final Logger logger = LoggerFactory.getLogger(InheritanceTest.class);

	@BeforeClass
	public static void clearDatabase()
	throws Exception
	{
		logger.info("clearDatabase: Clearing database (dropping all tables).");
		CleanupUtil.dropAllTables();
	}

	@Before
	public void deleteAllInstances()
	{
		for (Iterator<?> it = pm.getExtent(InheritanceHierarchy0.class).iterator(); it.hasNext(); ) {
			Object o = it.next();
			pm.deletePersistent(o);
		}
	}

	@Test
	public void persistOneInheritanceObject()
	throws Exception
	{
		logger.info("createOneInheritanceObject: entered");
		pm.getExtent(InheritanceHierarchy4.class);

		InheritanceHierarchy4 inheritanceObject = new InheritanceHierarchy4();
		inheritanceObject = pm.makePersistent(inheritanceObject);
	}

	private Map<Class<? extends InheritanceHierarchy0>, List<InheritanceHierarchy0>> persistSomeInstances()
	{
		Map<Class<? extends InheritanceHierarchy0>, List<InheritanceHierarchy0>> result = new HashMap<Class<? extends InheritanceHierarchy0>, List<InheritanceHierarchy0>>();
		List<InheritanceHierarchy0> list;

		list = new ArrayList<InheritanceHierarchy0>(); result.put(InheritanceHierarchy0.class, list);
		list.add(pm.makePersistent(new InheritanceHierarchy0()));
		list.add(pm.makePersistent(new InheritanceHierarchy0()));

		list = new ArrayList<InheritanceHierarchy0>(); result.put(InheritanceHierarchy1.class, list);
		list.add(pm.makePersistent(new InheritanceHierarchy1()));
		list.add(pm.makePersistent(new InheritanceHierarchy1()));
		list.add(pm.makePersistent(new InheritanceHierarchy1()));

		list = new ArrayList<InheritanceHierarchy0>(); result.put(InheritanceHierarchy2.class, list);
		list.add(pm.makePersistent(new InheritanceHierarchy2()));
		list.add(pm.makePersistent(new InheritanceHierarchy2()));
		list.add(pm.makePersistent(new InheritanceHierarchy2()));
		list.add(pm.makePersistent(new InheritanceHierarchy2()));

		list = new ArrayList<InheritanceHierarchy0>(); result.put(InheritanceHierarchy3.class, list);
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));
		list.add(pm.makePersistent(new InheritanceHierarchy3()));

		list = new ArrayList<InheritanceHierarchy0>(); result.put(InheritanceHierarchy4.class, list);
		list.add(pm.makePersistent(new InheritanceHierarchy4()));
		list.add(pm.makePersistent(new InheritanceHierarchy4()));
		list.add(pm.makePersistent(new InheritanceHierarchy4()));
		list.add(pm.makePersistent(new InheritanceHierarchy4()));
		list.add(pm.makePersistent(new InheritanceHierarchy4()));
		list.add(pm.makePersistent(new InheritanceHierarchy4()));

		return result;
	}

	@Test
	public void persistAndQueryIncludingSubclasses()
	throws Exception
	{
		persistSomeInstances();

		commitAndBeginNewTransaction();

		{
			Query query = pm.newQuery(InheritanceHierarchy0.class);
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(23, result.size());
		}

		{
			Query query = pm.newQuery(InheritanceHierarchy1.class);
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(21, result.size());
		}

		{
			Query query = pm.newQuery(InheritanceHierarchy2.class);
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(18, result.size());
		}

		{
			Query query = pm.newQuery(InheritanceHierarchy3.class);
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(14, result.size());
		}

		{
			Query query = pm.newQuery(InheritanceHierarchy4.class);
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(6, result.size());
		}
	}

	@Test
	public void persistAndQueryExcludingSubclassesUsingExtent()
	throws Exception
	{
		persistSomeInstances();

		commitAndBeginNewTransaction();

		{
			Query query = pm.newQuery(pm.getExtent(InheritanceHierarchy0.class, false));
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(2, result.size());
		}

		{
			Query query = pm.newQuery(pm.getExtent(InheritanceHierarchy1.class, false));
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(3, result.size());
		}

		{
			Query query = pm.newQuery(pm.getExtent(InheritanceHierarchy2.class, false));
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(4, result.size());
		}

		{
			Query query = pm.newQuery(pm.getExtent(InheritanceHierarchy3.class, false));
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(8, result.size());
		}

		{
			Query query = pm.newQuery(pm.getExtent(InheritanceHierarchy4.class, false));
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(6, result.size());
		}
	}

	@Test
	public void persistAndQueryExcludingSubclassesUsingJDOQL()
	throws Exception
	{
		persistSomeInstances();

		commitAndBeginNewTransaction();

		{
			Query query = pm.newQuery("SELECT FROM " + InheritanceHierarchy0.class.getName() + " EXCLUDE SUBCLASSES");
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(2, result.size());
		}

		{
			Query query = pm.newQuery("SELECT FROM " + InheritanceHierarchy1.class.getName() + " EXCLUDE SUBCLASSES");
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(3, result.size());
		}

		{
			Query query = pm.newQuery("SELECT FROM " + InheritanceHierarchy2.class.getName() + " EXCLUDE SUBCLASSES");
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(4, result.size());
		}

		{
			Query query = pm.newQuery("SELECT FROM " + InheritanceHierarchy3.class.getName() + " EXCLUDE SUBCLASSES");
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(8, result.size());
		}

		{
			Query query = pm.newQuery("SELECT FROM " + InheritanceHierarchy4.class.getName() + " EXCLUDE SUBCLASSES");
			Collection<?> result = (Collection<?>) query.execute();
			Assert.assertEquals(6, result.size());
		}
	}

	@Test
	public void persistAndIterateExtent()
	throws Exception
	{
		persistSomeInstances();

		commitAndBeginNewTransaction();

		Map<Class<?>, Integer[]> class2count = new HashMap<Class<?>, Integer[]>();
		for (Iterator<?> it = pm.getExtent(InheritanceHierarchy0.class).iterator(); it.hasNext(); ) {
			Object o = it.next();
			Assert.assertNotNull(o);
			Integer[] count = class2count.get(o.getClass());
			if (count == null) {
				count = new Integer[] { 0 };
				class2count.put(o.getClass(), count);
			}
			++count[0];
		}

		Assert.assertNotNull(class2count.get(InheritanceHierarchy0.class));
		Assert.assertNotNull(class2count.get(InheritanceHierarchy1.class));
		Assert.assertNotNull(class2count.get(InheritanceHierarchy2.class));
		Assert.assertNotNull(class2count.get(InheritanceHierarchy3.class));
		Assert.assertNotNull(class2count.get(InheritanceHierarchy4.class));

		Assert.assertEquals(Integer.valueOf(2), class2count.get(InheritanceHierarchy0.class)[0]);
		Assert.assertEquals(Integer.valueOf(3), class2count.get(InheritanceHierarchy1.class)[0]);
		Assert.assertEquals(Integer.valueOf(4), class2count.get(InheritanceHierarchy2.class)[0]);
		Assert.assertEquals(Integer.valueOf(8), class2count.get(InheritanceHierarchy3.class)[0]);
		Assert.assertEquals(Integer.valueOf(6), class2count.get(InheritanceHierarchy4.class)[0]);
	}

	@Test
	public void persistAndQuery()
	throws Exception
	{
		Map<Class<? extends InheritanceHierarchy0>, List<InheritanceHierarchy0>> map = persistSomeInstances();

		pm.getFetchPlan().setMaxFetchDepth(-1);
		pm.getFetchPlan().setGroup(FetchPlan.ALL);

		List<InheritanceHierarchy0> list2 = map.get(InheritanceHierarchy2.class);
		Collection<InheritanceHierarchy0> c2 = pm.detachCopyAll(list2);

		List<InheritanceHierarchy0> list3 = map.get(InheritanceHierarchy3.class);
		Collection<InheritanceHierarchy0> c3 = pm.detachCopyAll(list3);

		commitAndBeginNewTransaction();

		InheritanceHierarchy2 obj2 = (InheritanceHierarchy2) c2.iterator().next();
		InheritanceHierarchy3 obj3 = (InheritanceHierarchy3) c3.iterator().next();

		Query query = pm.newQuery(InheritanceHierarchy2.class);
		query.setFilter("this.i2 == :arg");

		Collection<?> queryResult = (Collection<?>) query.execute(obj2.getI2());
		Assert.assertTrue(queryResult.contains(obj2));
		for (Object object : queryResult) {
			InheritanceHierarchy2 o = (InheritanceHierarchy2) object;
			Assert.assertEquals(obj2.getI2(), o.getI2());
		}

		queryResult = (Collection<?>) query.execute(obj3.getI2());
		Assert.assertTrue(queryResult.contains(obj3));
		for (Object object : queryResult) {
			InheritanceHierarchy2 o = (InheritanceHierarchy2) object;
			Assert.assertEquals(obj3.getI2(), o.getI2());
		}
	}
}
