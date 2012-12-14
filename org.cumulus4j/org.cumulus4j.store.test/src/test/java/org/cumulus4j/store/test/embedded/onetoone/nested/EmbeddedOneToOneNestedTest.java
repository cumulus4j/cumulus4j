package org.cumulus4j.store.test.embedded.onetoone.nested;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.Query;

import junit.framework.Assert;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbeddedOneToOneNestedTest extends AbstractJDOTransactionalTestClearingDatabase {
	private static final Logger logger = LoggerFactory.getLogger(EmbeddedOneToOneNestedTest.class);

	@Before
	public void before() throws Exception {
		final Class<?>[] classesToClear = {
			EmbeddedA.class,
			EmbeddedA0.class,
			EmbeddedA1.class,
			EmbeddedB.class,
			EmbeddedContainer.class
		};

		for (Class<?> clazz : classesToClear) {
			for (Iterator<?> it = pm.getExtent(clazz).iterator(); it.hasNext(); ) {
				pm.deletePersistent(it.next());
			}
			pm.flush();
		}
	}

	@Test
	public void writeAndRead() {
		// BEGIN populate meta-data in separate tx for debugging reasons
		pm.getExtent(EmbeddedContainer.class);
		commitAndBeginNewTransaction();
		// END populate meta-data in separate tx for debugging reasons

//		EmbeddedContainer container = new EmbeddedContainer();
//		container.setName("container");
//
//		EmbeddedA embeddedA = createEmbeddedA("");
//		container.setEmbeddedA(embeddedA);
//
//		EmbeddedA againEmbeddedA = createEmbeddedA("again_");
//		container.setAgainEmbeddedA(againEmbeddedA);
//
//		EmbeddedB embeddedB = new EmbeddedB();
//		container.setEmbeddedB(embeddedB);
//		embeddedB.setName("embeddedB");
		EmbeddedContainer container = createEmbeddedContainer("");
		pm.makePersistent(container);

		commitAndBeginNewTransaction();

		Iterator<EmbeddedContainer> iterator = pm.getExtent(EmbeddedContainer.class).iterator();
		Assert.assertTrue("No EmbeddedContainer found in database!", iterator.hasNext());
		EmbeddedContainer embeddedContainer = iterator.next();
		Assert.assertFalse("More than one EmbeddedContainer found in database!", iterator.hasNext());

		// TODO check, why the hell the pm.getExtent(...) code below finds instances (is that a bug in DN?). but later...

//		Assert.assertTrue("non-embedded EmbeddedA found in database!", pm.getExtent(EmbeddedA.class).iterator().hasNext());
//		Assert.assertTrue("non-embedded EmbeddedA0 found in database!", pm.getExtent(EmbeddedA0.class).iterator().hasNext());
//		Assert.assertTrue("non-embedded EmbeddedA1 found in database!", pm.getExtent(EmbeddedA1.class).iterator().hasNext());
//		Assert.assertTrue("non-embedded EmbeddedB found in database!", pm.getExtent(EmbeddedB.class).iterator().hasNext());

		Assert.assertTrue("non-embedded EmbeddedA found in database!", ((Collection<?>)pm.newQuery(EmbeddedA.class).execute()).isEmpty());
		Assert.assertTrue("non-embedded EmbeddedA0 found in database!", ((Collection<?>)pm.newQuery(EmbeddedA0.class).execute()).isEmpty());
		Assert.assertTrue("non-embedded EmbeddedA1 found in database!", ((Collection<?>)pm.newQuery(EmbeddedA1.class).execute()).isEmpty());
		Assert.assertTrue("non-embedded EmbeddedB found in database!", ((Collection<?>)pm.newQuery(EmbeddedB.class).execute()).isEmpty());

		assertEmbeddedContainerCorrect("", embeddedContainer);
	}

	private void assertEmbeddedContainerCorrect(String namePrefix, EmbeddedContainer embeddedContainer) {
		Assert.assertNotNull(embeddedContainer.getEmbeddedA());
		Assert.assertNotNull(embeddedContainer.getEmbeddedB());
		Assert.assertNotNull(embeddedContainer.getAgainEmbeddedA());
		Assert.assertNotNull(embeddedContainer.getEmbeddedA().getEmbeddedA0());
		Assert.assertNotNull(embeddedContainer.getEmbeddedA().getEmbeddedA1());
		Assert.assertNotNull(embeddedContainer.getAgainEmbeddedA().getEmbeddedA0());
		Assert.assertNotNull(embeddedContainer.getAgainEmbeddedA().getEmbeddedA1());

		Assert.assertEquals(namePrefix + "container", embeddedContainer.getName());
		Assert.assertEquals(namePrefix + "embeddedA", embeddedContainer.getEmbeddedA().getName());
		Assert.assertEquals(namePrefix + "embeddedA0", embeddedContainer.getEmbeddedA().getEmbeddedA0().getName());
		Assert.assertEquals(namePrefix + "embeddedA1", embeddedContainer.getEmbeddedA().getEmbeddedA1().getName());

		Assert.assertEquals(namePrefix + "embeddedB", embeddedContainer.getEmbeddedB().getName());

		Assert.assertEquals(namePrefix + "again_embeddedA", embeddedContainer.getAgainEmbeddedA().getName());
		Assert.assertEquals(namePrefix + "again_embeddedA0", embeddedContainer.getAgainEmbeddedA().getEmbeddedA0().getName());
		Assert.assertEquals(namePrefix + "again_embeddedA1", embeddedContainer.getAgainEmbeddedA().getEmbeddedA1().getName());
	}

	private EmbeddedContainer createEmbeddedContainer(String namePrefix) {
		EmbeddedContainer container = new EmbeddedContainer();
		container.setName(namePrefix + "container");

		EmbeddedA embeddedA = createEmbeddedA(namePrefix);
		container.setEmbeddedA(embeddedA);

		EmbeddedA againEmbeddedA = createEmbeddedA(namePrefix + "again_");
		container.setAgainEmbeddedA(againEmbeddedA);

		EmbeddedB embeddedB = new EmbeddedB();
		container.setEmbeddedB(embeddedB);
		embeddedB.setName(namePrefix + "embeddedB");

		return container;
	}

	private EmbeddedA createEmbeddedA(String namePrefix) {
		EmbeddedA embeddedA = new EmbeddedA();
		embeddedA.setName(namePrefix + "embeddedA");

		EmbeddedA0 embeddedA0 = new EmbeddedA0();
		embeddedA.setEmbeddedA0(embeddedA0);
		embeddedA0.setName(namePrefix + "embeddedA0");

		EmbeddedA1 embeddedA1 = new EmbeddedA1();
		embeddedA.setEmbeddedA1(embeddedA1);
		embeddedA1.setName(namePrefix + "embeddedA1");
		return embeddedA;
	}

	@Test
	public void query1stLevel() {
		pm.makePersistent(createEmbeddedContainer("1."));
		pm.makePersistent(createEmbeddedContainer("2."));
		pm.makePersistent(createEmbeddedContainer("3."));
		pm.makePersistent(createEmbeddedContainer("1001."));
		pm.makePersistent(createEmbeddedContainer("1002."));

		{
			Query q = pm.newQuery(EmbeddedContainer.class);
			q.setFilter("this.embeddedA.name == :embeddedAName");

			@SuppressWarnings("unchecked")
			Collection<EmbeddedContainer> c = (Collection<EmbeddedContainer>) q.execute("2.embeddedA");
			Assert.assertEquals(1, c.size());
			EmbeddedContainer embeddedContainer = c.iterator().next();
			assertEmbeddedContainerCorrect("2.", embeddedContainer);
		}

		{
			Query q = pm.newQuery(EmbeddedContainer.class);
			q.setFilter("this.embeddedA.name.indexOf(:embeddedAName) >= 0");
			q.setOrdering("this.name descending");

			@SuppressWarnings("unchecked")
			Collection<EmbeddedContainer> c = (Collection<EmbeddedContainer>) q.execute("100");
			Assert.assertEquals(2, c.size());
			Iterator<EmbeddedContainer> it = c.iterator();
			assertEmbeddedContainerCorrect("1002.", it.next());
			assertEmbeddedContainerCorrect("1001.", it.next());
		}
	}

	@Test
	public void query2ndLevel() {
		pm.makePersistent(createEmbeddedContainer("1."));
		pm.makePersistent(createEmbeddedContainer("2."));
		pm.makePersistent(createEmbeddedContainer("3."));
		pm.makePersistent(createEmbeddedContainer("1001."));
		pm.makePersistent(createEmbeddedContainer("1002."));

		{
			Query q = pm.newQuery(EmbeddedContainer.class);
			q.setFilter("this.embeddedA.embeddedA0.name == :embeddedAName");

			@SuppressWarnings("unchecked")
			Collection<EmbeddedContainer> c = (Collection<EmbeddedContainer>) q.execute("2.embeddedA0");
			Assert.assertEquals(1, c.size());
			EmbeddedContainer embeddedContainer = c.iterator().next();
			assertEmbeddedContainerCorrect("2.", embeddedContainer);
		}

		{
			Query q = pm.newQuery(EmbeddedContainer.class);
			q.setFilter("this.embeddedA.embeddedA0.name.indexOf(:embeddedAName) >= 0");
			q.setOrdering("this.name descending");

			@SuppressWarnings("unchecked")
			Collection<EmbeddedContainer> c = (Collection<EmbeddedContainer>) q.execute("100");
			Assert.assertEquals(2, c.size());
			Iterator<EmbeddedContainer> it = c.iterator();
			assertEmbeddedContainerCorrect("1002.", it.next());
			assertEmbeddedContainerCorrect("1001.", it.next());
		}
	}
}
