package org.cumulus4j.keymanager.gwt.client;

import com.google.gwt.json.client.JSONObject;

/**
 * Callback interface for rest requests.
 * 
 * @author Jan Morlock - jan dot morlock at googlemail dot com
 */
public interface RESTCallback {
	
	/**
	 * Method to call in case of a successful request.
	 * 
	 * @param val
	 *            Response object
	 */
	void onSuccess(JSONObject val);

	/**
	 * Method to call if the request failed.
	 * 
	 * @param textCallback
	 *            Error string
	 */
	void onError(String textCallback);
}
