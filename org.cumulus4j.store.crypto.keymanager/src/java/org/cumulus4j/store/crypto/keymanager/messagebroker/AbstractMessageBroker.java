package org.cumulus4j.store.crypto.keymanager.messagebroker;

import org.cumulus4j.keymanager.back.shared.Request;

/**
 * Abstract super-class to be subclassed by {@link MessageBroker} implementations.
 * It is urgently recommended that <code>MessageBroker</code> implementations do not
 * directly implement the <code>MessageBroker</code> interface, but instead subclass this abstract class.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractMessageBroker
implements MessageBroker
{
	/**
	 * The remote key server has to wait this long (in millisec). Its HTTP client's timeout should thus be longer
	 * than this time!
	 */
	private long pollRequestTimeout = 2L * 60L * 1000L;

	/**
	 * The local API client (who calls {@link #query(Class, Request)}) gets an exception, if the request was not processed &amp;
	 * answered within this timeout (in millisec).
	 */
	private long queryTimeoutMSec = 5L * 60L * 1000L;

	public long getQueryTimeout() {
		// TODO take MessageBroker.SYSTEM_PROPERTY_QUERY_TIMEOUT into account!
		return queryTimeoutMSec;
	}

	public long getPollRequestTimeout() {
		// TODO take MessageBroker.SYSTEM_PROPERTY_POLL_REQUEST_TIMEOUT into account!
		return pollRequestTimeout;
	}

//	@Override
//	public ActiveKeyManagerChannelRegistration registerActiveKeyManagerChannel(String cryptoSessionIDPrefix, String internalKeyManagerChannelURL)
//	{
//		// no-op
//		return null;
//	}
//
//	@Override
//	public void unregisterActiveKeyManagerChannel(ActiveKeyManagerChannelRegistration registration)
//	{
//		// no-op
//	}
}
