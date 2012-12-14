package org.cumulus4j.store.test.embedded.onetomany;

import java.util.Collection;
import java.util.Iterator;

import junit.framework.Assert;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class EmbeddedOneToManyMapTest extends AbstractJDOTransactionalTestClearingDatabase {
	@Before
	public void before() throws Exception {
		final Class<?>[] classesToClear = {
			Embedded1ToNElement.class,
			Embedded1ToNMapContainer.class
		};

		for (Class<?> clazz : classesToClear) {
			for (Iterator<?> it = pm.getExtent(clazz).iterator(); it.hasNext(); ) {
				pm.deletePersistent(it.next());
			}
			pm.flush();
		}
	}

	@Test
	@Ignore("Embedded1ToNMapContainer causes an NPE in DN, if correctly annotated as embeddedValue. Since this is not a C4j bug, we temporarily disable this test.")
	public void writeAndRead() {
		// BEGIN populate meta-data in separate tx for debugging reasons
		pm.getExtent(Embedded1ToNMapContainer.class);
		commitAndBeginNewTransaction();
		// END populate meta-data in separate tx for debugging reasons

		{
			Embedded1ToNMapContainer container = new Embedded1ToNMapContainer();
			container.setName("container00");

			Embedded1ToNElement element00 = new Embedded1ToNElement();
			element00.setName("element00");
			container.getElements().put("x00", element00);

			Embedded1ToNElement element01 = new Embedded1ToNElement();
			element01.setName("element01");
			container.getElements().put("x01", element01);

			Embedded1ToNElement element02 = new Embedded1ToNElement();
			element02.setName("element02");
			container.getElements().put("x02", element02);

			pm.makePersistent(container);
		}

		commitAndBeginNewTransaction();

		Iterator<Embedded1ToNMapContainer> iterator = pm.getExtent(Embedded1ToNMapContainer.class).iterator();
		Assert.assertTrue("No Embedded1ToNListContainer found in database!", iterator.hasNext());
		Embedded1ToNMapContainer container = iterator.next();
		Assert.assertFalse("More than one Embedded1ToNMapContainer found in database!", iterator.hasNext());

		Assert.assertTrue("non-embedded Embedded1ToNElement found in database!", ((Collection<?>)pm.newQuery(Embedded1ToNElement.class).execute()).isEmpty());

		Assert.assertEquals("container00", container.getName());
		Assert.assertNotNull(container.getElements());
		Assert.assertEquals(3, container.getElements().size());
		Embedded1ToNElement element;
		element = container.getElements().get("x00");
		Assert.assertNotNull(element);
		Assert.assertEquals("element00", element.getName());

		element = container.getElements().get("x01");
		Assert.assertNotNull(element);
		Assert.assertEquals("element01", element.getName());

		element = container.getElements().get("x02");
		Assert.assertNotNull(element);
		Assert.assertEquals("element02", element.getName());
	}

}
