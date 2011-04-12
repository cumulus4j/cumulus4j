package org.cumulus4j.api.crypto;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface CryptoManager
{
	static final String PROPERTY_CRYPTO_MANAGER_ID = "cumulus4j.cryptoManagerID";

	void setCryptoManagerID(String cryptoManagerID);
	String getCryptoManagerID();

	CryptoSession getCryptoSession(String cryptoSessionID);
}
