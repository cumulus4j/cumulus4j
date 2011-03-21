package org.cumulus4j.core.resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.jdo.PersistenceManagerFactory;

/**
 * Helper class to load resources.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class ResourceHelper
{
	/**
	 * File name of the backend-properties.
	 * @see #getCumulus4jBackendProperties()
	 */
	protected static final String CUMULUS4J_BACKEND_PROPERTIES = "cumulus4j-backend.properties";

	/**
	 * <p>
	 * Open an {@link InputStream} reading the file {@value #CUMULUS4J_BACKEND_PROPERTIES}.
	 * </p>
	 * <p>
	 * <b>Important:</b> You must close this <code>InputStream</code>!!!
	 * </p>
	 * @return an {@link InputStream} (never <code>null</code>).
	 * @see #getCumulus4jBackendProperties()
	 */
	protected static InputStream openCumulus4jBackendProperties()
	{
		InputStream in = ResourceHelper.class.getResourceAsStream(CUMULUS4J_BACKEND_PROPERTIES);
		if (in == null)
			throw new IllegalStateException("Resource does not exist (or cannot be openend): " + CUMULUS4J_BACKEND_PROPERTIES);

		return in;
	}

	/**
	 * <p>
	 * Get the backend-properties (from file {@value #CUMULUS4J_BACKEND_PROPERTIES}).
	 * </p>
	 * <p>
	 * The backend-properties are loaded from the file {@value #CUMULUS4J_BACKEND_PROPERTIES}
	 * and used for the configuration of the backend-{@link PersistenceManagerFactory}. This file only
	 * contains connection-independent settings (i.e. things that should never change).
	 * The connection-dependent settings are copied from the frontend-PMF's configuration.
	 * </p>
	 * <p>
	 * Additionally, all properties in the frontend-PMF's configuration starting with
	 * "cumulus4j.datanucleus." or "cumulus4j.javax." are forwarded to the backend-PMF
	 * (without the "cumulus4j."-prefix, of course) <b>overriding</b> the default settings.
	 * </p>
	 * @return a {@link Map} containing all settings from the backend-properties; never <code>null</code>.
	 */
	public static Map<String, Object> getCumulus4jBackendProperties()
	{
		Properties properties = new Properties();
		try {
			InputStream in = openCumulus4jBackendProperties();
			properties.load(in);
			in.close();
		} catch (IOException e) { // should never happen as it is a resource => rethrow as RuntimeException
			throw new RuntimeException(e);
		}

		Map<String, Object> result = new HashMap<String, Object>(properties.size());
		for (Map.Entry<?, ?> me : properties.entrySet())
			result.put(String.valueOf(me.getKey()).toLowerCase(Locale.ENGLISH), String.valueOf(me.getValue()));

		return result;
	}
}
