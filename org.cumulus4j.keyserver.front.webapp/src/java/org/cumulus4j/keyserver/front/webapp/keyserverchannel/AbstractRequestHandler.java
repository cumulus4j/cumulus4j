package org.cumulus4j.keyserver.front.webapp.keyserverchannel;

import org.cumulus4j.keyserver.back.shared.Request;

public abstract class AbstractRequestHandler<R extends Request> implements RequestHandler<R>
{
	private KeyServerChannelManager keyServerChannelManager;

	@Override
	public KeyServerChannelManager getKeyServerChannelManager() {
		return keyServerChannelManager;
	}

	@Override
	public void setKeyServerChannelManager(KeyServerChannelManager keyServerChannelManager) {
		this.keyServerChannelManager = keyServerChannelManager;
	}

}
