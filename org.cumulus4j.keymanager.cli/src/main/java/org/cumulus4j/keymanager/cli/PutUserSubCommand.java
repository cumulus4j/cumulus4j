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
 * {@link SubCommand} implementation for creating a new user or updating an existing one.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class PutUserSubCommand extends SubCommandWithKeyManagerAPIWithAuth
{
	@Option(name="-userName", required=true, usage="The name of the user to be created/updated.")
	private String userName;

	@Option(name="-password", required=false, usage="The password of the user. If omitted, the user will be asked for it interactively.")
	private String password;

	@Override
	public String getSubCommandName() {
		return "putUser";
	}

	@Override
	public String getSubCommandDescription() {
		return "Create a new user or change an existing user's password.";
	}

	@Override
	public void prepare() throws Exception {
		super.prepare();

		if (password == null)
			password = promptPassword("password: ");
	}

	@Override
	public void run() throws Exception {
		getKeyManagerAPI().putUser(userName, password.toCharArray());
	}
}
