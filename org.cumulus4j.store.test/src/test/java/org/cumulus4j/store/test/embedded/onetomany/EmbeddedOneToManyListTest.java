package org.cumulus4j.store.test.embedded.onetomany;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.Assert;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.Before;
import org.junit.Test;

public class EmbeddedOneToManyListTest extends AbstractJDOTransactionalTestClearingDatabase {

	@Before
	public void before() throws Exception {
		final Class<?>[] classesToClear = {
			Embedded1ToNElement.class,
			Embedded1ToNListContainer.class
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
		pm.getExtent(Embedded1ToNListContainer.class);
		commitAndBeginNewTransaction();
		// END populate meta-data in separate tx for debugging reasons

		{
			Embedded1ToNListContainer container = new Embedded1ToNListContainer();
			container.setName("container00");

			Embedded1ToNElement element00 = new Embedded1ToNElement();
			element00.setName("element00");
			container.getElements().add(element00);

			Embedded1ToNElement element01 = new Embedded1ToNElement();
			element01.setName("element01");
			container.getElements().add(element01);

			Embedded1ToNElement element02 = new Embedded1ToNElement();
			element02.setName("element02");
			container.getElements().add(element02);

			pm.makePersistent(container);
		}

		commitAndBeginNewTransaction();

		Iterator<Embedded1ToNListContainer> iterator = pm.getExtent(Embedded1ToNListContainer.class).iterator();
		Assert.assertTrue("No Embedded1ToNListContainer found in database!", iterator.hasNext());
		Embedded1ToNListContainer container = iterator.next();
		Assert.assertFalse("More than one Embedded1ToNListContainer found in database!", iterator.hasNext());

		Assert.assertTrue("non-embedded Embedded1ToNElement found in database!", ((Collection<?>)pm.newQuery(Embedded1ToNElement.class).execute()).isEmpty());

		Assert.assertEquals("container00", container.getName());
		Assert.assertNotNull(container.getElements());
		Assert.assertEquals(3, container.getElements().size());
		Iterator<Embedded1ToNElement> itElem = container.getElements().iterator();
		Assert.assertTrue(itElem.hasNext());
		Assert.assertEquals("element00", itElem.next().getName());

		Assert.assertTrue(itElem.hasNext());
		Assert.assertEquals("element01", itElem.next().getName());

		Assert.assertTrue(itElem.hasNext());
		Assert.assertEquals("element02", itElem.next().getName());

		Assert.assertFalse(itElem.hasNext());
	}

}
