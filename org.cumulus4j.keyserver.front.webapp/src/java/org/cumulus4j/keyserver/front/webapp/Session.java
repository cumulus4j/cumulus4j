package org.cumulus4j.keyserver.front.webapp;

import java.math.BigInteger;
import java.security.SecureRandom;
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
	private static SecureRandom random = new SecureRandom();

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
		this.cryptoSessionID = createCryptoSessionID();

		this.userName = userName;
		this.password = password;
	}

	private static String createCryptoSessionID()
	{
		byte[] val = new byte[17];
		random.nextBytes(val);
		val[0] = (byte)(val[0] & 0x7F); // ensure a positive value
		BigInteger bi = new BigInteger(val);
		String result = bi.toString(36).substring(1); // cut the first character, because its range is limited (never reaches 'z')

		if (result.length() < 26) { // prepend with '0' to reach a fixed length.
			StringBuilder sb = new StringBuilder(26);
			for (int i = result.length(); i < 26; ++i)
				sb.append('0');

			sb.append(result);
			result = sb.toString();
		}

		if (result.length() != 26)
			throw new IllegalStateException("Why is result.length != 26 chars?!");

		return result;
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
