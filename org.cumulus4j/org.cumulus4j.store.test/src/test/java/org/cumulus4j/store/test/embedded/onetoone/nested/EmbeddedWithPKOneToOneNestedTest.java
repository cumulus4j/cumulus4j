package org.cumulus4j.store.test.embedded.onetoone.nested;

import java.util.Collection;
import java.util.Iterator;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EmbeddedWithPKOneToOneNestedTest extends AbstractJDOTransactionalTestClearingDatabase {

	@Before
	public void before() throws Exception {
		final Class<?>[] classesToClear = {
				EmbeddedWithPKSub.class,
				EmbeddedWithPK.class,
				EmbeddedWithPKContainer.class
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
		pm.getExtent(EmbeddedWithPKContainer.class);
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
		EmbeddedWithPKContainer container = createEmbeddedContainer("");
		pm.makePersistent(container);

		commitAndBeginNewTransaction();

		Iterator<EmbeddedWithPKContainer> iterator = pm.getExtent(EmbeddedWithPKContainer.class).iterator();
		Assert.assertTrue("No EmbeddedWithPKContainer found in database!", iterator.hasNext());
		EmbeddedWithPKContainer embeddedContainer = iterator.next();
		Assert.assertFalse("More than one EmbeddedWithPKContainer found in database!", iterator.hasNext());

		// TODO check, why the hell the pm.getExtent(...) code below finds instances (is that a bug in DN?). but later...
		Assert.assertTrue("non-embedded EmbeddedWithPK found in database!", ((Collection<?>)pm.newQuery(EmbeddedWithPK.class).execute()).isEmpty());
		Assert.assertTrue("non-embedded EmbeddedWithPKSub found in database!", ((Collection<?>)pm.newQuery(EmbeddedWithPKSub.class).execute()).isEmpty());

		assertEmbeddedContainerCorrect("", embeddedContainer);
	}

	private EmbeddedWithPKContainer createEmbeddedContainer(String namePrefix) {
		EmbeddedWithPKContainer container = new EmbeddedWithPKContainer();
		container.setName(namePrefix + "container");

		EmbeddedWithPK embeddedWithPK = createEmbeddedWithPK(namePrefix);
		container.setEmbeddedWithPK(embeddedWithPK);

		return container;
	}

	private EmbeddedWithPK createEmbeddedWithPK(String namePrefix) {
		EmbeddedWithPK embeddedWithPK = new EmbeddedWithPK();
		embeddedWithPK.setName(namePrefix + "embeddedWithPK");

		embeddedWithPK.setId1(666l); // MUST specify this! doesn't work without it in both RDBMS (mysql) and C4j

		EmbeddedWithPKSub embeddedWithPKSub = new EmbeddedWithPKSub();
		embeddedWithPK.setEmbeddedWithPKSub(embeddedWithPKSub);
		embeddedWithPKSub.setName(namePrefix + "embeddedWithPKSub");

		embeddedWithPKSub.setId2(668l); // MUST specify this! doesn't work without it in both RDBMS (mysql) and C4j

		return embeddedWithPK;
	}

	private void assertEmbeddedContainerCorrect(String namePrefix, EmbeddedWithPKContainer embeddedContainer) {
		Assert.assertNotNull(embeddedContainer.getEmbeddedWithPK());
		Assert.assertNotNull(embeddedContainer.getEmbeddedWithPK().getEmbeddedWithPKSub());

		Assert.assertEquals(namePrefix + "container", embeddedContainer.getName());
		Assert.assertEquals(namePrefix + "embeddedWithPK", embeddedContainer.getEmbeddedWithPK().getName());
		Assert.assertEquals(namePrefix + "embeddedWithPKSub", embeddedContainer.getEmbeddedWithPK().getEmbeddedWithPKSub().getName());
	}

}
