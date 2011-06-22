package org.cumulus4j.keymanager.api;

/**
 * <p>
 * Entry point for the key management API.
 * </p>
 * <p>
 * Use <code>new DefaultKeyManagerAPI()</code> to get an instance, which you should keep (e.g. in a static shared
 * instance or some other context). Except for this one reference to {@link DefaultKeyManagerAPI},
 * you should only reference the interfaces of this API project!
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface KeyManagerAPI
{

	String getAuthUserName();

	void setAuthUserName(String authUserName);


	char[] getAuthPassword();

	void setAuthPassword(char[] authPassword);


	String getKeyStoreID();

	void setKeyStoreID(String keyStoreID);


	String getKeyManagerBaseURL();

	/**
	 *
	 * @param keyManagerBaseURL the base-URL of the remote key-server or a local file-URL, if a local key-store is to be used.
	 * This argument can be <code>null</code>, which means to use a local file in the default directory "${user.home}/.cumulus4j/".
	 */
	void setKeyManagerBaseURL(String keyManagerBaseURL);


	void initDateDependentKeyStrategy(DateDependentKeyStrategyInitParam param);


	Session getSession(String appServerBaseURL);

}
