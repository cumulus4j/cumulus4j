package org.polepos.teams.jdo.cumulus4j;

import java.util.HashMap;
import java.util.Map;

import org.polepos.Settings;
import org.polepos.framework.CarMotorFailureException;
import org.polepos.framework.Team;
import org.polepos.teams.jdo.JdoCar;

public class JdoCumulus4jCar extends JdoCar {

	private static JdoCumulus4jSettings cumulus4jSettings;

	static{
		cumulus4jSettings = new JdoCumulus4jSettings(Settings.CUMULUS4J);
	}

    protected JdoCumulus4jCar(Team team, String name, String dbName, String color) throws CarMotorFailureException {
    	super(team, name, dbName, cumulus4jSettings.get("cumulus4j.color"), false);
    }

    @SuppressWarnings("unchecked")
	@Override
    public Map<String, String> getPersistenceEngineProperties() {
    	Map<String, String> properties = new HashMap<String, String>(jdoImplSettings.getFilteredProperties(mDbName));
    	properties.put("datanucleus.storeManagerType", "cumulus4j");
    	properties.putAll((Map) ((JdoCumulus4jTeam)team()).getRuntimeProperties());

    	properties.putAll(cumulus4jSettings.getProperties(mDbName));

    	return properties;
    }

}