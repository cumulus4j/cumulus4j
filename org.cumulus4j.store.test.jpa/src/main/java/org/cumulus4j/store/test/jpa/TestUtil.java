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
package org.cumulus4j.store.test.jpa;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.nightlabs.util.IOUtil;
import org.slf4j.LoggerFactory;

public class TestUtil
{
	private static void populateMap(Properties destination, Properties source)
	{
		Map<String, String> propertiesMap = new HashMap<String, String>(System.getProperties().size());
		for (Map.Entry<?, ?> me : System.getProperties().entrySet())
			propertiesMap.put(me.getKey() == null ? null : me.getKey().toString(), me.getValue() == null ? null : me.getValue().toString());

		for (Map.Entry<?, ?> me : source.entrySet()) {
			String key = me.getKey() == null ? null : me.getKey().toString();
			String value = me.getValue() == null ? null : me.getValue().toString();

			if (value != null)
				value = IOUtil.replaceTemplateVariables(value, propertiesMap);

			if (value == null || "_NULL_".equals(value))
				destination.remove(key);
			else
				destination.put(key, value);
		}
	}

	/**
	 * Load a properties file. This is a convenience method delegating to {@link #loadProperties(String, boolean)}
	 * with <code>logToSystemOut == false</code> (it will thus use SLF4J to log).
	 * @param fileName the simple name of the properties file (no path!).
	 * @return the loaded and merged properties.
	 */
	public static Properties loadProperties(String fileName)
	{
		return loadProperties(fileName, false);
	}

	/**
	 * Load a properties file. The file is first loaded as resource and then merged with a file from the user's home directory
	 * (if it exists). Settings that are declared in the user's specific file override the settings from the non-user-specific
	 * file in the resources.
	 * @param fileName the simple name of the properties file (no path!).
	 * @param logToSystemOut whether to log to system out. This is useful, if the properties file to search is a <code>log4j.properties</code>.
	 * @return the loaded and merged properties.
	 */
	public static Properties loadProperties(String fileName, boolean logToSystemOut)
	{
		Properties result = new Properties();

		try {
			Properties defaultProps = new Properties();
			InputStream in = TestUtil.class.getClassLoader().getResourceAsStream(fileName);
			defaultProps.load(in);
			in.close();
			populateMap(result, defaultProps);

			File userPropsFile = new File(IOUtil.getUserHome(), fileName);
			if (userPropsFile.exists()) {
				Properties userProps = new Properties();
				in = new FileInputStream(userPropsFile);
				userProps.load(in);
				in.close();
				populateMap(result, userProps);
			}
			else {
				String msg = "loadProperties: File " + userPropsFile.getAbsolutePath() + " does not exist. Thus not overriding any settings with user-specific ones.";
				if (logToSystemOut)
					System.out.println(msg);
				else
					LoggerFactory.getLogger(TestUtil.class).info(msg);
			}
		} catch (IOException x) {
			throw new RuntimeException(x);
		}

		return result;
	}

	private static boolean loggingConfigured = false;

	public static void configureLoggingOnce()
	{
		if (loggingConfigured)
			return;

		loggingConfigured = true;

		Properties properties = loadProperties("cumulus4j-test-log4j.properties", true);
		PropertyConfigurator.configure(properties);
	}
}
