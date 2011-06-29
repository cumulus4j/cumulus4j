package org.cumulus4j.keymanager.api;

import java.io.Serializable;
import java.util.Arrays;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyManagerAPIConfiguration
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean readOnly;

	private String authUserName;

	private char[] authPassword;

	private String keyStoreID;

	private String keyManagerBaseURL;

	public KeyManagerAPIConfiguration() { }

	public KeyManagerAPIConfiguration(KeyManagerAPIConfiguration source)
	{
		setAuthUserName(source.getAuthUserName());
		setAuthPassword(source.getAuthPassword());
		setKeyStoreID(source.getKeyStoreID());
		setKeyManagerBaseURL(source.getKeyManagerBaseURL());
	}

	private void assertNotReadOnly()
	{
		if (readOnly)
			throw new IllegalStateException("This instance of KeyManagerAPIConfiguration is read-only! Cannot modify it, anymore!");
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	public void markReadOnly() {
		this.readOnly = true;
	}


	public String getAuthUserName() {
		return authUserName;
	}

	public void setAuthUserName(String authUserName) {
		assertNotReadOnly();
		this.authUserName = authUserName;
	}


	public char[] getAuthPassword() {
		return authPassword;
	}

	public void setAuthPassword(char[] authPassword) {
		assertNotReadOnly();

		char[] oldPw = this.authPassword;
		if (oldPw != null)
			Arrays.fill(oldPw, (char)0);

		this.authPassword = authPassword.clone(); // Cloning is essential, because we clear it later on.
	}


	public String getKeyStoreID() {
		return keyStoreID;
	}

	public void setKeyStoreID(String keyStoreID) {
		assertNotReadOnly();
		this.keyStoreID = keyStoreID;
	}


	public String getKeyManagerBaseURL() {
		return keyManagerBaseURL;
	}

	/**
	 *
	 * @param keyManagerBaseURL the base-URL of the remote key-server or a local file-URL, if a local key-store is to be used.
	 * This argument can be <code>null</code>, which means to use a local file in the default directory "${user.home}/.cumulus4j/".
	 */
	public void setKeyManagerBaseURL(String keyManagerBaseURL) {
		assertNotReadOnly();
		this.keyManagerBaseURL = keyManagerBaseURL;
	}

	@Override
	protected void finalize() throws Throwable {
		readOnly = false; // otherwise the following setAuthPassword(...) fails.
		setAuthPassword(null);
		super.finalize();
	}
}
