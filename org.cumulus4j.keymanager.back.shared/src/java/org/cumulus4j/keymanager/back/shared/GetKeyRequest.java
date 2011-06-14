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
public class GetKeyRequest extends Request
{
	private static final long serialVersionUID = 1L;

	private long keyID;

	private String keyEncryptionTransformation;

	private byte[] keyEncryptionPublicKey;

	public GetKeyRequest() { }

	public GetKeyRequest(String cryptoSessionID, long keyID, String keyEncryptionAlgorithm, byte[] keyEncryptionPublicKey) {
		super(cryptoSessionID);
		this.keyID = keyID;
		this.keyEncryptionTransformation = keyEncryptionAlgorithm;
		this.keyEncryptionPublicKey = keyEncryptionPublicKey;
	}

	public long getKeyID() {
		return keyID;
	}
	public void setKeyID(long keyID) {
		this.keyID = keyID;
	}

	public String getKeyEncryptionTransformation() {
		return keyEncryptionTransformation;
	}
	public void setKeyEncryptionTransformation(String keyEncryptionAlgorithm) {
		this.keyEncryptionTransformation = keyEncryptionAlgorithm;
	}

	public byte[] getKeyEncryptionPublicKey() {
		return keyEncryptionPublicKey;
	}
	public void setKeyEncryptionPublicKey(byte[] keyEncryptionPublicKey) {
		this.keyEncryptionPublicKey = keyEncryptionPublicKey;
	}
}
