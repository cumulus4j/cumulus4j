package org.cumulus4j.store.test.embedded;

import java.util.List;
import java.util.UUID;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.test.framework.AbstractJDOTransactionalTestClearingDatabase;
import org.cumulus4j.store.test.movie.Movie;
import org.cumulus4j.store.test.movie.Rating;
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
        pm.makePersistent(obj);
        pm.flush();
        
        commitAndBeginNewTransaction();
        
        //looking whether the object was really inserted
  		Query fQuery = pm.newQuery(F.class);
  		fQuery.setFilter("class_f_id == 'SimpleTestDBO'");
  		List<F> fList = (List<F>) fQuery.execute();
  		F f = fList.get(0);
  		logger.info("Found the object, its ID is: " + f.getClass_f_id());
 
  		commitAndBeginNewTransaction();
  		
        logger.info("end: insertObjectWithNoEmbeddedSubobjects");
        
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
  		List<B> bList = (List<B>) bQuery.execute();
  		B query_b = bList.get(0);
  		logger.info("Found the object, its ID is: " + query_b.getClass_a_id());
  		logger.info("Found the embedded object, its ID is: " + query_b.getInstance_of_c().getClass_c_id());
 
  		commitAndBeginNewTransaction();
		
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
        
        
        pm.makePersistent(d);
        pm.flush();
            
		commitAndBeginNewTransaction();
		
        //looking whether the object was really inserted
		Query dQuery = pm.newQuery(D.class);
  		dQuery.setFilter("class_d_id == 'ClassD_TestDBO'");
  		List<D> dList = (List<D>) dQuery.execute();
  		D query_d = dList.get(0);
  		logger.info("Found the object, its ID is: " + query_d.getClass_d_id());
  		logger.info("Found the embedded object, its information is is: " + query_d.getInstance_of_e().getInformation());
 
  		commitAndBeginNewTransaction();
		
		logger.info("end: insertObjectWithEmbeddedOnlySubobject");
        
    }
	
}
