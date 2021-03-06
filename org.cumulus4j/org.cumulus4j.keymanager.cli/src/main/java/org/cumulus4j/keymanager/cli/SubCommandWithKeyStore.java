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

import java.io.File;

import org.cumulus4j.keystore.KeyStore;
import org.kohsuke.args4j.Option;

/**
 * <p>
 * Abstract {@link SubCommand} implementation for being subclassed when a key-store is used.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @deprecated All sub-commands that currently subclass this class should instead subclass
 * {@link SubCommandWithKeyManagerAPI} or one of its subclasses.
 */
@Deprecated
public abstract class SubCommandWithKeyStore
extends SubCommand
{
	@Option(name="-keyStoreFile", required=true, usage="Specifies the key-store-file to work with.")
	private File keyStoreFile;

	public File getKeyStoreFile() {
		return keyStoreFile;
	}

	private KeyStore keyStore;

	public KeyStore getKeyStore()
	{
		return keyStore;
	}

	@Override
	public void prepare() throws Exception
	{
		super.prepare();
		String keyStoreID = keyStoreFile.getName();
		int i = keyStoreID.lastIndexOf('.');
		if (i >= 0) {
			keyStoreID = keyStoreID.substring(0, i);
		}
		keyStore = new KeyStore(keyStoreID, keyStoreFile);
	}
}
