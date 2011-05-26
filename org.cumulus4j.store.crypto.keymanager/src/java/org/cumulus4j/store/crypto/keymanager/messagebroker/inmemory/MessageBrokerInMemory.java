package org.cumulus4j.store.crypto.keymanager.messagebroker.inmemory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;
import org.cumulus4j.keymanager.back.shared.NullResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.store.crypto.keymanager.messagebroker.AbstractMessageBroker;
import org.cumulus4j.store.crypto.keymanager.rest.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Implementation of {@link org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBroker MessageBroker} which
 * works only in a single JVM. It manages all messages in-memory.
 * </p>
 * <p>
 * <b>Important:</b> This implementation can usually not be used in a cluster! It is only cluster-able, if you use transparent
 * JVM-clustering, e.g. with <a href="http://www.terracotta.org/">Terracotta</a>!
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class MessageBrokerInMemory
extends AbstractMessageBroker
{
	private static final Logger logger = LoggerFactory.getLogger(MessageBrokerInMemory.class);

	private ConcurrentHashMap<String, ConcurrentLinkedQueue<Request>> cryptoSessionIDPrefix2requestsWaitingForProcessing = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Request>>();
	private ConcurrentHashMap<String, Request> requestID2requestCurrentlyBeingProcessed = new ConcurrentHashMap<String, Request>();

	/**
	 * When a request was completed and a response returned, both are stored together here.
	 */
	private ConcurrentHashMap<Request, Response> request2response = new ConcurrentHashMap<Request, Response>();

	private ConcurrentLinkedQueue<Request> getRequestsWaitingForProcessing(String cryptoSessionIDPrefix)
	{
		if (cryptoSessionIDPrefix == null)
			throw new IllegalArgumentException("cryptoSessionIDPrefix == null");

		ConcurrentLinkedQueue<Request> requestsWaitingForProcessing = cryptoSessionIDPrefix2requestsWaitingForProcessing.get(cryptoSessionIDPrefix);
		if (requestsWaitingForProcessing == null) {
			requestsWaitingForProcessing = new ConcurrentLinkedQueue<Request>();
			cryptoSessionIDPrefix2requestsWaitingForProcessing.putIfAbsent(cryptoSessionIDPrefix, requestsWaitingForProcessing);
			requestsWaitingForProcessing = cryptoSessionIDPrefix2requestsWaitingForProcessing.get(cryptoSessionIDPrefix);
		}
		return requestsWaitingForProcessing;
	}

	public MessageBrokerInMemory() {
		// TODO We should try to find out if we run in a Terracotta-environment (e.g. check a system property, if available) and
		// throw an exception here, if we are not running with Terracotta. For now, I always log a warning. Marco :-)
		logger.warn("MessageBrokerInMemory instantiated. This implementation is NOT cluster-able without Terracotta! You MUST NOT use it, if you do not have transparent JVM-clustering present!");
	}

	@Override
	public <R extends Response> R query(Class<R> responseClass, Request request)
	throws TimeoutException, ErrorResponseException
	{
		return query(responseClass, request, getQueryTimeout());
	}

	protected <R extends Response> R query(Class<R> responseClass, Request request, long queryTimeout)
	throws TimeoutException, ErrorResponseException
	{
		ConcurrentLinkedQueue<Request> requestsWaitingForProcessing = getRequestsWaitingForProcessing(request.getCryptoSessionIDPrefix());
		requestsWaitingForProcessing.add(request); // This is a thread-safe object, hence add BEFORE synchronizing.

		synchronized (requestsWaitingForProcessing) { // synchronized only necessary for notification.
			requestsWaitingForProcessing.notify();
		}

		long beginTimestamp = System.currentTimeMillis();
		Response response;
		do {

			synchronized(request2response) {
				try {
					request2response.wait(10000L);
				} catch (InterruptedException e) {
					// ignore - only log.
					logger.warn("query: request2response.wait(...) was interrupted with an InterruptedException.");
				}
			}

			response = request2response.remove(request);

			if (response == null && System.currentTimeMillis() - beginTimestamp > queryTimeout) {
				logger.warn("query: Request {} for session {} was not answered within timeout.", request.getRequestID(), request.getCryptoSessionID());

				boolean removed = requestsWaitingForProcessing.remove(request);
				if (removed)
					logger.warn("query: Request {} for session {} was still in 'requestsWaitingForProcessing'.", request.getRequestID(), request.getCryptoSessionID());

				Request removedRequest = requestID2requestCurrentlyBeingProcessed.remove(request.getRequestID());
				if (removedRequest != null)
					logger.warn("query: Request {} for session {} was in 'requestID2requestCurrentlyBeingProcessed'.", request.getRequestID(), request.getCryptoSessionID());

				throw new TimeoutException("Request was not answered within timeout: " + request);
			}

		} while (response == null);

		// A NullResponse which has a requestID assigned is forwarded to the requester and must be transformed into null here.
		if (response instanceof NullResponse)
			return null;

		if (response instanceof ErrorResponse)
			throw new ErrorResponseException((ErrorResponse)response);

		try {
			return responseClass.cast(response);
		} catch (ClassCastException x) { // this exception has no nice message (according to source code), hence we throw our own below.
			throw new ClassCastException("Expected a response of type " + responseClass + " but got an instance of " + response.getClass().getName() + "!");
		}
	}

	@Override
	public Request pollRequest(String cryptoSessionIDPrefix)
	{
		return pollRequest(cryptoSessionIDPrefix, getPollRequestTimeout());
	}

	protected Request pollRequest(String cryptoSessionIDPrefix, long pollRequestTimeout)
	{
		ConcurrentLinkedQueue<Request> requestsWaitingForProcessing = getRequestsWaitingForProcessing(cryptoSessionIDPrefix);

		long beginTimestamp = System.currentTimeMillis();
		Request request;
		do {
			request = requestsWaitingForProcessing.poll();

			if (request == null) {
				if (System.currentTimeMillis() - beginTimestamp > pollRequestTimeout)
					break;

				synchronized(requestsWaitingForProcessing) {
					try {
						requestsWaitingForProcessing.wait(10000L);
					} catch (InterruptedException e) {
						// ignore - only log - and break loop.
						logger.warn("query: requestsWaitingForProcessing.wait(...) was interrupted with an InterruptedException.");
						break;
					}
				}
			}
		} while (request == null);

		if (request != null) {
			requestID2requestCurrentlyBeingProcessed.put(request.getRequestID(), request);
		}

		return request;
	}

	@Override
	public void pushResponse(Response response)
	{
		if (response == null)
			throw new IllegalArgumentException("response == null");

		if (response.getRequestID() == null)
			throw new IllegalArgumentException("response.requestID == null");

		Request request = requestID2requestCurrentlyBeingProcessed.remove(response.getRequestID());
		if (request == null) {
			logger.warn("pushResponse: There is no request currently being processed with requestID={}!!!", response.getRequestID());
		}
		else {
			request2response.put(request, response); // thread-safe instance => put BEFORE synchronized block
			synchronized (request2response) { // synchronized block only necessary for notification.
				request2response.notifyAll();
			}
		}
	}
}
