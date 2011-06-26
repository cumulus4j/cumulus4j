package org.cumulus4j.keymanager.api.internal;

import java.util.Arrays;

import org.cumulus4j.keymanager.api.KeyManagerAPI;
import org.cumulus4j.keymanager.api.KeyManagerAPIInstantiationException;

public abstract class AbstractKeyManagerAPI
implements KeyManagerAPI
{
	protected static final String FILE_URL_PREFIX = "file:";

	private String authUserName;

	private char[] authPassword;

	private String keyStoreID;

	private String keyManagerBaseURL;

	protected boolean initialised = false;
	protected void assertNotInitialised()
	{
		if (initialised)
			throw new IllegalStateException("This instance of KeyManagerAPI is already initialised! Cannot modify configuration anymore!");
	}
	protected void assertInitialised()
	{
		if (! initialised)
			throw new IllegalStateException("This instance of KeyManagerAPI is not yet initialised! Finish configuration and call init() first!");
	}

	@Override
	public String getAuthUserName() {
		return authUserName;
	}

	@Override
	public void setAuthUserName(String authUserName)
	{
		assertNotInitialised();

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
		assertNotInitialised();

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
	public void setKeyStoreID(String keyStoreID)
	{
		assertNotInitialised();

		if (equals(this.keyStoreID, keyStoreID))
			return;

		this.keyStoreID = keyStoreID;
	}

	@Override
	public String getKeyManagerBaseURL() {
		return keyManagerBaseURL;
	}

	@Override
	public void setKeyManagerBaseURL(String keyManagerBaseURL)
	{
		assertNotInitialised();

		if (equals(this.keyManagerBaseURL, keyManagerBaseURL))
			return;

		this.keyManagerBaseURL = keyManagerBaseURL;
	}

	@Override
	public void init() throws KeyManagerAPIInstantiationException {
		initialised = true;
	}

	@Override
	protected void finalize() throws Throwable {
		initialised = false; // otherwise the following setAuthPassword(...) fails.
		setAuthPassword(null);
		super.finalize();
	}

	protected static boolean equals(Object o1, Object o2)
	{
		return o1 == o2 || (o1 != null && o1.equals(o2));
	}
}
