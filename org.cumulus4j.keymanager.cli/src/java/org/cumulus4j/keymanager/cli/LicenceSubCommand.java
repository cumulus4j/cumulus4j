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
package org.cumulus4j.keymanager.cli;

import java.io.BufferedReader;

import org.cumulus4j.keymanager.cli.resource.ResourceHelper;

/**
 * <p>
 * {@link SubCommand} implementation for showing the licence.
 * </p>
 * <p>
 * Calling this will simply output the AGPL onto the console.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class LicenceSubCommand extends SubCommand {

	@Override
	public String getSubCommandName() {
		return "licence";
	}

	@Override
	public String getSubCommandDescription() {
		return "Show the licence of Cumulus4j in general and this command line interface in particular.";
	}

	@Override
	public void run() throws Exception {
		System.out.println("Cumulus4j is free software released under the GNU AFFERO GENERAL PUBLIC LICENCE.");
		System.out.println();
		BufferedReader licenceReader = ResourceHelper.openLicenceAsBufferedReader();
		try {
			String line;
			while (null != (line = licenceReader.readLine())) {
				System.out.println(line);
			}
		} finally {
			licenceReader.close();
		}
	}

}
