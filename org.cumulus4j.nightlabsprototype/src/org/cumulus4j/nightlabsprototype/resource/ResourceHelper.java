package org.cumulus4j.nightlabsprototype.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class ResourceHelper
{
	private static final String CUMULUS4J_BACKEND_PROPERTIES = "cumulus4j-backend.properties";

	public static InputStream openCumulus4jBackendProperties()
	{
		InputStream in = ResourceHelper.class.getResourceAsStream(CUMULUS4J_BACKEND_PROPERTIES);
		if (in == null)
			throw new IllegalStateException("Resource does not exist (or cannot be openend): " + CUMULUS4J_BACKEND_PROPERTIES);

		return in;
	}

	public static Properties getCumulus4jBackendProperties()
	{
		Properties properties = new Properties();
		try {
			InputStream in = openCumulus4jBackendProperties();
			properties.load(in);
			in.close();
		} catch (IOException e) { // should never happen as it is a resource => rethrow as RuntimeException
			throw new RuntimeException(e);
		}
		return properties;
	}
}
