package org.cumulus4j.store.test.embedded.onetomany;

import java.util.Collection;
import java.util.Iterator;

import javax.jdo.Query;

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

	protected Embedded1ToNListContainer createContainer(String containerPrefix, String elementPrefix, int index) {
		Embedded1ToNListContainer container = new Embedded1ToNListContainer();
		container.setName(containerPrefix + "container" + index);

		Embedded1ToNElement element00 = new Embedded1ToNElement();
		element00.setName(elementPrefix + "element" + index + "0");
		container.getElements().add(element00);

		Embedded1ToNElement element01 = new Embedded1ToNElement();
		element01.setName(elementPrefix + "element" + index + "1");
		container.getElements().add(element01);

		Embedded1ToNElement element02 = new Embedded1ToNElement();
		element02.setName(elementPrefix + "element" + index + "2");
		container.getElements().add(element02);

		return container;
	}

	@Test
	public void writeAndRead() {
		// BEGIN populate meta-data in separate tx for debugging reasons
		pm.getExtent(Embedded1ToNListContainer.class);
		commitAndBeginNewTransaction();
		// END populate meta-data in separate tx for debugging reasons

		{
			Embedded1ToNListContainer container = createContainer("", "", 0);
			pm.makePersistent(container);
		}

		commitAndBeginNewTransaction();

		Iterator<Embedded1ToNListContainer> iterator = pm.getExtent(Embedded1ToNListContainer.class).iterator();
		Assert.assertTrue("No Embedded1ToNListContainer found in database!", iterator.hasNext());
		Embedded1ToNListContainer container = iterator.next();
		Assert.assertFalse("More than one Embedded1ToNListContainer found in database!", iterator.hasNext());

		Assert.assertTrue("non-embedded Embedded1ToNElement found in database!", ((Collection<?>)pm.newQuery(Embedded1ToNElement.class).execute()).isEmpty());

		Assert.assertEquals("container0", container.getName());
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

	private void createQueryTestData() {
		pm.makePersistent(createContainer("AAA.", "Otto.", 0));
		pm.makePersistent(createContainer("AAA.", "Otto.", 1));
		pm.makePersistent(createContainer("AAA.", "Otto.", 2));

		pm.makePersistent(createContainer("BBB.", "Emil.", 0));
		pm.makePersistent(createContainer("BBB.", "Emil.", 1));
		pm.makePersistent(createContainer("BBB.", "Emil.", 2));
		pm.makePersistent(createContainer("BBB.", "Emil.", 3));

		pm.makePersistent(createContainer("CCC.", "Otto.", 0));
		pm.makePersistent(createContainer("CCC.", "Otto.", 1));

		pm.makePersistent(createContainer("DDD.", "Emil.", 0));
		pm.makePersistent(createContainer("DDD.", "Emil.", 1));
		pm.makePersistent(createContainer("DDD.", "Emil.", 2));
		pm.makePersistent(createContainer("DDD.", "Emil.", 3));
	}

	@Test
	public void queryElementNameEquals_containsBeforeEquals() {
		createQueryTestData();

		Query q = pm.newQuery(Embedded1ToNListContainer.class);
		q.declareVariables(Embedded1ToNElement.class.getName() + " element;");
		q.setFilter("this.elements.contains(element) && element.name == :name");
		@SuppressWarnings("unchecked")
		Collection<Embedded1ToNListContainer> c = (Collection<Embedded1ToNListContainer>) q.execute("Otto.element10");
		logContainers(c);
		Assert.assertEquals(2, c.size());
	}

	@Test
	public void queryElementNameEquals_containsAfterEquals() {
		createQueryTestData();

		Query q = pm.newQuery(Embedded1ToNListContainer.class);
		q.declareVariables(Embedded1ToNElement.class.getName() + " element;");
		q.setFilter("element.name == :name && this.elements.contains(element)");
		@SuppressWarnings("unchecked")
		Collection<Embedded1ToNListContainer> c = (Collection<Embedded1ToNListContainer>) q.execute("Otto.element10");
		logContainers(c);
		Assert.assertEquals(2, c.size());
	}

	@Test
	public void queryElementNameIndexOf() {
		createQueryTestData();

		Query q = pm.newQuery(Embedded1ToNListContainer.class);
		q.declareVariables(Embedded1ToNElement.class.getName() + " element;");
		q.setFilter("this.elements.contains(element) && element.name.indexOf(:namePart) >= 0");
		@SuppressWarnings("unchecked")
		Collection<Embedded1ToNListContainer> c = (Collection<Embedded1ToNListContainer>) q.execute("element3");
		logContainers(c);
		Assert.assertEquals(2, c.size());
	}

	private void logContainers(Collection<Embedded1ToNListContainer> c) {
		for (Embedded1ToNListContainer container : c) {
			System.out.println(" * container.name=" + container.getName());
			for (Embedded1ToNElement element : container.getElements()) {
				System.out.println("   * element.name=" + element.getName());
			}
		}
	}
}
