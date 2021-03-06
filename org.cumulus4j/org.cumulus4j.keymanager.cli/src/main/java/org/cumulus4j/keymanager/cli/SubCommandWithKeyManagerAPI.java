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

import org.cumulus4j.keymanager.api.DefaultKeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPIConfiguration;
import org.kohsuke.args4j.Option;

/**
 * <p>
 * Abstract {@link SubCommand} implementation for being subclassed when the {@link KeyManagerAPI} is used.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class SubCommandWithKeyManagerAPI
extends SubCommand
{
	@Option(name="-keyManagerBaseURL", required=false, usage="Specifies where the key-store is located (either a URL on a remote server or a local directory). If omitted, it defaults to '${user.home}/.cumulus4j'.")
	private String keyManagerBaseURL;

	@Option(name="-keyStoreID", required=true, usage="Specifies the key-store to work with.")
	private String keyStoreID;

	private KeyManagerAPI keyManagerAPI = new DefaultKeyManagerAPI();

	public KeyManagerAPI getKeyManagerAPI() {
		return keyManagerAPI;
	}

	@Override
	public void prepare() throws Exception
	{
		super.prepare();
		KeyManagerAPIConfiguration configuration = new KeyManagerAPIConfiguration();
		configuration.setKeyManagerBaseURL(keyManagerBaseURL);
		configuration.setKeyStoreID(keyStoreID);
		keyManagerAPI.setConfiguration(configuration);
	}

}
