package org.cumulus4j.keymanager;

import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class Session
{
	private static final Logger logger = LoggerFactory.getLogger(Session.class);

	private SessionManager sessionManager;

	public Session(SessionManager sessionManager, String userName, char[] password)
	{
		if (sessionManager == null)
			throw new IllegalArgumentException("sessionManager == null");

		if (userName == null)
			throw new IllegalArgumentException("userName == null");

		if (password == null)
			throw new IllegalArgumentException("password == null");

		this.sessionManager = sessionManager;

		this.cryptoSessionID = (
				sessionManager.getCryptoSessionIDPrefix()
				+ '.'
				+ Long.toString(sessionManager.nextCryptoSessionSerial(), 36)
				+ '.'
				+ IdentifierUtil.createRandomID(10)
		);

		this.userName = userName;
		// Clone to prevent the password in the session from being nulled, when the outside password is nulled
		// or the outside password from being corrupted when this session is closed. Marco :-)
		this.password = password.clone();
	}

	private String cryptoSessionID;
	private String userName;
	private char[] password;
	private Date lastUse;
	private Date expiry;
	private boolean locked = true;

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

	public void updateLastUse(long expiryAgeMSec) {
		lastUse = new Date();
		expiry = new Date(lastUse.getTime() + expiryAgeMSec);
	}

	public Date getExpiry() {
		return expiry;
	}

	public void close()
	{
		SessionManager sm = sessionManager;
		if (sm == null)
			return;

		sm.onCloseSession(this);

		sessionManager = null;

		logger.debug("close: Closing session for userName='{}' cryptoSessionID='{}'.", userName, cryptoSessionID);

		char[] pw = password;
		if (pw != null) {
			Arrays.fill(pw, (char)0);
			password = null;
		}

		cryptoSessionID = null;
		userName = null;
	}

	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public boolean isLocked() {
		return locked;
	}
}
