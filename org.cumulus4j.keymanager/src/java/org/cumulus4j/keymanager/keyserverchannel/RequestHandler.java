package org.cumulus4j.keymanager.keyserverchannel;

import org.cumulus4j.keyserver.back.shared.Request;
import org.cumulus4j.keyserver.back.shared.Response;

public interface RequestHandler<R extends Request>
{
	KeyServerChannelManager getKeyServerChannelManager();
	void setKeyServerChannelManager(KeyServerChannelManager keyServerChannelManager);

	/**
	 * Handle the given request.
	 * @param request the request to be handled; never <code>null</code>.
	 * @return the response for the given request; must not be <code>null</code>.
	 */
	Response handle(R request) throws Throwable;
}
