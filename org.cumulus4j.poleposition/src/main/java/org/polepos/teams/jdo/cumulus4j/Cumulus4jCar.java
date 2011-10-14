package org.polepos.teams.jdo.cumulus4j;

import java.util.Properties;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.polepos.framework.Car;
import org.polepos.framework.CarMotorFailureException;
import org.polepos.framework.Team;
import org.polepos.teams.jdbc.Jdbc;

public class Cumulus4jCar extends Car{

    private transient PersistenceManagerFactory _persistenceManagerFactory;

    private final String              mDbName;
    private final String              mName;

    Cumulus4jCar(Team team, String name, String dbName, String color) throws CarMotorFailureException {
    	super(team, color);

        mName = name;
        mDbName = dbName;

        _website = Cumulus4j.settings().getWebsite(name);
        _description = Cumulus4j.settings().getDescription(name);

        initialize();

    }

    private boolean isSQL() {
        return mDbName != null;
    }

    private void initialize() {

        Properties properties = new Properties();

        properties.setProperty("javax.jdo.PersistenceManagerFactoryClass", Cumulus4j.settings()
            .getFactory(mName));

        // properties.setProperty("javax.jdo.option.NontransactionalRead", "true");

        properties.setProperty("javax.jdo.option.Multithreaded", "true");
        properties.setProperty("javax.jdo.option.Optimistic", "true");

        // Versant VODJDO specific settings
        properties.setProperty("versant.metadata.0", "org/polepos/teams/jdo/data/vod.jdo");

        properties.setProperty("versant.allowPmfCloseWithPmHavingOpenTx","true");
        properties.setProperty("versant.vdsSchemaEvolve","true");

        properties.setProperty("versant.hyperdrive", "true");
        properties.setProperty("versant.remoteAccess", "false");

        // Turning this on can make the Concurrency tests crash.
        // Versant reports this is fixed.
        // TODO: Test again against the latest VOD release
//        properties.setProperty("versant.l2CacheEnabled", "false");
//
//        // Reduces RPC calls for VOD for optimistic read from 3 to 1
//        properties.setProperty("versant.retainConnectionInOptTx", "true");
//
//        properties.setProperty("versant.l2CacheMaxObjects", "5000000");
//        properties.setProperty("versant.l2QueryCacheEnabled", "true");
//        properties.setProperty("versant.logDownloader", "none");
//        properties.setProperty("versant.logging.logEvents", "none");
//        properties.setProperty("versant.metricSnapshotIntervalMs", "1000000000");
//        properties.setProperty("versant.metricStoreCapacity", "0");
//        properties.setProperty("versant.vdsNamingPolicy", "none");
//
//
//
//
//        properties.setProperty("versant.remoteMaxActive", "30");
//        properties.setProperty("versant.maxActive", "30");

        if (isSQL()) {
            try {
                Class.forName(Jdbc.settings().getDriverClass(mDbName)).newInstance();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            properties.setProperty("javax.jdo.option.ConnectionDriverName", Jdbc.settings()
                .getDriverClass(mDbName));
            String connectUrl = Jdbc.settings().getConnectUrl(mDbName);

			properties.setProperty("javax.jdo.option.ConnectionURL", connectUrl);

            String user = Jdbc.settings().getUsername(mDbName);
            if (user != null) {
                properties.setProperty("javax.jdo.option.ConnectionUserName", user);
            }

            String password = Jdbc.settings().getPassword(mDbName);
            if (password != null) {
                properties.setProperty("javax.jdo.option.ConnectionPassword", password);
            }
        } else {

            properties.setProperty("javax.jdo.option.ConnectionURL", Cumulus4j.settings().getURL(mName));

            String user = Cumulus4j.settings().getUsername(mName);
            if (user != null) {
                properties.setProperty("javax.jdo.option.ConnectionUserName", user);
            }

            String password = Cumulus4j.settings().getPassword(mName);
            if (password != null) {
                properties.setProperty("javax.jdo.option.ConnectionPassword", password);
            }
        }
//****************CUMULUS4J*******************************************************************
        properties.setProperty("datanucleus.storeManagerType", "cumulus4j");
        properties.setProperty("cumulus4j.cryptoSessionID", "dummy");
        properties.setProperty("cumulus4j.cryptoManagerID", "dummy");

//        properties.setProperty("datanucleus.rdbms.stringDefaultLength", "255");
//        properties.setProperty("datanucleus.query.checkUnusedParameters", "false");
//        properties.setProperty("datanucleus.store.allowReferencesWithNoImplementations", "true");
//        properties.setProperty("datanucleus.cache.level2.type", "none");
//        properties.setProperty("datanucleus.plugin.allowUserBundles", "true");
//        properties.setProperty("datanucleus.persistenceByReachabilityAtCommit", "false");
//        properties.setProperty("cumulus4j.datanucleus.storeManagerType", "rdbms");


//        datanucleus.persistenceByReachabilityAtCommit=false
//
//        #cumulus4j.datanucleus.storeManagerType=rdbms
//
//        datanucleus.rdbms.stringDefaultLength=255
//        #datanucleus.jpa.oneToManyUniFkRelations=true
//
//        datanucleus.query.checkUnusedParameters=false
//
//        datanucleus.store.allowReferencesWithNoImplementations=true
//
//        datanucleus.cache.level2.type=none
//
//
//        # *** cumulus4j ***
//        datanucleus.plugin.allowUserBundles=true

//**********************************************************************************************

        properties.setProperty("datanucleus.autoCreateSchema", "true");

//        properties.setProperty("datanucleus.validateTables", "false");
//        properties.setProperty("datanucleus.validateConstraints", "false");
//        properties.setProperty("datanucleus.metadata.validate", "false");

        properties.setProperty("datanucleus.connectionPool.maxIdle", "15");
        properties.setProperty("datanucleus.connectionPool.minIdle", "5");
        properties.setProperty("datanucleus.connectionPool.maxActive", "30");


        properties.setProperty("datanucleus.autoCreateConstraints", "false");
//        properties.setProperty("datanucleus.validateColumns", "false");


        properties.setProperty("datanucleus.connectionPoolingType", "DBCP");

		properties.setProperty("datanucleus.persistenceByReachabilityAtCommit", "false");
		properties.setProperty("datanucleus.manageRelationships", "false");


        _persistenceManagerFactory = JDOHelper.getPersistenceManagerFactory(properties, JDOHelper.class.getClassLoader());
    }

    public PersistenceManager getPersistenceManager() {

        PersistenceManager persistenceManager = _persistenceManagerFactory.getPersistenceManager();

        if(! "hsqldb".equals(mDbName)){
        	return persistenceManager;
        }


//        JDOConnection dataStoreConnection = persistenceManager.getDataStoreConnection();
//        Connection connection = (Connection) dataStoreConnection.getNativeConnection();
//        Connection connection = (Connection)((PersistenceManagerConnection) dataStoreConnection.getNativeConnection()).getIndexPM().getDataStoreConnection();
//
//        JdbcCar.hsqlDbWriteDelayToZero(connection);
//        try {
//
//        	// Closing the connection here really feels strange, but otherwise
//        	// Datanucleus hangs, probably because it runs out of JDBC connections.
//
//			connection.close();
//
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
		return persistenceManager;
    }

    @Override
    public String name() {

        if(isSQL()){
            return Cumulus4j.settings().getName(mName) + "/" +Jdbc.settings().getName(mDbName)+"-"+Jdbc.settings().getVersion(mDbName);
        }
        return Cumulus4j.settings().getVendor(mName) + "/" + Cumulus4j.settings().getName(mName)+"-" + Cumulus4j.settings().getVersion(mName);

    }

}