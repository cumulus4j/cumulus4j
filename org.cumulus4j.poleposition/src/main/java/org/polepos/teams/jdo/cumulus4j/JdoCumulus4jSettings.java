package org.polepos.teams.jdo.cumulus4j;

import java.util.HashMap;
import java.util.Map;

import org.polepos.framework.PropertiesHandler;

public class JdoCumulus4jSettings extends PropertiesHandler
{

	public JdoCumulus4jSettings(String propertiesname) {
		super(propertiesname);
	}

	private static final String[] variableNames = {
		"cumulus4j.cryptoSessionExpiryAge",
		"cumulus4j.cryptoSessionExpiryTimer.enabled",
		"cumulus4j.cryptoSessionExpiryTimer.period",
		"cumulus4j.CryptoCache.cleanupTimer.enabled",
		"cumulus4j.CryptoCache.cleanupTimer.period",
		"cumulus4j.CryptoCache.entryExpiryAge"
	};

	public Map<String, String> getProperties(String dbName)
	{
		Map<String, String> props = new HashMap<String, String>();
		for (String variableName : variableNames)
			props.put(variableName, nullToEmptyString(get(variableName)));

		return props;
	}

	private String nullToEmptyString(String s) {
		return s == null ? "" : s;
	}

	public String getColor(){

		String color = get("cumulus4j.color");

		if(color != null)
			return color;
		else
			return "0x27518C";
	}

}
