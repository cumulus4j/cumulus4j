package org.cumulus4j.integrationtest.gwt.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * The async counterpart of <code>MovieService</code>.
 * 
 * @author Jan Morlock - jan dot morlock at googlemail dot com
 */
public interface MovieServiceAsync {

	/**
	 * Proof of concept service: fetch some movies.
	 * 
	 * @param cryptoSessionID
	 *            Crypto session id
	 * @param callback
	 *            Callback object
	 */
	void fetchSomeMovies(String cryptoSessionID, AsyncCallback<String> callback);
}
