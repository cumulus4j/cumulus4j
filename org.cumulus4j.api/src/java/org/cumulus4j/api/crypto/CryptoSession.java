package org.cumulus4j.api.crypto;

import java.util.Date;

public interface CryptoSession
{
	static final String PROPERTY_CRYPTO_SESSION_ID = "cumulus4j.cryptoSessionID";

	void setCryptoManager(CryptoManager cryptoManager);
	CryptoManager getCryptoManager();

	String getCryptoSessionID();
	void setCryptoSessionID(String keyManagerSessionID);

	Date getCreationTimestamp();

	/**
	 * Encrypt the given <a href="http://en.wikipedia.org/wiki/Plaintext">plaintext</a>.
	 * @param plaintext the unencrypted information (aka <a href="http://en.wikipedia.org/wiki/Plaintext">plaintext</a>) to be encrypted.
	 * @return the encrypted information (aka <a href="http://en.wikipedia.org/wiki/Ciphertext">ciphertext</a>).
	 */
	Ciphertext encrypt(Plaintext plaintext);

	/**
	 * Decrypt the given <a href="http://en.wikipedia.org/wiki/Ciphertext">ciphertext</a>.
	 * @param ciphertext the encrypted information (aka <a href="http://en.wikipedia.org/wiki/Ciphertext">ciphertext</a>) to be decrypted.
	 * @return the unencrypted information (aka <a href="http://en.wikipedia.org/wiki/Plaintext">plaintext</a>).
	 */
	Plaintext decrypt(Ciphertext ciphertext);
}
