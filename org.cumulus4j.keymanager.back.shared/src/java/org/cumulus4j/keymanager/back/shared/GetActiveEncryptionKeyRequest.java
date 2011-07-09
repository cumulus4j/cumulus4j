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

import org.cumulus4j.crypto.CryptoRegistry;

/**
 * <p>
 * {@link Request} implementation to get the currently active encryption key.
 * </p><p>
 * In order to prevent an attacker dumping an app-server's memory from gaining access to <b>all</b> the data,
 * Cumulus4j uses many different keys for encryption. Usually, it rotates the encryption key once per day, but
 * different settings are possible (e.g. once per hour for the very paranoid).
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @see GetActiveEncryptionKeyResponse
 */
@XmlRootElement
public class GetActiveEncryptionKeyRequest extends Request
{
	private static final long serialVersionUID = 1L;

	private Date timestamp;

	private String keyEncryptionTransformation;

	private byte[] keyEncryptionPublicKey;

	/**
	 * Create an empty instance of <code>GetActiveEncryptionKeyRequest</code>.
	 * Only used for serialisation/deserialisation.
	 */
	public GetActiveEncryptionKeyRequest() { }

	/**
	 * Create an instance of <code>GetActiveEncryptionKeyRequest</code> for asking the key-manager about
	 * the currently active encryption key.
	 *
	 * @param cryptoSessionID the identifier of the crypto-session in which the request should be processed.
	 * It must exist and be unlocked for this request to succeed.
	 * @param keyEncryptionTransformation the asymmetric encryption algorithm (with padding) that should be
	 * used by the key-manager to encrypt the symmetric secret key, before sending it to the app-server. For example
	 * "RSA//OAEPWITHSHA1ANDMGF1PADDING".
	 * @param keyEncryptionPublicKey the public key to be used by the key-manager to encrypt the
	 * key when sending it back to the app-server.
	 */
	public GetActiveEncryptionKeyRequest(String cryptoSessionID, String keyEncryptionTransformation, byte[] keyEncryptionPublicKey) {
		super(cryptoSessionID);
		this.keyEncryptionTransformation = keyEncryptionTransformation;
		this.keyEncryptionPublicKey = keyEncryptionPublicKey;
		this.timestamp = new Date();
	}

	/**
	 * Get the timestamp which the active encryption key should be determined for. The main reason for this
	 * is to prevent problems when the key-manager's clock is incorrect by using the app-server's timestamp.
	 * @return the timestamp which the active encryption key should be determined for.
	 */
	public Date getTimestamp() {
		return timestamp;
	}

	/**
	 * Set the timestamp which the active encryption key should be determined for
	 * @param timestamp the timestamp which the active encryption key should be determined for
	 */
	public void setTimestamp(Date timestamp) {
		this.timestamp = timestamp;
	}

	/**
	 * <p>
	 * Get the asymmetric encryption algorithm to be used to encrypt the symmetric secret key.
	 * </p><p>
	 * The key-manager uses this transformation
	 * (which should include a padding, e.g. "RSA//OAEPWITHSHA1ANDMGF1PADDING") to
	 * {@link CryptoRegistry#createCipher(String) obtain a Cipher} for encrypting the secret key
	 * before sending it to the app-server.
	 * </p>
	 * @return the asymmetric encryption algorithm to be used when encrypting the symmetric secret key.
	 * @see #setKeyEncryptionTransformation(String)
	 */
	public String getKeyEncryptionTransformation() {
		return keyEncryptionTransformation;
	}
	/**
	 * Set the asymmetric encryption algorithm to be used when encrypting the symmetric secret key.
	 * @param keyEncryptionTransformation the asymmetric encryption algorithm to be used when encrypting the symmetric secret key.
	 * @see #getKeyEncryptionTransformation()
	 */
	public void setKeyEncryptionTransformation(String keyEncryptionTransformation) {
		this.keyEncryptionTransformation = keyEncryptionTransformation;
	}

	/**
	 * Get the public key to be used to encrypt the symmetric secret key.
	 * @return the public key to be used to encrypt the symmetric secret key.
	 */
	public byte[] getKeyEncryptionPublicKey() {
		return keyEncryptionPublicKey;
	}
	/**
	 * Set the public key to be used to encrypt the symmetric secret key.
	 * @param keyEncryptionPublicKey the public key to be used to encrypt the symmetric secret key.
	 */
	public void setKeyEncryptionPublicKey(byte[] keyEncryptionPublicKey) {
		this.keyEncryptionPublicKey = keyEncryptionPublicKey;
	}
}
