package org.cumulus4j.store.crypto.keymanager.messagebroker;

import java.util.concurrent.TimeoutException;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;
import org.cumulus4j.keymanager.back.shared.NullResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.store.crypto.keymanager.rest.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(AbstractMessageBroker.class);

	private long queryTimeoutMSec = -1;

	private long pollRequestTimeout = -1;

	@Override
	public long getQueryTimeout()
	{
		if (queryTimeoutMSec < 0) {
			String property = System.getProperty(SYSTEM_PROPERTY_QUERY_TIMEOUT);
			long timeout = -1;
			if (property != null && !property.isEmpty()) {
				try {
					timeout = Long.parseLong(property);
				} catch (NumberFormatException x) {
					logger.warn("Value \"{}\" of system property '{}' is not valid, because it cannot be parsed as number!", property, SYSTEM_PROPERTY_QUERY_TIMEOUT);
				}
				if (timeout < 0)
					logger.warn("Value \"{}\" of system property '{}' is not valid, because it is less than 0!", property, SYSTEM_PROPERTY_QUERY_TIMEOUT);
				else {
					logger.info("System property '{}' is specified with value {}.", SYSTEM_PROPERTY_QUERY_TIMEOUT, timeout);
					queryTimeoutMSec = timeout;
				}
			}

			if (queryTimeoutMSec < 0) {
				timeout = 5L * 60L * 1000L;
				queryTimeoutMSec = timeout;
				logger.info("System property '{}' is not specified; using default value {}.", SYSTEM_PROPERTY_QUERY_TIMEOUT, timeout);
			}
		}

		return queryTimeoutMSec;
	}

	@Override
	public long getPollRequestTimeout()
	{
		if (pollRequestTimeout < 0) {
			String property = System.getProperty(SYSTEM_PROPERTY_POLL_REQUEST_TIMEOUT);
			long timeout = -1;
			if (property != null && !property.isEmpty()) {
				try {
					timeout = Long.parseLong(property);
				} catch (NumberFormatException x) {
					logger.warn("Value \"{}\" of system property '{}' is not valid, because it cannot be parsed as number!", property, SYSTEM_PROPERTY_POLL_REQUEST_TIMEOUT);
				}
				if (timeout < 0)
					logger.warn("Value \"{}\" of system property '{}' is not valid, because it is less than 0!", property, SYSTEM_PROPERTY_POLL_REQUEST_TIMEOUT);
				else {
					logger.info("System property '{}' is specified with value {}.", SYSTEM_PROPERTY_POLL_REQUEST_TIMEOUT, timeout);
					pollRequestTimeout = timeout;
				}
			}

			if (pollRequestTimeout < 0) {
				timeout = 1L * 60L * 1000L;
				pollRequestTimeout = timeout;
				logger.info("System property '{}' is not specified; using default value {}.", SYSTEM_PROPERTY_POLL_REQUEST_TIMEOUT, timeout);
			}
		}

		return pollRequestTimeout;
	}

//	public static void main(String[] args)
//	throws Exception
//	{
//		MessageBroker mb = new MessageBrokerPMF();
//		mb.query(null, new GetKeyRequest());
//	}

	@Override
	public final <R extends Response> R query(Class<R> responseClass, Request request)
	throws TimeoutException, ErrorResponseException
	{
		Class<? extends Response> rc = responseClass;
		if (rc == null)
			rc = NullResponse.class;

		if (request == null)
			throw new IllegalArgumentException("request == null");

		Response response = _query(rc, request);

		if (response == null) // the implementation obviously already unmasked null somehow => directly returning it.
			return null;

		// A NullResponse which has a requestID assigned is forwarded to the requester and must be transformed into null here.
		if (response instanceof NullResponse)
			return null;

		if (response instanceof ErrorResponse)
			throw new ErrorResponseException((ErrorResponse)response);

		if (responseClass == null) {
			if (logger.isDebugEnabled()) {
				Exception x = new Exception("StackTrace");
				logger.warn("query: Caller passed responseClass=null, i.e. does not expect a result, but the server sent one, which we discard (we return null nevertheless). Here's the response we got: " + response, x);
			}
			else
				logger.warn("query: Caller passed responseClass=null, i.e. does not expect a result, but the server sent one, which we discard (we return null nevertheless). Enable DEBUG logging to get a stack trace. Here's the response we got: {}", response);

			return null;
		}

		try {
			return responseClass.cast(response);
		} catch (ClassCastException x) { // this exception has no nice message (according to source code), hence we throw our own below.
			throw new ClassCastException("Expected a response of type " + responseClass + " but got an instance of " + response.getClass().getName() + "!");
		}
	}

	/**
	 * Delegate of the {@link #query(Class, Request)} method. Subclasses should implement this method instead of <code>query(...)</code>.
	 *
	 * @param responseClass the type of the expected response; can be null, if you expect to receive null (i.e. you pass a "void" request).
	 * @param request the request to be sent to the key-manager.
	 * @return the response from the key-manager. Will be <code>null</code>, if the key-manager replied with a {@link NullResponse}.
	 * @throws TimeoutException if the request was not replied within the {@link #SYSTEM_PROPERTY_QUERY_TIMEOUT query-timeout}.
	 * @throws ErrorResponseException if the key-manager (either running embedded on the remote client or
	 * in a separate key-server) sent an {@link ErrorResponse}.
	 */
	protected abstract Response _query(Class<? extends Response> responseClass, Request request)
	throws TimeoutException, ErrorResponseException;

	@Override
	public final Request pollRequest(String cryptoSessionIDPrefix)
	{
		if (cryptoSessionIDPrefix == null)
			throw new IllegalArgumentException("cryptoSessionIDPrefix == null");

		return _pollRequest(cryptoSessionIDPrefix);
	}

	/**
	 * Delegate of the {@link #pollRequest(String)} method. Subclasses should implement this method instead of <code>pollRequest(...)</code>.
	 *
	 * @param cryptoSessionIDPrefix usually, every key-manager uses the same prefix for
	 * all crypto-sessions. Thus, this prefix is used to efficiently route requests to
	 * the right key-manager.
	 * @return the next request waiting for processing and fitting to the given <code>cryptoSessionIDPrefix</code>
	 * or <code>null</code>, if no such request pops up in the to-do-queue within the timeout.
	 */
	protected abstract Request _pollRequest(String cryptoSessionIDPrefix);

	@Override
	public final void pushResponse(Response response)
	{
		if (response == null)
			throw new IllegalArgumentException("response == null");

		if (response.getRequestID() == null)
			throw new IllegalArgumentException("response.requestID == null");

		_pushResponse(response);
	}

	/**
	 * Delegate of the {@link #pushResponse(Response)} method. Subclasses should implement this method instead of <code>pushResponse(...)</code>.
	 *
	 * @param response the response answering a previous {@link Request} enqueued by {@link #query(Class, Request)}.
	 */
	protected abstract void _pushResponse(Response response);

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
