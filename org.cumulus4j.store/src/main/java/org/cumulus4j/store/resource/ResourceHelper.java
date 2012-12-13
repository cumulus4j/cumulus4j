/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.resource;

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

	protected static final String CUMULUS4J_PERSISTENCE_PROPERTIES = "cumulus4j-persistence.properties";

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

	protected static InputStream tryOpenCumulus4jPersistenceProperties()
	{
		InputStream in = ResourceHelper.class.getResourceAsStream(CUMULUS4J_PERSISTENCE_PROPERTIES);
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

		return getPropertiesAsMap(properties);
	}

	public static Map<String, Object> getCumulus4jPersistenceProperties()
	{
		Properties properties = new Properties();
		try {
			InputStream in = tryOpenCumulus4jPersistenceProperties();
			if (in != null) {
				properties.load(in);
				in.close();
			}
		} catch (IOException e) { // should never happen as it is a resource => rethrow as RuntimeException
			throw new RuntimeException(e);
		}

		return getPropertiesAsMap(properties);
	}

	protected static Map<String, Object> getPropertiesAsMap(Properties properties) {
		Map<String, Object> result = new HashMap<String, Object>(properties.size());
		for (Map.Entry<?, ?> me : properties.entrySet())
			result.put(String.valueOf(me.getKey()).toLowerCase(Locale.ENGLISH), String.valueOf(me.getValue()));

		return result;
	}
}
