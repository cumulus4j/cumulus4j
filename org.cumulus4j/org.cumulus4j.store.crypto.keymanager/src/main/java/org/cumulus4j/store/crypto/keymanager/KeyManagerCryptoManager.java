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
package org.cumulus4j.store.crypto.keymanager;

import org.cumulus4j.store.crypto.AbstractCryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;

/**
 * <p>
 * Implementation of {@link org.cumulus4j.store.crypto.CryptoManager CryptoManager} working with a
 * key-manager as shown in <a target="_blank" href="http://cumulus4j.org/${project.version}/documentation/deployment-scenarios.html">Deployment scenarios</a>.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyManagerCryptoManager extends AbstractCryptoManager
{
	private CryptoCache cryptoCache = new CryptoCache(this);

	@Override
	protected CryptoSession createCryptoSession() {
		return new KeyManagerCryptoSession();
	}

	public CryptoCache getCryptoCache() {
		return cryptoCache;
	}
}
