package org.cumulus4j.keymanager.channel;

import org.cumulus4j.keymanager.back.shared.Request;

public abstract class AbstractRequestHandler<R extends Request> implements RequestHandler<R>
{
	private KeyManagerChannelManager keyManagerChannelManager;

	@Override
	public KeyManagerChannelManager getKeyManagerChannelManager() {
		return keyManagerChannelManager;
	}

	@Override
	public void setKeyManagerChannelManager(KeyManagerChannelManager keyManagerChannelManager) {
		this.keyManagerChannelManager = keyManagerChannelManager;
	}

}
