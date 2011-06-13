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

package org.cumulus4j.store.crypto.keymanager.messagebroker;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @deprecated Currently not used anymore. This class might disappear or it might be used again, later.
 */
@Deprecated
public class ActiveKeyManagerChannelRegistration
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private UUID id = UUID.randomUUID();
	private String clusterNodeID;
	private String cryptoSessionIDPrefix;

	public ActiveKeyManagerChannelRegistration(String clusterNodeID, String cryptoSessionIDPrefix)
	{
		if (clusterNodeID == null)
			throw new IllegalArgumentException("clusterNodeID == null");

		if (cryptoSessionIDPrefix == null)
			throw new IllegalArgumentException("cryptoSessionIDPrefix == null");

		this.clusterNodeID = clusterNodeID;
		this.cryptoSessionIDPrefix = cryptoSessionIDPrefix;
	}

	public String getClusterNodeID() {
		return clusterNodeID;
	}
	public String getCryptoSessionIDPrefix() {
		return cryptoSessionIDPrefix;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActiveKeyManagerChannelRegistration other = (ActiveKeyManagerChannelRegistration) obj;
		return this.id.equals(other.id);
	}

	@Override
	public String toString() {
		return super.toString() + '[' + id + ']';
	}
}
