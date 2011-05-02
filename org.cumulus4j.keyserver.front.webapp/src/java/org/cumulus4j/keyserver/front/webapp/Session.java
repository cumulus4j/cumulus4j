package org.cumulus4j.keyserver.front.webapp;

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
		this.cryptoSessionID = IdentifierUtil.createRandomID();

		this.userName = userName;
		this.password = password;
	}

	private String cryptoSessionID;
	private String userName;
	private char[] password;
	private Date lastUse;
	private Date expiry;
	private boolean locked = true;

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
