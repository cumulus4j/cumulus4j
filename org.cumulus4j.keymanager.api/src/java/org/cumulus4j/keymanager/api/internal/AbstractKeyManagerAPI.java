package org.cumulus4j.keymanager.api.internal;

import java.util.Arrays;

import org.cumulus4j.keymanager.api.KeyManagerAPI;

public abstract class AbstractKeyManagerAPI
implements KeyManagerAPI
{
	protected static final String FILE_URL_PREFIX = "file:";

	private String authUserName;

	private char[] authPassword;

	private String keyStoreID;

	private String keyManagerBaseURL;

	@Override
	public String getAuthUserName() {
		return authUserName;
	}

	@Override
	public void setAuthUserName(String authUserName) {
		if (equals(this.authUserName, authUserName))
			return;

		this.authUserName = authUserName;
	}

	@Override
	public char[] getAuthPassword() {
		return authPassword;
	}

	@Override
	public void setAuthPassword(char[] authPassword)
	{
		if (Arrays.equals(this.authPassword, authPassword))
			return;

		char[] oldPw = this.authPassword;
		this.authPassword = null;

		if (oldPw != null)
			Arrays.fill(oldPw, (char)0);

		this.authPassword = authPassword == null ? null : authPassword.clone();
	}

	@Override
	public String getKeyStoreID() {
		return keyStoreID;
	}

	@Override
	public void setKeyStoreID(String keyStoreID) {
		if (equals(this.keyStoreID, keyStoreID))
			return;

		this.keyStoreID = keyStoreID;
	}

	@Override
	public String getKeyManagerBaseURL() {
		return keyManagerBaseURL;
	}

	@Override
	public void setKeyManagerBaseURL(String keyManagerBaseURL) {
		if (equals(this.keyManagerBaseURL, keyManagerBaseURL))
			return;

		this.keyManagerBaseURL = keyManagerBaseURL;
	}

	@Override
	protected void finalize() throws Throwable {
		setAuthPassword(null);
		super.finalize();
	}

	protected static boolean equals(Object o1, Object o2)
	{
		return o1 == o2 || (o1 != null && o1.equals(o2));
	}
}
