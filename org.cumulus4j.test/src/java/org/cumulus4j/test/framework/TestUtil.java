package org.cumulus4j.test.framework;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.nightlabs.util.IOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestUtil
{
	private static final Logger logger = LoggerFactory.getLogger(TestUtil.class);

	private static void populateMap(Properties destination, Properties source)
	{
		Map<String, String> propertiesMap = new HashMap<String, String>(System.getProperties().size());
		for (Map.Entry<?, ?> me : System.getProperties().entrySet())
			propertiesMap.put(me.getKey() == null ? null : me.getKey().toString(), me.getValue() == null ? null : me.getValue().toString());

		for (Map.Entry<?, ?> me : source.entrySet()) {
			String value = me.getValue() == null ? null : me.getValue().toString();

			if (value != null)
				value = IOUtil.replaceTemplateVariables(value, propertiesMap);

			destination.put(me.getKey() == null ? null : me.getKey().toString(), value);
		}
	}

	public static Properties loadProperties(String fileName)
	{
		Properties result = new Properties();

		try {
			Properties defaultProps = new Properties();
			InputStream in = TransactionalRunner.class.getClassLoader().getResourceAsStream(fileName);
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
			else
				logger.info("loadProperties: File " + userPropsFile.getAbsolutePath() + " does not exist. Thus not overriding any settings with user-specific ones.");
		} catch (IOException x) {
			throw new RuntimeException(x);
		}

		return result;
	}
}
