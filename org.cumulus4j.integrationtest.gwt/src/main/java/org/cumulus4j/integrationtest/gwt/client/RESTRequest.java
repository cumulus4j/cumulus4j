package org.cumulus4j.integrationtest.gwt.client;

import com.google.gwt.json.client.JSONObject;

/**
 * <p>
 * Object containing all information necessary for performing rest requests.
 * </p>
 * <p>
 * <b>Note:</b> the request is performed as http post request.
 * </p>
 * 
 * @author Jan Morlock - jan dot morlock at googlemail dot com
 */
public class RESTRequest {

	/**
	 * Uniform resource locator to call.
	 */
	private String url;

	/**
	 * Username to be used for authentication request.
	 */
	private String username;

	/**
	 * Password to be used for authentication request.
	 */
	private String password;

	/**
	 * Request context information.
	 */
	private JSONObject data;

	/**
	 * Get the url.
	 * 
	 * @return url
	 */
	public final String getUrl() {
		return url;
	}

	/**
	 * Set the url.
	 * 
	 * @param newUrl
	 *            New url to set
	 */
	public final void setUrl(final String newUrl) {
		url = newUrl;
	}

	/**
	 * Get the user name.
	 * 
	 * @return User name
	 */
	public final String getUsername() {
		return username;
	}

	/**
	 * Set the user name.
	 * 
	 * @param newUsername
	 *            New user name to set
	 */
	public final void setUsername(final String newUsername) {
		username = newUsername;
	}

	/**
	 * Get the password.
	 * 
	 * @return Password
	 */
	public final String getPassword() {
		return password;
	}

	/**
	 * Set the password.
	 * 
	 * @param newPassword
	 *            New password to set
	 */
	public final void setPassword(final String newPassword) {
		password = newPassword;
	}

	/**
	 * Get the request data.
	 * 
	 * @return Request data
	 */
	public final JSONObject getData() {
		return data;
	}

	/**
	 * Set the request data.
	 * 
	 * @param newData
	 *            New data to set
	 */
	public final void setData(final JSONObject newData) {
		data = newData;
	}
}
