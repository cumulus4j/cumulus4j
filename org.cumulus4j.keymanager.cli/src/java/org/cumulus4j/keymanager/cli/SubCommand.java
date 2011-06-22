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

import org.kohsuke.args4j.Option;

/**
 * <p>
 * Sub-command for a certain CLI operation.
 * </p>
 * <p>
 * The key-store-command-line-interface uses a syntax similar to the svn command and the logic of the
 * command 'java -jar org.cumulus4j.keymanager.cli-VERSION.jar SUBCOMMAND -arg1 val1 -arg2 val2 ...'
 * is thus actually implemented by a class extending this class and {@link #getSubCommandName() registering}
 * for a certain 'SUBCOMMAND'.
 * </p>
 * <p>
 * Every subclass of this class can declare its arguments using annotations like {@link Option}.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class SubCommand
{
	/**
	 * Get the name of the sub-command, i.e. what the user has to write in the command line.
	 * @return the name of the sub-command.
	 */
	public abstract String getSubCommandName();

	/**
	 * Get the description for this sub-command.
	 * @return the description.
	 */
	public abstract String getSubCommandDescription();

	public void prepare()
	throws Exception
	{

	}

	public abstract void run()
	throws Exception;
}
