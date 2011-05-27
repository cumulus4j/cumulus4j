package org.cumulus4j.keymanager.channel;

import org.cumulus4j.keymanager.back.shared.Request;

/**
 * Abstract base class for {@link RequestHandler} implementations.
 * Implementors should subclass this class instead of directly implementing the
 * <code>RequestHandler</code> interface.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 *
 * @param <R> the request type for which this request handler is responsible.
 */
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
