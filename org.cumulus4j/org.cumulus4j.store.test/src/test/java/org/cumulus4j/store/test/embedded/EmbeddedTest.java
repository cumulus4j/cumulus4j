package org.cumulus4j.store.test.embedded;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.jdo.JDOHelper;
import javax.jdo.Query;

import junit.framework.Assert;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Tests for embedded objects.
 *
 * @author mschulze
 */
public class EmbeddedTest extends AbstractJDOTransactionalTestClearingDatabase {

	private static final Logger logger = LoggerFactory.getLogger(EmbeddedTest.class);

	@Before
	public void before() throws Exception {
		final Class<?>[] classesToClear = {
			B.class,
			C.class,
			D.class,
			F.class,
			G.class
		};

		for (Class<?> clazz : classesToClear) {
			for (Iterator<?> it = pm.getExtent(clazz).iterator(); it.hasNext(); ) {
				pm.deletePersistent(it.next());
			}
			pm.flush();
		}
	}

	@Test
	public void insertObjectWithNoEmbeddedSubobjects() throws Exception {

		logger.info("start: insertObjectWithNoEmbeddedSubobjects");

		//setting up and inserting the simple object of class E
		F obj = new F();
		obj.setClass_f_id("SimpleTestDBO");
		obj.setCounter(1);
		G g = new G();
		g.setClass_g_id("instance of G");
		g.setCounter(33);
		obj.setG(g);
		pm.makePersistent(obj);
		pm.flush();

		commitAndBeginNewTransaction();

		//looking whether the object was really inserted
		Query fQuery = pm.newQuery(F.class);
		fQuery.setFilter("class_f_id == 'SimpleTestDBO'");
		@SuppressWarnings("unchecked")
		List<F> fList = (List<F>) fQuery.execute();
		Assert.assertEquals(1, fList.size());
		F f = fList.get(0);
		Assert.assertNotNull(f);
		logger.info("Found the object, its ID is: " + f.getClass_f_id());

		logger.info("end: insertObjectWithNoEmbeddedSubobjects");
		Assert.assertNotNull(f.getG());
		Assert.assertEquals("instance of G", f.getG().getClass_g_id());
		Assert.assertEquals(33, f.getG().getCounter());
	}

	@Test
	public void insertObjectWithEmbeddedSubobject() throws Exception {

		logger.info("start: insertObjectWithEmbeddedSubobject");

		//setting up the objects and inserting the main object of class B

		C c = new C();
		c.setClass_c_id("ClassC_TestDBO");
		//c.setAdditional_info(d);

		B b = new B();
		b.setClass_a_id("ClassB_TestDBO");
		b.setInstance_of_c(c);
		b.setFurtherDetails("no further details");

		pm.makePersistent(b);
		pm.flush();

		commitAndBeginNewTransaction();

		//looking whether the object was really inserted
		Query bQuery = pm.newQuery(B.class);
		bQuery.setFilter("class_a_id == 'ClassB_TestDBO'");
		@SuppressWarnings("unchecked")
		List<B> bList = (List<B>) bQuery.execute();
		Assert.assertEquals(1, bList.size());
		B query_b = bList.get(0);
		logger.info("Found the object, its ID is: " + query_b.getClass_a_id());
		Assert.assertNotNull(query_b.getInstance_of_c());
		logger.info("Found the embedded object, its ID is: " + query_b.getInstance_of_c().getClass_c_id());
		Assert.assertEquals("ClassC_TestDBO", query_b.getInstance_of_c().getClass_c_id());
		logger.info("end: insertObjectWithEmbeddedSubobject");
	}

	@Test
	public void insertObjectWithEmbeddedOnlySubobject() throws Exception {

		logger.info("start: insertObjectWithEmbeddedOnlySubobject");

		//setting up the subobject and the main object of class D and inserting the main object
		E e = new E();
		e.setInformation("some information in the Class E DBO");

		D d = new D();
		d.setClass_d_id("ClassD_TestDBO");
		d.setInstance_of_e(e);

		pm.makePersistent(d);
		pm.flush();

		commitAndBeginNewTransaction();

		//looking whether the object was really inserted
		Query dQuery = pm.newQuery(D.class);
		dQuery.setFilter("class_d_id == 'ClassD_TestDBO'");
		@SuppressWarnings("unchecked")
		List<D> dList = (List<D>) dQuery.execute();
		Assert.assertEquals(1, dList.size());
		D query_d = dList.get(0);
		logger.info("Found the object, its ID is: " + query_d.getClass_d_id());
		Assert.assertNotNull(query_d.getInstance_of_e());
		logger.info("Found the embedded object, its information is is: " + query_d.getInstance_of_e().getInformation());
		Assert.assertEquals("some information in the Class E DBO", query_d.getInstance_of_e().getInformation());
		logger.info("end: insertObjectWithEmbeddedOnlySubobject");
	}

	@Test
	public void assignNonEmbeddedPersistentInstanceAsEmbeddedField() throws Exception {
		String cid_initial = "initial";
		String cid_modified = "modified";
		String bid = "bbb";

		{
			C c = new C();
			// The initial value is written into the embedded instance only, because it is assigned at the moment when
			// we assign c.instance_of_c.
			c.setClass_c_id(cid_initial);
			c = pm.makePersistent(c);
			Object coid = JDOHelper.getObjectId(c);

			B b = new B();
			b.setClass_a_id(bid);
			b.setInstance_of_c(c);
			b = pm.makePersistent(b);

			// The following assignment should only apply to the non-embedded instance.
			// At least this is how DN behaves without C4j.
//			b.getInstance_of_c().setClass_c_id(cid_modified);

			// Since version 3.2 of DataNucleus, it keeps the very same instance in RAM, which is IMHO
			// correct, too. Maybe even more correct than the previous behaviour. However, it means that
			// we now have to flush AND evict the L1 cache to make sure we modify solely the non-embedded
			// instance.
			pm.flush(); pm.evictAll();

			c = (C) pm.getObjectById(coid);
			c.setClass_c_id(cid_modified);
		}

		commitAndBeginNewTransaction();

		{
			Query q = pm.newQuery(C.class);
			q.setFilter("class_c_id == :id");
			@SuppressWarnings("unchecked")
			Collection<C> collection = (Collection<C>) q.execute(cid_modified);

			Assert.assertEquals(1, collection.size());
			C c = collection.iterator().next();
			Assert.assertEquals(cid_modified, c.getClass_c_id());
		}

		{
			Query q = pm.newQuery(B.class);
			q.setFilter("class_a_id == :id");
			@SuppressWarnings("unchecked")
			Collection<B> collection = (Collection<B>) q.execute(bid);
			Assert.assertEquals(1, collection.size());
			B b = collection.iterator().next();
			Assert.assertEquals(bid, b.getClass_a_id());
			Assert.assertNotNull(b.getInstance_of_c());
			Assert.assertEquals(cid_initial, b.getInstance_of_c().getClass_c_id());
		}
	}


	@Test
	public void assignNonEmbeddedNonPersistentInstanceAsEmbeddedField() throws Exception {
		String cid_initial = "initial";
		String cid_modified = "modified";
		String bid = "bbb";

		{
			C c = new C();
			// The initial value is written into the embedded instance when we assign c.instance_of_c.
			c.setClass_c_id(cid_initial);
			// we do NOT persist c on its own!

			B b = new B();
			b.setClass_a_id(bid);
			b.setInstance_of_c(c);
			b = pm.makePersistent(b);

			// The following assignment should apply to the embedded instance, because there is no non-embedded instance.
			// At least this is how DN behaves without C4j.
			b.getInstance_of_c().setClass_c_id(cid_modified);
		}

		commitAndBeginNewTransaction();

		{
			// There should be NO instance of c in the database, because we did not persist it on its own.
			Query q = pm.newQuery(C.class);
			@SuppressWarnings("unchecked")
			Collection<C> collection = (Collection<C>) q.execute(cid_modified);
			Assert.assertTrue(collection.isEmpty());
		}

		{
			Query q = pm.newQuery(B.class);
			q.setFilter("class_a_id == :id");
			@SuppressWarnings("unchecked")
			Collection<B> collection = (Collection<B>) q.execute(bid);
			Assert.assertEquals(1, collection.size());
			B b = collection.iterator().next();
			Assert.assertEquals(bid, b.getClass_a_id());
			Assert.assertNotNull(b.getInstance_of_c());
			Assert.assertEquals(cid_modified, b.getInstance_of_c().getClass_c_id());
		}
	}
}
