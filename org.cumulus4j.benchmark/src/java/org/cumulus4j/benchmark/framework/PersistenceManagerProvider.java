package org.cumulus4j.benchmark.framework;

import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.cumulus4j.store.test.framework.TestUtil;

/**
 *
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
public class PersistenceManagerProvider {

	private PersistenceManagerFactory pmf;

	private static PersistenceManagerProvider sharedInstance = null;

	public static PersistenceManagerProvider sharedInstance(){
		if(sharedInstance == null){
			synchronized (PersistenceManagerProvider.class) {
				if(sharedInstance == null)
					sharedInstance = new PersistenceManagerProvider();
			}
		}

		return sharedInstance;
	}

	private PersistenceManagerProvider(){}

	private synchronized PersistenceManagerFactory getPersistenceManagerFactory()
	{
		if (pmf == null) {
			try {
				CleanupUtil.dropAllTables();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			Properties props = TestUtil.loadProperties("cumulus4j-test-datanucleus.properties");

//			Map<String, String> properties = PropertyHandler.nextConfiguration();
//			for(String key : properties.keySet()){
//				props.setProperty(key, properties.get(key));
//			}

			pmf = JDOHelper.getPersistenceManagerFactory(props);
		}

		return pmf;
	}

	public PersistenceManager getPersistenceManager(String cryptoManagerID, String cryptoSessionID)
	{
		if (cryptoManagerID == null)
			throw new IllegalArgumentException("cryptoManagerID == null");

		if (cryptoSessionID == null)
			throw new IllegalArgumentException("cryptoSessionID == null");

		PersistenceManager pm = getPersistenceManagerFactory().getPersistenceManager();
		pm.setProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID, cryptoManagerID);
		pm.setProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID, cryptoSessionID);

		return pm;
	}

	public void nextConfiguration(){
		pmf = null;
	}
}
