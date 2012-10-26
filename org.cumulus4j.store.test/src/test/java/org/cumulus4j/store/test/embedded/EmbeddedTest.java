package org.cumulus4j.store.test.embedded;

import java.util.List;

import javax.jdo.Query;

import junit.framework.Assert;

import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class EmbeddedTest extends AbstractJDOTransactionalTestClearingDatabase {

	private static final Logger logger = LoggerFactory.getLogger(EmbeddedTest.class);

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

		//setting up the subobject and the main object of class B and inserting the main object
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
		D query_d = dList.get(0);
		logger.info("Found the object, its ID is: " + query_d.getClass_d_id());
		Assert.assertNotNull(query_d.getInstance_of_e());
		logger.info("Found the embedded object, its information is is: " + query_d.getInstance_of_e().getInformation());
		Assert.assertEquals("some information in the Class E DBO", query_d.getInstance_of_e().getInformation());
		logger.info("end: insertObjectWithEmbeddedOnlySubobject");

	}

}
