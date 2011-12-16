package org.cumulus4j.integrationtest.gwt.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client side stub for the RPC service.
 * 
 * @author Jan Morlock - jan dot morlock at googlemail dot com
 */
@RemoteServiceRelativePath("movieService")
public interface MovieService extends RemoteService {

	/**
	 * Proof of concept service: fetch some movies.
	 * 
	 * @param cryptoSessionID
	 *            Crypto session id
	 * @return Movie string
	 */
	String fetchSomeMovies(String cryptoSessionID);
}
