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

/**
 * <p>
 * {@link SubCommand} implementation for showing the help.
 * </p>
 * <p>
 * Since the 'help' sub-command is currently handled by {@link KeyStoreCLI} internally,
 * this is a dummy class at the moment (just to show the 'help' in the help, for example).
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class HelpSubCommand extends SubCommand {

	@Override
	public String getSubCommandName() {
		return "help";
	}

	@Override
	public String getSubCommandDescription() {
		return "Get help.";
	}

	@Override
	public void run() throws Exception {
		throw new UnsupportedOperationException("The help command is handled by the KeyStoreCLI itself.");
	}

}
