package org.cumulus4j.keymanager.api;

import java.io.Serializable;
import java.util.Arrays;

import org.cumulus4j.keystore.KeyStore;

/**
 * <p>
 * Configuration of the {@link KeyManagerAPI}.
 * </p><p>
 * An instance of this class tells the <code>KeyManagerAPI</code> {@link #getKeyManagerBaseURL() where} the key store
 * is located and which key-store (identified by its {@link #getKeyStoreID() keyStoreID}) is to be used (among other things).
 * </p><p>
 * When you pass a <code>KeyManagerAPIConfiguration</code> instance to {@link KeyManagerAPI#setConfiguration(KeyManagerAPIConfiguration)},
 * it becomes {@link #isReadOnly() immutable}. If you want to change the configuration afterwards,
 * {@link #KeyManagerAPIConfiguration(KeyManagerAPIConfiguration) create a new one} and call
 * <code>KeyManagerAPI.setConfiguration(KeyManagerAPIConfiguration)</code> again.
 * </p>
 *
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

	/**
	 * Create an empty configuration.
	 */
	public KeyManagerAPIConfiguration() { }

	/**
	 * Create a copy of another configuration. You can use this to modify an existing (already immutable) configuration.
	 * @param source the original configuration to be copied. Can be <code>null</code>, which means that the new configuration
	 * will be completely empty (just as if it was created by the default constructor).
	 */
	public KeyManagerAPIConfiguration(KeyManagerAPIConfiguration source)
	{
		if (source != null) {
			setAuthUserName(source.getAuthUserName());
			setAuthPassword(source.getAuthPassword());
			setKeyStoreID(source.getKeyStoreID());
			setKeyManagerBaseURL(source.getKeyManagerBaseURL());
		}
	}

	/**
	 * Ensure that the configuration can be modified.
	 *
	 * @throws IllegalStateException if {@link #isReadOnly()}<code> == true</code>.
	 */
	private void assertNotReadOnly()
	throws IllegalStateException
	{
		if (readOnly)
			throw new IllegalStateException("This instance of KeyManagerAPIConfiguration is read-only! Cannot modify it, anymore!");
	}

	/**
	 * Get the immutable flag. Iff <code>true</code>, every attempt to modify this instance (i.e. every setter)
	 * will throw an {@link IllegalStateException}.
	 * @return the immutable flag.
	 * @see #markReadOnly()
	 */
	public boolean isReadOnly() {
		return readOnly;
	}

	/**
	 * Set the immutable flag. After this method was called, every setter will throw an {@link IllegalStateException}
	 * rendering this instance read-only.
	 * @see #isReadOnly()
	 */
	public void markReadOnly() {
		this.readOnly = true;
	}

	/**
	 * Get the user name that will be used for authentication at the {@link KeyStore}.
	 * @return the user name for authentication at the {@link KeyStore}.
	 * @see #setAuthUserName(String)
	 */
	public String getAuthUserName() {
		return authUserName;
	}

	/**
	 * Set the user name that will be used for authentication at the {@link KeyStore}.
	 * @param authUserName the user name for authentication at the {@link KeyStore}.
	 * @see #getAuthUserName()
	 */
	public void setAuthUserName(String authUserName) {
		assertNotReadOnly();
		this.authUserName = authUserName;
	}

	/**
	 * Get the password that will be used for authentication at the {@link KeyStore}.
	 * @return the password for authentication at the {@link KeyStore}.
	 * @see #setAuthPassword(char[])
	 */
	public char[] getAuthPassword() {
		return authPassword;
	}

	/**
	 * Set the password that will be used for authentication at the {@link KeyStore}.
	 * @param authPassword the password for authentication at the {@link KeyStore}.
	 * This will be copied, i.e. later modifications to the given password will not
	 * affect this configuration. You indeed should zero-out the given password as soon
	 * as you don't need to keep it in memory, anymore.
	 * @see #getAuthPassword()
	 */
	public void setAuthPassword(char[] authPassword) {
		assertNotReadOnly();

		char[] oldPw = this.authPassword;
		if (oldPw != null)
			Arrays.fill(oldPw, (char)0);

		this.authPassword = authPassword == null ? null : authPassword.clone(); // Cloning is essential, because we clear it later on.
	}

	/**
	 * <p>
	 * Get the {@link KeyStore}'s identifier.
	 * </p><p>
	 * One key manager can manage multiple key stores. This identifier specifies which one to use.
	 * </p>
	 * @return the {@link KeyStore}'s identifier.
	 * @see #setKeyStoreID(String)
	 */
	public String getKeyStoreID() {
		return keyStoreID;
	}

	/**
	 * Set the {@link KeyStore}'s identifier.
	 * @param keyStoreID the {@link KeyStore}'s identifier. This should not contain spaces and other special characters that
	 * might not be used in restricted operating-systems (like Windows), because this might be used as (part of) a file name.
	 * Actually, it should contain only characters that can be used in URLs.
	 * @see #getKeyStoreID()
	 */
	public void setKeyStoreID(String keyStoreID) {
		assertNotReadOnly();
		this.keyStoreID = keyStoreID;
	}

	/**
	 * Get the URL where the {@link KeyStore} is accessible. This can either be a local directory (the URL starts with
	 * "file:") or a key-server (accessible via REST over HTTP or HTTPS).
	 * @return the {@link KeyStore}'s base-URL (the complete URL is composed of this and the {@link #getKeyStoreID() key-store-ID}.
	 * @see #setKeyManagerBaseURL(String)
	 */
	public String getKeyManagerBaseURL() {
		return keyManagerBaseURL;
	}

	/**
	 * Set the URL where the {@link KeyStore} is accessible. This can either be a local directory (the URL starts with
	 * "file:") or a key-server (accessible via REST over HTTP or HTTPS).
	 * @param keyManagerBaseURL the base-URL of the remote key-server or a local file-URL (referencing a directory!),
	 * if a local key-store is to be used. This argument can be <code>null</code>, which means to use a local file in
	 * the default directory "&#36;{user.home}/.cumulus4j/".
	 * @see #getKeyManagerBaseURL()
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
