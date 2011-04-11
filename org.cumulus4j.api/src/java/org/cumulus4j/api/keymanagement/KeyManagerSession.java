package org.cumulus4j.api.keymanagement;

import java.util.Date;

import javax.crypto.Cipher;

public interface KeyManagerSession
{
	void setKeyManager(KeyManager keyManager);
	KeyManager getKeyManager();

	String getKeyManagerSessionID();
	void setKeyManagerSessionID(String keyManagerSessionID);

	Date getCreationTimestamp();

	/**
	 * Get the {@link Cipher} to be used for encryption.
	 * @param keyID the key identifier.
	 * @return the {@link Cipher} for encrypting data; must not be <code>null</code>.
	 */
	Cipher getEncrypter(long keyID);

	/**
	 * Get the {@link Cipher} to be used for decryption.
	 * @param keyID the key identifier.
	 * @return the {@link Cipher} for decrypting data; must not be <code>null</code>.
	 */
	Cipher getDecrypter(long keyID);
}
