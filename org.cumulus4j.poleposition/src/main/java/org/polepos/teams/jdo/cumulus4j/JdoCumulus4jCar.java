package org.polepos.teams.jdo.cumulus4j;

import java.util.HashMap;
import java.util.Map;

import org.polepos.framework.CarMotorFailureException;
import org.polepos.framework.Team;
import org.polepos.teams.jdo.JdoCar;

public class JdoCumulus4jCar extends JdoCar {

//	private JdoCumulus4jSettings cumulus4jSettings;

    protected JdoCumulus4jCar(Team team, String name, String dbName, String color) throws CarMotorFailureException {
    	super(team, name, dbName, color, false);

//    	cumulus4jSettings = new JdoCumulus4jSettings("cumulus4j.properties");
    }

    @Override
    public Map<String, String> getPersistenceEngineProperties() {
    	Map<String, String> properties = new HashMap<String, String>(jdoImplSettings.getFilteredProperties(mDbName));
    	properties.put("datanucleus.storeManagerType", "cumulus4j");
    	properties.putAll((Map) ((JdoCumulus4jTeam)team()).getRuntimeProperties());

    	//TODO  move properties to cumulus4j.config file
//    	properties.put("cumulus4j.cryptoSessionExpiryAge", "10000");
//    	properties.put("cumulus4j.cryptoSessionExpiryTimer.enabled", "true");
//    	properties.put("cumulus4j.cryptoSessionExpiryTimer.period", "10000");
//    	properties.put("cumulus4j.CryptoCache.cleanupTimer.enabled","false");
//    	properties.put("cumulus4j.CryptoCache.cleanupTimer.period", "10000");
//    	properties.put("cumulus4j.CryptoCache.entryExpiryAge", "10000");

//    	properties.putAll(cumulus4jSettings.getProperties(mDbName));

    	return properties;
    }

}