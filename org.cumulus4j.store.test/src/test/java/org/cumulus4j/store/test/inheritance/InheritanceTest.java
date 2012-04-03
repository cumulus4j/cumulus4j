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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import junit.framework.Assert;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTest;
import org.cumulus4j.store.test.framework.CleanupUtil;
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

	@Test
	public void persistOneInheritanceObject()
	throws Exception
	{
		logger.info("createOneInheritanceObject: entered");
		pm.getExtent(InheritanceHierarchy4.class);

		InheritanceHierarchy4 inheritanceObject = new InheritanceHierarchy4();
		inheritanceObject = pm.makePersistent(inheritanceObject);
	}

	@Test
	public void persistAndIterateExtent()
	throws Exception
	{
		pm.makePersistent(new InheritanceHierarchy0());
		pm.makePersistent(new InheritanceHierarchy0());

		pm.makePersistent(new InheritanceHierarchy1());
		pm.makePersistent(new InheritanceHierarchy1());
		pm.makePersistent(new InheritanceHierarchy1());

		pm.makePersistent(new InheritanceHierarchy2());
		pm.makePersistent(new InheritanceHierarchy2());
		pm.makePersistent(new InheritanceHierarchy2());
		pm.makePersistent(new InheritanceHierarchy2());

		pm.makePersistent(new InheritanceHierarchy3());
		pm.makePersistent(new InheritanceHierarchy3());
		pm.makePersistent(new InheritanceHierarchy3());
		pm.makePersistent(new InheritanceHierarchy3());
		pm.makePersistent(new InheritanceHierarchy3());
		pm.makePersistent(new InheritanceHierarchy3());
		pm.makePersistent(new InheritanceHierarchy3());
		pm.makePersistent(new InheritanceHierarchy3());

		pm.makePersistent(new InheritanceHierarchy4());
		pm.makePersistent(new InheritanceHierarchy4());
		pm.makePersistent(new InheritanceHierarchy4());
		pm.makePersistent(new InheritanceHierarchy4());
		pm.makePersistent(new InheritanceHierarchy4());
		pm.makePersistent(new InheritanceHierarchy4());

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
}
