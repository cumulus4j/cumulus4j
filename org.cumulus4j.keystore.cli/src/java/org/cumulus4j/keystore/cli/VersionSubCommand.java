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
package org.cumulus4j.keystore.cli;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * <p>
 * {@link SubCommand} implementation for showing the version number of the command
 * line tool.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class VersionSubCommand
extends SubCommand
{
	public static String getVersion() throws IOException
	{
		Properties properties = new Properties();
		InputStream in = VersionSubCommand.class.getResourceAsStream("/META-INF/maven/org.cumulus4j/org.cumulus4j.keystore.cli/pom.properties");
		if (in == null)
			return "UNKNOWN";

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
