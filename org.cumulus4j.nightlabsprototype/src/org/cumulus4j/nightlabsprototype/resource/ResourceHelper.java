package org.cumulus4j.nightlabsprototype.resource;

import java.io.InputStream;

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
}