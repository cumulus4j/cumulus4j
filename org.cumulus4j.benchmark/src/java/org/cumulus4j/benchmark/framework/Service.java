package org.cumulus4j.benchmark.framework;

import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.cumulus4j.store.test.framework.TestUtil;

public class Service {
	
	protected final int objectCount;
	
	private static PersistenceManagerFactory pmf;
	
	public Service(){

		Properties benchmarkProps = TestUtil.loadProperties("benchmark.properties");
		objectCount = Integer.parseInt(benchmarkProps.getProperty("cumulus4j.benchmark.objectCount"));
	}
	
	private static synchronized PersistenceManagerFactory getPersistenceManagerFactory()
	{
		if (pmf == null) {
			try {
				CleanupUtil.dropAllTables();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			pmf = JDOHelper.getPersistenceManagerFactory(TestUtil.loadProperties("cumulus4j-test-datanucleus.properties"));
		}

		return pmf;
	}
	
	protected PersistenceManager getPersistenceManager(String cryptoManagerID, String cryptoSessionID)
	{
		if (cryptoManagerID == null)
			throw new IllegalArgumentException("cryptoManagerID == null");

		if (cryptoSessionID == null)
			throw new IllegalArgumentException("cryptoSessionID == null");

		// We enforce a fresh start every time, because we execute this now with different key-servers / embedded key-stores:
//		if (pmf != null) {
//			pmf.close();
//			pmf = null;
//		}
		
		PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, cryptoManagerID);
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, cryptoSessionID);
		return pm;
	}

}
