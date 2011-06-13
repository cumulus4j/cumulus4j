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
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class GetKeyResponse extends Response
{
	private static final long serialVersionUID = 1L;

	private long keyID;
	private byte[] keyEncodedEncrypted;

	public GetKeyResponse() { }

	public GetKeyResponse(Request request, long keyID, byte[] keyEncodedEncrypted) {
		super(request);

		if (keyEncodedEncrypted == null)
			throw new IllegalArgumentException("keyEncodedEncrypted == null");

		this.keyID = keyID;
		this.keyEncodedEncrypted = keyEncodedEncrypted;
	}

	public long getKeyID() {
		return keyID;
	}
	public void setKeyID(long keyID) {
		this.keyID = keyID;
	}

	public byte[] getKeyEncodedEncrypted() {
		return keyEncodedEncrypted;
	}
	public void setKeyEncodedEncrypted(byte[] key) {
		this.keyEncodedEncrypted = key;
	}
}
