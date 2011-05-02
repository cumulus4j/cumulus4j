package org.cumulus4j.keyserver.back.plugin;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeoutException;

import org.cumulus4j.keyserver.back.shared.Request;
import org.cumulus4j.keyserver.back.shared.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class RequestResponseBroker
{
	private static final Logger logger = LoggerFactory.getLogger(RequestResponseBroker.class);

	private static RequestResponseBroker sharedInstance = new RequestResponseBroker();

	/**
	 * Get the singleton.
	 * @return the single shared instance.
	 */
	public static RequestResponseBroker sharedInstance() { return sharedInstance; }

	private ConcurrentHashMap<String, ConcurrentLinkedQueue<Request>> keyServerID2requestsWaitingForProcessing = new ConcurrentHashMap<String, ConcurrentLinkedQueue<Request>>();
	private ConcurrentHashMap<UUID, Request> requestID2requestCurrentlyBeingProcessed = new ConcurrentHashMap<UUID, Request>();

	/**
	 * When a request was completed and a response returned, both are stored together here.
	 */
	private ConcurrentHashMap<Request, Response> request2response = new ConcurrentHashMap<Request, Response>();

	private ConcurrentLinkedQueue<Request> getRequestsWaitingForProcessing(String keyServerID)
	{
		ConcurrentLinkedQueue<Request> requestsWaitingForProcessing = keyServerID2requestsWaitingForProcessing.get(keyServerID);
		if (requestsWaitingForProcessing == null) {
			requestsWaitingForProcessing = new ConcurrentLinkedQueue<Request>();
			keyServerID2requestsWaitingForProcessing.putIfAbsent(keyServerID, requestsWaitingForProcessing);
			requestsWaitingForProcessing = keyServerID2requestsWaitingForProcessing.get(keyServerID);
		}
		return requestsWaitingForProcessing;
	}

	/**
	 * The remote key server has to wait this long. Its HTTP client's timeout should thus be longer
	 * than this time!
	 */
	private long timeoutPollRequestForProcessing = 2L * 60L * 1000L;

	/**
	 * The local API client gets an exception, if the request was not processed &amp; answered within this timeout.
	 */
	private long timeoutQuery = 5L * 60L * 1000L;

	/**
	 * Send <code>request</code> to the key server and return its response.
	 * @param request the request to be sent to the key server.
	 * @return the response from the key server.
	 * @throws TimeoutException if the request was not replied within the query-timeout.
	 */
	public Response query(Request request)
	throws TimeoutException
	{
		ConcurrentLinkedQueue<Request> requestsWaitingForProcessing = getRequestsWaitingForProcessing(request.getKeyServerID());
		requestsWaitingForProcessing.add(request);
		requestsWaitingForProcessing.notify();

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

			if (response == null && System.currentTimeMillis() - beginTimestamp > timeoutQuery) {
				logger.warn("query: Request {} for KeyServer {} was not answered within timeout.", request.getRequestID(), request.getKeyServerID());

				boolean removed = requestsWaitingForProcessing.remove(request);
				if (removed)
					logger.warn("query: Request {} for KeyServer {} was still in 'requestsWaitingForProcessing'.", request.getRequestID(), request.getKeyServerID());

				Request removedRequest = requestID2requestCurrentlyBeingProcessed.remove(request.getRequestID());
				if (removedRequest != null)
					logger.warn("query: Request {} for KeyServer {} was in 'requestID2requestCurrentlyBeingProcessed'.", request.getRequestID(), request.getKeyServerID());

				throw new TimeoutException("Request was not answered within timeout: " + request);
			}

		} while (response == null);

		return response;
	}

	public Request pollRequestForProcessing(String keyServerID)
	{
		ConcurrentLinkedQueue<Request> requestsWaitingForProcessing = getRequestsWaitingForProcessing(keyServerID);

		long beginTimestamp = System.currentTimeMillis();
		Request request;
		do {
			request = requestsWaitingForProcessing.poll();

			if (request == null) {
				if (System.currentTimeMillis() - beginTimestamp > timeoutPollRequestForProcessing)
					break;

				synchronized(requestsWaitingForProcessing) {
					try {
						requestsWaitingForProcessing.wait(10000L);
					} catch (InterruptedException e) {
						// ignore - only log.
						logger.warn("query: requestsWaitingForProcessing.wait(...) was interrupted with an InterruptedException.");
					}
				}
			}
		} while (request == null);

		if (request != null) {
			requestID2requestCurrentlyBeingProcessed.put(request.getRequestID(), request);
		}

		return request;
	}

	public void pushResponse(Response response)
	{
		if (response == null)
			throw new IllegalArgumentException("response == null");

		Request request = requestID2requestCurrentlyBeingProcessed.remove(response.getRequestID());
		if (request == null) {
			logger.warn("pushResponse: There is no request currently being processed with requestID={}!!!", response.getRequestID());
		}
		else {
			request2response.put(request, response);
			request2response.notifyAll();
		}
	}
}
