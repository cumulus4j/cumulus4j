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
package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Super-class for all requests sent from app-server to key-manager.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @see Response
 */
@XmlRootElement
public abstract class Request extends Message
{
	private static final long serialVersionUID = 1L;

	private String cryptoSessionID;

	public Request() { }

	public Request(String cryptoSessionID)
	{
		if (cryptoSessionID == null)
			throw new IllegalArgumentException("cryptoSessionID == null");

		setRequestID(IdentifierUtil.createRandomID());
		this.cryptoSessionID = cryptoSessionID;
	}

	public String getCryptoSessionID() {
		return cryptoSessionID;
	}
	public void setCryptoSessionID(String cryptoSessionID) {
		this.cryptoSessionID = cryptoSessionID;
	}

	public String getCryptoSessionIDPrefix()
	{
		String id = cryptoSessionID;
		if (id == null)
			return null;

		int dotIdx = id.indexOf('.');
		if (dotIdx < 0)
			throw new IllegalStateException("cryptoSessionID does not contain a dot ('.')!!!");

		return id.substring(0, dotIdx);
	}

	@Override
	public String toString() {
		return super.toString() + '[' + getRequestID() + ',' + cryptoSessionID + ']';
	}
}
