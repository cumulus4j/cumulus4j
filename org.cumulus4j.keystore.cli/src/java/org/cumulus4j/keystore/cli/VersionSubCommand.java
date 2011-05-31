package org.cumulus4j.keystore.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VersionSubCommand
extends SubCommand
{
	@Override
	public String getSubCommandName() {
		return "version";
	}

	@Override
	public String getSubCommandDescription() {
		return "Return the version number of the JAR.";
	}

	@Override
	public void run() throws Exception
	{
//		Manifest manifest = new Manifest();
//		InputStream in = VersionSubCommand.class.getResourceAsStream("/META-INF/MANIFEST.MF");
//		try {
//			manifest.read(in);
//		} catch (IOException x) {
//			throw new IOException("Cannot read resource: /META-INF/MANIFEST.MF", x);
//		} finally {
//			in.close();
//		}
//
//		String version = manifest.getMainAttributes().getValue("ImplementationVersion");
//		System.err.println(version);

		Properties properties = new Properties();
		InputStream in = VersionSubCommand.class.getResourceAsStream("/META-INF/maven/org.cumulus4j/org.cumulus4j.keystore/pom.properties");
		try {
			properties.load(in);
		} catch (IOException x) {
			throw new IOException("Cannot read resource: /META-INF/MANIFEST.MF", x);
		} finally {
			in.close();
		}
		String version = properties.getProperty("version");
		System.err.println(version);
	}

}
