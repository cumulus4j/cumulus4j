package org.cumulus4j.keystore;

import java.util.Arrays;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedMasterKey
{
	private static final Logger logger = LoggerFactory.getLogger(CachedMasterKey.class);

	public CachedMasterKey(String userName, char[] password, MasterKey masterKey) {
		this.userName = userName;
		this.password = password;
		this.masterKey = masterKey;
		this.lastUse = new Date();
	}

	private String userName;
	private char[] password;
	private MasterKey masterKey;
	private Date lastUse;

	public String getUserName() {
		return userName;
	}
	public char[] getPassword() {
		return password;
	}
	public MasterKey getMasterKey() {
		return masterKey;
	}
	public Date getLastUse() {
		return lastUse;
	}
	public void updateLastUse() {
		lastUse = new Date();
	}

	public void clear()
	{
		logger.debug("clear: Clearing for userName='{}'.", userName);

		char[] pw = password;
		if (pw != null) {
			Arrays.fill(pw, (char)0);
			password = null;
		}

		MasterKey mk = masterKey;
		if (mk != null) {
			mk.clear();
			masterKey = null;
		}
	}
}
