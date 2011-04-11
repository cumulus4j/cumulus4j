package org.cumulus4j.api.keymanagement;

/**
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface KeyManager
{
	static final String PROPERTY_KEY_MANAGER_ID = "cumulus4j.keyManagerID";
	static final String PROPERTY_KEY_MANAGER_SESSION_ID = "cumulus4j.keyManagerSessionID";

	void setKeyManagerID(String keyManagerID);
	String getKeyManagerID();

	KeyManagerSession getKeyManagerSession(String keyManagerSessionID);
}
