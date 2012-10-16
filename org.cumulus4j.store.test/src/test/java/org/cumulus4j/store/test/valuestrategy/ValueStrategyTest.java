package org.cumulus4j.store.test.valuestrategy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.Test;

public class ValueStrategyTest extends AbstractJDOTransactionalTestClearingDatabase
{
	@Test
	public void identity() {
		System.out.println(">>> identity - begin");
		pm.makePersistent(new EntityIdentity("Anton"));
		pm.makePersistent(new EntityIdentity("Emil"));
		pm.makePersistent(new EntityIdentitySub("Otto", "test1"));

		commitAndBeginNewTransaction();

		@SuppressWarnings("unchecked")
		Collection<EntityIdentity> entities = (Collection<EntityIdentity>) pm.newQuery(EntityIdentity.class).execute();
		Assert.assertEquals("entities.size() is wrong!", 3, entities.size());
		Set<Long> ids = new HashSet<Long>();
		int countSub = 0;
		for (EntityIdentity entity : entities) {
			System.out.println("  * " + entity);
			Assert.assertTrue("entity.id < 0", entity.getId() >= 0);
			ids.add(entity.getId());
			if (entity instanceof EntityIdentitySub)
				++countSub;
		}
		Assert.assertEquals("ids.size() is wrong!", entities.size(), ids.size());
		Assert.assertEquals("countSub wrong!", 1, countSub);
		System.out.println("<<< identity - end");
	}

	@Test
	public void increment() {
		System.out.println(">>> increment - begin");
		pm.makePersistent(new EntityIncrement("Anton"));
		pm.makePersistent(new EntityIncrement("Emil"));
		pm.makePersistent(new EntityIncrementSub("Otto", "test1"));

		commitAndBeginNewTransaction();

		@SuppressWarnings("unchecked")
		Collection<EntityIncrement> entities = (Collection<EntityIncrement>) pm.newQuery(EntityIncrement.class).execute();
		Assert.assertEquals("entities.size() is wrong!", 3, entities.size());
		Set<Long> ids = new HashSet<Long>();
		int countSub = 0;
		for (EntityIncrement entity : entities) {
			System.out.println("  * " + entity);
			Assert.assertTrue("entity.id < 0", entity.getId() >= 0);
			ids.add(entity.getId());
			if (entity instanceof EntityIncrementSub)
				++countSub;
		}
		Assert.assertEquals("ids.size() is wrong!", entities.size(), ids.size());
		Assert.assertEquals("countSub wrong!", 1, countSub);
		System.out.println("<<< increment - end");
	}

	@Test
	public void native_() {
		System.out.println(">>> native_ - begin");
		pm.makePersistent(new EntityNative("Anton"));
		pm.makePersistent(new EntityNative("Emil"));
		pm.makePersistent(new EntityNativeSub("Otto", "test1"));

		commitAndBeginNewTransaction();

		@SuppressWarnings("unchecked")
		Collection<EntityNative> entities = (Collection<EntityNative>) pm.newQuery(EntityNative.class).execute();
		Assert.assertEquals("entities.size() is wrong!", 3, entities.size());
		Set<Long> ids = new HashSet<Long>();
		int countSub = 0;
		for (EntityNative entity : entities) {
			System.out.println("  * " + entity);
			Assert.assertTrue("entity.id < 0", entity.getId() >= 0);
			ids.add(entity.getId());
			if (entity instanceof EntityNativeSub)
				++countSub;
		}
		Assert.assertEquals("ids.size() is wrong!", entities.size(), ids.size());
		Assert.assertEquals("countSub wrong!", 1, countSub);
		System.out.println("<<< native_ - end");
	}

	@Test
	public void sequence() {
		System.out.println(">>> sequence - begin");
		pm.makePersistent(new EntitySequence("Anton"));
		pm.makePersistent(new EntitySequence("Emil"));
		pm.makePersistent(new EntitySequenceSub("Otto", "test1"));

		commitAndBeginNewTransaction();

		@SuppressWarnings("unchecked")
		Collection<EntitySequence> entities = (Collection<EntitySequence>) pm.newQuery(EntitySequence.class).execute();
		Assert.assertEquals("entities.size() is wrong!", 3, entities.size());
		Set<Long> ids = new HashSet<Long>();
		int countSub = 0;
		for (EntitySequence entity : entities) {
			System.out.println("  * " + entity);
			Assert.assertTrue("entity.id < 0", entity.getId() >= 0);
			ids.add(entity.getId());
			if (entity instanceof EntitySequenceSub)
				++countSub;
		}
		Assert.assertEquals("ids.size() is wrong!", entities.size(), ids.size());
		Assert.assertEquals("countSub wrong!", 1, countSub);
		System.out.println("<<< sequence - end");
	}
}
