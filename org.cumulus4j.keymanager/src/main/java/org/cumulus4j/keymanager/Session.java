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
package org.cumulus4j.keymanager;

import java.util.Arrays;
import java.util.Date;

import org.cumulus4j.keymanager.back.shared.IdentifierUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Session to control and restrict the key exchange with the application server.
 * </p>
 * <p>
 * In order to protect the keys as well as possible, keys can only be requested from an application
 * server within the scope of a so-called crypto-session. The client controls when to open/close a crypto-session
 * and when to allow keys to be transferred. Key transfer is only possible while a session is {@link #setReleased(boolean) unlocked}.
 * </p>
 * <p>
 * This is not API! Use the classes and interfaces provided by <code>org.cumulus4j.keymanager.api</code> instead.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Session
{
	private static final Logger logger = LoggerFactory.getLogger(Session.class);

	private SessionManager sessionManager;

	protected Session(SessionManager sessionManager, String userName, char[] password)
	{
		if (sessionManager == null)
			throw new IllegalArgumentException("sessionManager == null");

		if (userName == null)
			throw new IllegalArgumentException("userName == null");

		if (password == null)
			throw new IllegalArgumentException("password == null");

		this.sessionManager = sessionManager;

		// see org.cumulus4j.keymanager.back.shared.Request#getCryptoSessionIDPrefix()
		// see org.cumulus4j.store.crypto.AbstractCryptoSession#getKeyStoreID()
		this.cryptoSessionID = (
				sessionManager.getCryptoSessionIDPrefix()
				+ '*'
				+ Long.toString(sessionManager.nextCryptoSessionSerial(), 36)
				+ '*'
				+ IdentifierUtil.createRandomID(6)
		);

		this.userName = userName;
		// Clone to prevent the password in the session from being nulled, when the outside password is nulled
		// or the outside password from being corrupted when this session is closed. Marco :-)
		this.password = password.clone();
	}

	private String cryptoSessionID;
	private String userName;
	private char[] password;
	private volatile Date lastUse;
	private volatile Date expiry;
	private volatile boolean released;

	/**
	 * Get the identifier of this session.
	 * @return the session's unique identifier.
	 */
	public String getCryptoSessionID() {
		return cryptoSessionID;
	}

	public String getUserName() {
		return userName;
	}
	public char[] getPassword() {
		return password;
	}

	public Date getLastUse() {
		return lastUse;
	}

	protected void updateLastUse(long expiryAgeMSec) {
		lastUse = new Date();
		expiry = new Date(lastUse.getTime() + expiryAgeMSec);
	}

	public Date getExpiry() {
		return expiry;
	}

	public void destroy()
	{
		SessionManager sm = sessionManager;
		if (sm == null)
			return;

		sm.onDestroySession(this);

		sessionManager = null;

		logger.debug("destroy: Destroying session for userName='{}' cryptoSessionID='{}'.", userName, cryptoSessionID);

		char[] pw = password;
		if (pw != null) {
			Arrays.fill(pw, (char)0);
			password = null;
		}

		cryptoSessionID = null;
		userName = null;
	}

	/**
	 * <p>
	 * Set the 'released' status.
	 * </p>
	 * <p>
	 * The application server can only request keys from a session that is currently acquired. That means, a session
	 * should first be acquired, then the app-server should be made to work (on behalf of the client) and finally,
	 * it should be released again.
	 * </p>
	 *
	 * @param released the new 'released' status.
	 */
	protected void setReleased(boolean released) {
		this.released = released;
	}

	public void release() {
		SessionManager sm = sessionManager;
		if (sm == null)
			return;

		sm.onReleaseSession(this);
	}

	public boolean isReleased() {
		return released;
	}

	public void reacquire() {
		SessionManager sm = sessionManager;
		if (sm == null)
			return;

		sm.onReacquireSession(this);
	}
}
