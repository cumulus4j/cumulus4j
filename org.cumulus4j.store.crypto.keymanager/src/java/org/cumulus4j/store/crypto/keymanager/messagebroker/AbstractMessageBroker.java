package org.cumulus4j.store.crypto.keymanager.messagebroker;

import org.cumulus4j.keymanager.back.shared.Request;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractMessageBroker
implements MessageBroker
{
	/**
	 * The remote key server has to wait this long (in millisec). Its HTTP client's timeout should thus be longer
	 * than this time!
	 */
	protected long timeoutPollRequestForProcessing = 2L * 60L * 1000L;

	/**
	 * The local API client (who calls {@link #query(Class, Request)}) gets an exception, if the request was not processed &amp;
	 * answered within this timeout (in millisec).
	 */
	protected long queryTimeoutMSec = 5L * 60L * 1000L;

	@Override
	public ActiveKeyManagerChannelRegistration registerActiveKeyManagerChannel(String cryptoSessionIDPrefix, String internalKeyManagerChannelURL)
	{
		// no-op
		return null;
	}

	@Override
	public void unregisterActiveKeyManagerChannel(ActiveKeyManagerChannelRegistration registration)
	{
		// no-op
	}
}
