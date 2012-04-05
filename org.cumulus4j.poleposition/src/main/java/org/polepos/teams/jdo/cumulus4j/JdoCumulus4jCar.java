package org.polepos.teams.jdo.cumulus4j;

import java.util.HashMap;
import java.util.Map;

import org.polepos.framework.CarMotorFailureException;
import org.polepos.framework.Team;
import org.polepos.teams.jdbc.Jdbc;
import org.polepos.teams.jdo.Jdo;
import org.polepos.teams.jdo.JdoCar;

public class JdoCumulus4jCar extends JdoCar {

//    private transient PersistenceManagerFactory _persistenceManagerFactory;

//    private final String              mDbName;
//    private final String              mName;

	private JdoCumulus4jSettings cumulus4jSettings;

    protected JdoCumulus4jCar(Team team, String name, String dbName, String color) throws CarMotorFailureException {
    	super(team, name, dbName, color, false);

//        mName = name;
//        mDbName = dbName;
//
//        _website = JdoCumulus4j.settings().getWebsite(name);
//        _description = JdoCumulus4j.settings().getDescription(name);

    	cumulus4jSettings = new JdoCumulus4jSettings("cumulus4j.properties");

//        initialize();
    }

    private boolean isSQL() {
        return mDbName != null;
    }

    @Override
    public Map<String, String> getPersistenceEngineProperties() {
    	Map<String, String> properties = new HashMap<String, String>(jdoImplSettings.getFilteredProperties(mDbName));
    	properties.put("datanucleus.storeManagerType", "cumulus4j");
    	properties.putAll((Map) ((JdoCumulus4jTeam)team()).getRuntimeProperties());
    	return properties;
    }

//    @Override
//    protected PersistenceManagerFactory createPersistenceManagerFactory() {
//    	Map<String, String> properties = new HashMap<String, String>(jdoImplSettings.getFilteredProperties(mDbName));
////    	properties.putAll(cumulus4jSettings.getProperties(mDbName));
//    	properties.put("datanucleus.storeManagerType", "cumulus4j");
//    	properties.putAll((Map) ((JdoCumulus4jTeam)team()).getRuntimeProperties());
//		return JDOHelper.getPersistenceManagerFactory(properties, JDOHelper.class.getClassLoader());
//    }

//    private void initialize() {
//
//        Properties properties = new Properties();
//
//        properties.setProperty("javax.jdo.PersistenceManagerFactoryClass", JdoCumulus4j.settings()
//            .getFactory(mName));
//
//        // properties.setProperty("javax.jdo.option.NontransactionalRead", "true");
//
//        properties.setProperty("javax.jdo.option.Multithreaded", "true");
//        properties.setProperty("javax.jdo.option.Optimistic", "true");
//
//        // Versant VODJDO specific settings
//        properties.setProperty("versant.metadata.0", "org/polepos/teams/jdo/data/vod.jdo");
//
//        properties.setProperty("versant.allowPmfCloseWithPmHavingOpenTx","true");
//        properties.setProperty("versant.vdsSchemaEvolve","true");
//
//        properties.setProperty("versant.hyperdrive", "true");
//        properties.setProperty("versant.remoteAccess", "false");
//
//        // Turning this on can make the Concurrency tests crash.
//        // Versant reports this is fixed.
//        // TODO: Test again against the latest VOD release
////        properties.setProperty("versant.l2CacheEnabled", "false");
////
////        // Reduces RPC calls for VOD for optimistic read from 3 to 1
////        properties.setProperty("versant.retainConnectionInOptTx", "true");
////
////        properties.setProperty("versant.l2CacheMaxObjects", "5000000");
////        properties.setProperty("versant.l2QueryCacheEnabled", "true");
////        properties.setProperty("versant.logDownloader", "none");
////        properties.setProperty("versant.logging.logEvents", "none");
////        properties.setProperty("versant.metricSnapshotIntervalMs", "1000000000");
////        properties.setProperty("versant.metricStoreCapacity", "0");
////        properties.setProperty("versant.vdsNamingPolicy", "none");
////
////
////
////
////        properties.setProperty("versant.remoteMaxActive", "30");
////        properties.setProperty("versant.maxActive", "30");
//
//        if (isSQL()) {
//            try {
//                Class.forName(Jdbc.settings().getDriverClass(mDbName)).newInstance();
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//            properties.setProperty("javax.jdo.option.ConnectionDriverName", Jdbc.settings()
//                .getDriverClass(mDbName));
//            String connectUrl = Jdbc.settings().getConnectUrl(mDbName);
//
//			properties.setProperty("javax.jdo.option.ConnectionURL", connectUrl);
//
//            String user = Jdbc.settings().getUsername(mDbName);
//            if (user != null) {
//                properties.setProperty("javax.jdo.option.ConnectionUserName", user);
//            }
//
//            String password = Jdbc.settings().getPassword(mDbName);
//            if (password != null) {
//                properties.setProperty("javax.jdo.option.ConnectionPassword", password);
//            }
//        } else {
//
//            properties.setProperty("javax.jdo.option.ConnectionURL", JdoCumulus4j.settings().getURL(mName));
//
//            String user = JdoCumulus4j.settings().getUsername(mName);
//            if (user != null) {
//                properties.setProperty("javax.jdo.option.ConnectionUserName", user);
//            }
//
//            String password = JdoCumulus4j.settings().getPassword(mName);
//            if (password != null) {
//                properties.setProperty("javax.jdo.option.ConnectionPassword", password);
//            }
//        }
//
//        properties.setProperty("datanucleus.autoCreateSchema", "true");
//
////        properties.setProperty("datanucleus.validateTables", "false");
////        properties.setProperty("datanucleus.validateConstraints", "false");
////        properties.setProperty("datanucleus.metadata.validate", "false");
//
//        properties.setProperty("datanucleus.connectionPool.maxIdle", "15");
//        properties.setProperty("datanucleus.connectionPool.minIdle", "5");
//        properties.setProperty("datanucleus.connectionPool.maxActive", "30");
//
//        properties.setProperty("datanucleus.autoCreateConstraints", "false");
////        properties.setProperty("datanucleus.validateColumns", "false");
//
//
//        properties.setProperty("datanucleus.connectionPoolingType", "DBCP");
//
//		properties.setProperty("datanucleus.persistenceByReachabilityAtCommit", "false");
//		properties.setProperty("datanucleus.manageRelationships", "false");
//
////###################################Cumulus4j#####################################################################
//		properties.setProperty("datanucleus.storeManagerType", "cumulus4j");
//
//	    if(Settings.CRYPTO_MANAGER_ID != null && Settings.CRYPTO_SESSION_ID != null){
//	    	properties.setProperty("cumulus4j.cryptoManagerID", Settings.CRYPTO_MANAGER_ID);
//	    	properties.setProperty("cumulus4j.cryptoSessionID", Settings.CRYPTO_SESSION_ID);
//
//	    	properties.setProperty("javax.jdo.PersistenceManagerFactoryClass", "org.datanucleus.api.jdo.JDOPersistenceManagerFactory");
////			properties.setProperty("javax.jdo.option.ConnectionDriverName", "org.apache.derby.jdbc.EmbeddedDriver");
////			properties.setProperty("javax.jdo.option.ConnectionURL", "jdbc:derby:${java.io.tmpdir}/derby/cumulus4j;create=true");
//	    	properties.setProperty("javax.jdo.option.ConnectionDriverName", "org.hsqldb.jdbcDriver");
//	    	properties.setProperty("javax.jdo.option.ConnectionURL", "jdbc:hsqldb:mem:nucleus1;create=true");
//			properties.setProperty("javax.jdo.option.ServerTimeZoneID", "UTC");
////			properties.setProperty("javax.jdo.option.ConnectionUserName", "root");
//			properties.setProperty("javax.jdo.option.ConnectionUserName", "sa");
//			properties.setProperty("javax.jdo.option.ConnectionPassword", "");
//
////			properties.setProperty("cumulus4j.cryptoSessionExpiryAge", "10000");
////			properties.setProperty("cumulus4j.cryptoSessionExpiryTimer.enabled", "true");
////			properties.setProperty("cumulus4j.cryptoSessionExpiryTimer.period", "10000");
////
////			properties.setProperty("cumulus4j.CryptoCache.cleanupTimer.enabled", "false");
////			properties.setProperty("cumulus4j.CryptoCache.cleanupTimer.period", "10000");
////			properties.setProperty("cumulus4j.CryptoCache.entryExpiryAge", "10000");
//	    }
//	    else{
//	        properties.setProperty("cumulus4j.cryptoManagerID", "dummy");
//	        	properties.setProperty("cumulus4j.cryptoSessionID", "dummy");
//	    }
////##################################################################################################################
//
//        _persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(properties, JDOHelper.class.getClassLoader());
//    }

//    @Override
//	public PersistenceManager getPersistenceManager() {
//
//    	return _persistenceManagerFactory.getPersistenceManager();
//
////        PersistenceManager persistenceManager = _persistenceManagerFactory.getPersistenceManager();
////
////        if(! "hsqldb".equals(mDbName)){
////        	return persistenceManager;
////        }
////
////        JDOConnection dataStoreConnection = persistenceManager.getDataStoreConnection();
//////        Connection connection = (Connection) dataStoreConnection.getNativeConnection();
////        Connection connection = (Connection)((PersistenceManagerConnection) dataStoreConnection.getNativeConnection()).getIndexPM().getDataStoreConnection();
////
////        JdbcCar.hsqlDbWriteDelayToZero(connection);
////        try {
////
////        	// Closing the connection here really feels strange, but otherwise
////        	// Datanucleus hangs, probably because it runs out of JDBC connections.
////
////			connection.close();
////
////		} catch (SQLException e) {
////			e.printStackTrace();
////		}
////		return persistenceManager;
//    }

    @Override
    public String name() {

        if(isSQL()){
            return Jdo.settings().getName(mName) + "/" +Jdbc.settings().getName(mDbName)+"-"+Jdbc.settings().getVersion(mDbName);
        }
        return Jdo.settings().getVendor(mName) + "/" + Jdo.settings().getName(mName)+"-" + Jdo.settings().getVersion(mName);

    }

}