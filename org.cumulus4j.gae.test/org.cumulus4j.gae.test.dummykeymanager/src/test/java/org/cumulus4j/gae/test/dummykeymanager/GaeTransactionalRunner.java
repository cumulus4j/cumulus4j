package org.cumulus4j.gae.test.dummykeymanager;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.store.test.account.Account;
import org.cumulus4j.store.test.account.LocalAccountantDelegate;
import org.cumulus4j.store.test.account.SummaryAccount;
import org.cumulus4j.store.test.framework.JDOTransactionalRunner;
import org.junit.runners.model.InitializationError;

import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;

public class GaeTransactionalRunner extends JDOTransactionalRunner {

	private static final LocalServiceTestHelper helper =
			new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig().setApplyAllHighRepJobPolicy())
				.setEnvIsLoggedIn(true)
				.setEnvAuthDomain("localhost")
				.setEnvEmail("test@localhost");

	private static boolean helperUp;

	public GaeTransactionalRunner(Class<?> testClass) throws InitializationError {
		super(testClass);
	}

	@Override
	public PersistenceManagerFactory createPersistenceManagerFactory() {
		if (helperUp)
			helper.tearDown();

		helper.setUp();
		helperUp = true;

		Map<String, String> props = new HashMap<String, String>();
	    props.put("datanucleus.appengine.BigDecimalsEncoding", "String");
//	    ds = DatastoreServiceFactory.getDatastoreService();
	    PersistenceManagerFactory pmf = JDOHelper.getPersistenceManagerFactory(props, "transactions-optional");

	    PersistenceManager pm = pmf.getPersistenceManager();
	    setEncryptionCoordinates(pm);
	    pm.getExtent(SummaryAccount.class);
	    pm.getExtent(LocalAccountantDelegate.class);
	    pm.close();

	    pm = pmf.getPersistenceManager();
	    setEncryptionCoordinates(pm);
	    pm.getExtent(LocalAccountantDelegate.class);
	    pm.getExtent(Account.class);
	    pm.getExtent(SummaryAccount.class);
	    pm.close();

		return pmf;
	}

}
