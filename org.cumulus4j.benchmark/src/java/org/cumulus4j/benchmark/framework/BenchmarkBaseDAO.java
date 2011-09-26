package org.cumulus4j.benchmark.framework;

import java.util.Map;
import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.benchmark.report.ConsoleReportTracker;
import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.test.framework.CleanupUtil;
import org.cumulus4j.store.test.framework.TestUtil;

/**
 *
 * @author Jan Mortensen - jmortensen at nightlabs dot de
 *
 */
public abstract class BenchmarkBaseDAO {

	private PersistenceManagerFactory pmf;

	private Map<String, String> currentConfiguration;

	private IReportTracker<String> tracker;

	public BenchmarkBaseDAO(){
		tracker = new ConsoleReportTracker();
		currentConfiguration = PropertyHandler.nextConfiguration();
		tracker.newStory(currentConfiguration.toString());
	}

	private synchronized PersistenceManagerFactory getPersistenceManagerFactory()
	{
		if (pmf == null) {
			try {
				CleanupUtil.dropAllTables();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			Properties props = TestUtil.loadProperties("cumulus4j-test-datanucleus.properties");

//			currentConfiguration = PropertyHandler.nextConfiguration();
			if(currentConfiguration != null){
				props.put("datanucleus.storeManagerType", "cumulus4j");
				for(String key : currentConfiguration.keySet()){
					props.setProperty(key, currentConfiguration.get(key));
				}
			}

			pmf = JDOHelper.getPersistenceManagerFactory(props);
		}

		return pmf;
	}

	protected PersistenceManager getPersistenceManager(String cryptoManagerID, String cryptoSessionID)
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

	/**
	 * Getter method for the current configuration of the DAO class.
	 * This means the currently selected encryption and mac algorithm.
	 *
	 * @return The current configuration of the DAO class.
	 */
	public Map<String, String> currentConfiguration(){
		return currentConfiguration;
	}

	public void nextConfiguration(){
		currentConfiguration = PropertyHandler.nextConfiguration();
		tracker.newStory(currentConfiguration == null ? "" : currentConfiguration.toString());
		pmf = null;
	}

	public IReportTracker<String> getTracker(){
		return tracker;
	}
}
