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
 * <p>
 * {@link Response} implementation to send a specific symmetric secret key to the app-server.
 * It is the response to a {@link GetKeyRequest}.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @see GetKeyRequest
 */
@XmlRootElement
public class GetKeyResponse extends Response
{
	private static final long serialVersionUID = 1L;

	private long keyID;
	private byte[] keyEncodedEncrypted;

	/**
	 * Create an empty instance of <code>GetKeyResponse</code>.
	 * Only used for serialisation/deserialisation.
	 */
	public GetKeyResponse() { }

	/**
	 * Create an instance of <code>GetKeyResponse</code> in order to reply the given <code>request</code>.
	 *
	 * @param request the request to be replied (an instance of {@link GetActiveEncryptionKeyRequest}).
	 * @param keyID the identifier of the key to be sent to the app-server.
	 * @param keyEncodedEncrypted the {@link KeyEncryptionUtil#encryptKey(byte[], org.cumulus4j.crypto.Cipher) encrypted} symmetric secret key.
	 */
	public GetKeyResponse(Request request, long keyID, byte[] keyEncodedEncrypted) {
		super(request);

		if (keyEncodedEncrypted == null)
			throw new IllegalArgumentException("keyEncodedEncrypted == null");

		this.keyID = keyID;
		this.keyEncodedEncrypted = keyEncodedEncrypted;
	}

	/**
	 * Get the identifier of the symmetric secret key transported by this response.
	 * @return the identifier of the symmetric secret key transported by this response.
	 * @see #setKeyID(long)
	 */
	public long getKeyID() {
		return keyID;
	}
	/**
	 * Set the identifier of the symmetric secret key transported by this response.
	 * @param keyID the identifier of the symmetric secret key transported by this response.
	 * @see #getKeyID()
	 */
	public void setKeyID(long keyID) {
		this.keyID = keyID;
	}

	/**
	 * Get the {@link KeyEncryptionUtil#encryptKey(byte[], org.cumulus4j.crypto.Cipher) encrypted} symmetric secret key.
	 * @return the {@link KeyEncryptionUtil#encryptKey(byte[], org.cumulus4j.crypto.Cipher) encrypted} symmetric secret key.
	 * @see #setKeyEncodedEncrypted(byte[])
	 */
	public byte[] getKeyEncodedEncrypted() {
		return keyEncodedEncrypted;
	}
	/**
	 * Set the {@link KeyEncryptionUtil#encryptKey(byte[], org.cumulus4j.crypto.Cipher) encrypted} symmetric secret key.
	 * @param key the {@link KeyEncryptionUtil#encryptKey(byte[], org.cumulus4j.crypto.Cipher) encrypted} symmetric secret key.
	 * @see #getKeyEncodedEncrypted()
	 */
	public void setKeyEncodedEncrypted(byte[] key) {
		this.keyEncodedEncrypted = key;
	}
}
