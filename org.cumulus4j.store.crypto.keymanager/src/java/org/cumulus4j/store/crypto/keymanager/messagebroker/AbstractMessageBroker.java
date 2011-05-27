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
	private long queryTimeoutMSec = -1;

	private long pollRequestTimeout = -1;

	/**
	 * <p>
	 * Get the {@link MessageBroker#query(Class, Request) query} timeout in milliseconds.
	 * </p>
	 * <p>
	 * This method takes the system property {@link MessageBroker#SYSTEM_PROPERTY_QUERY_TIMEOUT} into account.
	 * If the system property is not present or not a valid number, the default value 300000 (5 minutes) is returned.
	 * </p>
	 *
	 * @return the {@link MessageBroker#query(Class, Request) query} timeout in milliseconds.
	 */
	public long getQueryTimeout()
	{
		if (queryTimeoutMSec < 0) {
			// TODO take MessageBroker.SYSTEM_PROPERTY_QUERY_TIMEOUT into account!
			queryTimeoutMSec = 5L * 60L * 1000L;
		}

		return queryTimeoutMSec;
	}

	/**
	 * <p>
	 * Get the {@link MessageBroker#pollRequest(String) pollRequest(....)} timeout in milliseconds.
	 * </p>
	 * <p>
	 * This method takes the system property {@link MessageBroker#SYSTEM_PROPERTY_POLL_REQUEST_TIMEOUT} into account.
	 * If the system property is not present or not a valid number, the default value 60000 (1 minute) is returned.
	 * </p>
	 * <p>
	 * Usually, a value of about 1 minute is recommended in most situations. However, when
	 * using certain runtimes, it must be much shorter  (e.g. the Google App Engine allows
	 * requests not to take longer than 30 sec, thus 20 sec are an appropriate time to stay safe).
	 * </p>
	 * <p>
	 * Additionally, since the remote key-manager must wait at maximum this time, its HTTP-client's
	 * timeout must be longer than this timeout.
	 * </p>
	 *
	 * @return the {@link MessageBroker#pollRequest(String) pollRequest(....)} timeout in milliseconds.
	 */
	public long getPollRequestTimeout()
	{
		if (pollRequestTimeout < 0) {
			// TODO take MessageBroker.SYSTEM_PROPERTY_POLL_REQUEST_TIMEOUT into account!
			pollRequestTimeout = 1L * 60L * 1000L;
		}

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
