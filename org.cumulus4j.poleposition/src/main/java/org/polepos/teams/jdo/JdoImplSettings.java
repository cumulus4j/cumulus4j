package org.polepos.teams.jdo;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.nightlabs.util.PropertiesUtil;
import org.polepos.framework.PropertiesHandler;
import org.polepos.teams.jdbc.Jdbc;

public class JdoImplSettings extends PropertiesHandler
{
	public JdoImplSettings(String propertiesname) {
		super(propertiesname);
	}

	private Map<String, Map<String, String>> dbName2filteredProperties = Collections.synchronizedMap(new HashMap<String, Map<String,String>>());

	private static final String[] variableNames = {
		JdoSettings.KEY_CONNECTURL,
		JdoSettings.KEY_DRIVERCLASS,
		JdoSettings.KEY_USER,
		JdoSettings.KEY_PASSWORD
	};

	public Map<String, String> getFilteredProperties(String dbName)
	{
		Map<String, String> filteredProperties = dbName2filteredProperties.get(dbName);

		if (filteredProperties == null) {
			Map<String, String> variables = new HashMap<String, String>();
			for (String variableName : variableNames)
				variables.put(variableName, nullToEmptyString(Jdbc.settings().get(dbName + "." + variableName)));

			filteredProperties = Collections.unmodifiableMap(PropertiesUtil.filterProperties(getProperties(), variables));
			dbName2filteredProperties.put(dbName, filteredProperties);
		}

		return filteredProperties;
	}

	private String nullToEmptyString(String s) {
		return s == null ? "" : s;
	}
}
