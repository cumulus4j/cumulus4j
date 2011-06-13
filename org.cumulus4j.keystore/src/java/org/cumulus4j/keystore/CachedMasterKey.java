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

package org.cumulus4j.keystore;

import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class CachedMasterKey
{
	private static final Logger logger = LoggerFactory.getLogger(CachedMasterKey.class);

	public CachedMasterKey(String userName, char[] password, MasterKey masterKey) {
		this.userName = userName;
		this.password = password.clone(); // This clone() is essential, because we overwrite the password in the clear() method!
		this.masterKey = masterKey;
		this.lastUse = new Date();
	}

	private String userName;
	private char[] password;
	private MasterKey masterKey;
	private Date lastUse;

	public String getUserName() {
		return userName;
	}
	public char[] getPassword() {
		return password;
	}
	public MasterKey getMasterKey() {
		return masterKey;
	}
	public Date getLastUse() {
		return lastUse;
	}
	public void updateLastUse() {
		lastUse = new Date();
	}

	public void clear()
	{
		logger.debug("clear: Clearing for userName='{}'.", userName);

		char[] pw = password;
		if (pw != null) {
			Arrays.fill(pw, (char)0);
			password = null;
		}

		MasterKey mk = masterKey;
		if (mk != null) {
			mk.clear();
			masterKey = null;
		}
	}
}
