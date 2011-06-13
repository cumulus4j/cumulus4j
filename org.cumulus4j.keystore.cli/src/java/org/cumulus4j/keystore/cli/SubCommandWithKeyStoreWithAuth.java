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

import org.kohsuke.args4j.Option;

/**
 * <p>
 * Abstract {@link SubCommand} implementation for being subclassed when a key-store and authentication
 * (user + password) is used.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class SubCommandWithKeyStoreWithAuth
extends SubCommandWithKeyStore
{
	@Option(name="-authUserName", required=true, usage="The authenticated user authorizing this action. If the very first user is created, this value is ignored.")
	private String authUserName;

	public String getAuthUserName()
	{
		return authUserName;
	}

	@Option(name="-authPassword", required=false, usage="The password for authenticating the user specified by -authUserName. If the very first user is created, this value is ignored. If omitted, the user will be asked interactively (if required, i.e. if not creating the very first user).")
	private String authPassword;

	public char[] getAuthPasswordAsCharArray()
	{
		return authPassword == null ? null : authPassword.toCharArray();
	}

	public String getAuthPassword()
	{
		return authPassword;
	}

	@Override
	public void prepare() throws Exception
	{
		super.prepare();
		if (authPassword == null && !getKeyStore().isEmpty())
			authPassword = promptPassword("authPassword: ");
	}
}
