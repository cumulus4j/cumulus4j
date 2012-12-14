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

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * {@link Response} implementation to send the currently active encryption key to the app-server.
 * It is the response to a {@link GetActiveEncryptionKeyRequest}.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @see GetActiveEncryptionKeyRequest
 */
@XmlRootElement
public class GetActiveEncryptionKeyResponse extends GetKeyResponse
{
	private static final long serialVersionUID = 1L;

	private Date activeUntilExcl;

	/**
	 * Create an empty instance of <code>GetActiveEncryptionKeyResponse</code>.
	 * Only used for serialisation/deserialisation.
	 */
	public GetActiveEncryptionKeyResponse() { }

	/**
	 * Create an instance of <code>GetActiveEncryptionKeyResponse</code> in order to reply the given <code>request</code>.
	 *
	 * @param request the request to be replied (an instance of {@link GetActiveEncryptionKeyRequest}).
	 * @param keyID the identifier of the key to be sent to the app-server.
	 * @param keyEncodedEncrypted the {@link KeyEncryptionUtil#encryptKey(byte[], org.cumulus4j.crypto.Cipher) encrypted} symmetric secret key.
	 * @param activeUntilExcl the timestamp until which this key should be used for encryption. As soon as this timestamp
	 * is reached, the app-server should again send a {@link GetActiveEncryptionKeyRequest} to the key-manager.
	 */
	public GetActiveEncryptionKeyResponse(Request request, long keyID, byte[] keyEncodedEncrypted, Date activeUntilExcl)
	{
		super(request, keyID, keyEncodedEncrypted);
		this.activeUntilExcl = activeUntilExcl;
	}

	/**
	 * Get the moment in time until (excluding) which the key transported by this response should be used for encryption.
	 * After this timestamp (or more precisely when it is reached), the app-server should request it again.
	 * @return the moment in time until which the key transported by this response should be used for encryption.
	 * @see #setActiveUntilExcl(Date)
	 */
	public Date getActiveUntilExcl() {
		return activeUntilExcl;
	}
	/**
	 * Set the moment in time until (excluding) which the key transported by this response should be used for encryption.
	 * @param activeUntilExcl the moment in time until which the key transported by this response should be used for encryption.
	 * @see #getActiveUntilExcl()
	 */
	public void setActiveUntilExcl(Date activeUntilExcl) {
		this.activeUntilExcl = activeUntilExcl;
	}
}
