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
package org.cumulus4j.keymanager.front.shared;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Response sent back from the key-server to the client after it requested opening a session.
 * This might not necessarily
 * represent a new session as a previously opened session might just be refreshed and reused.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class AcquireCryptoSessionResponse implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String cryptoSessionID;

	private Date expiry;

	/**
	 * <p>
	 * Get the crypto-session's unique identifier.
	 * </p>
	 * <p>
	 * This identifier is composed of 3 parts:
	 * </p>
	 * <ul>
	 * <li><code>cryptoSessionIDPrefix</code>: A random ID of the key server. This is used to optimize communication between app-server
	 * and key server. A new prefix is generated at every startup of the key server.
	 * </li>
	 * <li>Separator '.': A dot is used as separator.</li>
	 * <li>The rest of the cryptoSessionID, which is unique within the scope of the prefix.</li>
	 * </ul>
	 * <p>
	 * Note, that this identifier is structurally the same, no matter if the key-manager is embedded in the client
	 * or separate in a key-server.
	 * </p>
	 *
	 * @return the crypto-session's unique identifier.
	 * @see #setCryptoSessionID(String)
	 */
	public String getCryptoSessionID() {
		return cryptoSessionID;
	}
	/**
	 * Set the crypto-session's unique identifier. See {@link #getCryptoSessionID()} for
	 * how such an identifier must look like.
	 * @param cryptoSessionID the crypto-session's unique identifier.
	 * @see #getCryptoSessionID()
	 */
	public void setCryptoSessionID(String cryptoSessionID) {
		this.cryptoSessionID = cryptoSessionID;
	}

	/**
	 * Get the timestamp, when this session expires. The session expires only, if it is not refreshed
	 * before this date (refreshing postpones the expiry).
	 * @return the timestamp, when this session expires.
	 * @see #setExpiry(Date)
	 */
	public Date getExpiry() {
		return expiry;
	}
	/**
	 * Set the timestamp, when this session expires.
	 * @param expiry the timestamp, when this session expires.
	 * @see #getExpiry()
	 */
	public void setExpiry(Date expiry) {
		this.expiry = expiry;
	}

}
