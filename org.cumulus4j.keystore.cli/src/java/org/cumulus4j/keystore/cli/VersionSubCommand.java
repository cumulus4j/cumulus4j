package org.cumulus4j.keystore.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class VersionSubCommand
extends SubCommand
{
	public static String getVersion() throws IOException
	{
		Properties properties = new Properties();
		InputStream in = VersionSubCommand.class.getResourceAsStream("/META-INF/maven/org.cumulus4j/org.cumulus4j.keystore.cli/pom.properties");
		try {
			properties.load(in);
		} catch (IOException x) {
			throw new IOException("Cannot read resource: /META-INF/MANIFEST.MF", x);
		} finally {
			in.close();
		}
		String version = properties.getProperty("version");
		return version;
	}

	@Override
	public String getSubCommandName() {
		return "version";
	}

	@Override
	public String getSubCommandDescription() {
		return "Display the version of this JAR.";
	}

	@Override
	public void run() throws Exception
	{
		System.out.println(getVersion());
	}
}
