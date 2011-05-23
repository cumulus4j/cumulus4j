package org.cumulus4j.store.crypto.keymanager.rest.messagebrokerpmf;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;
import org.cumulus4j.keymanager.back.shared.NullResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.keymanager.back.shared.SystemPropertyUtil;
import org.cumulus4j.store.crypto.keymanager.rest.ErrorResponseException;
import org.cumulus4j.store.crypto.keymanager.rest.MessageBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class MessageBrokerPMF extends MessageBroker
{
	private static final Logger logger = LoggerFactory.getLogger(MessageBrokerPMF.class);

	public static final String SYSTEM_PROPERTY_MESSAGE_BROKER_PMF_CONFIG_PREFIX = "cumulus4j.keymanager.messagebroker.";

	private PersistenceManagerFactory pmf;

	public MessageBrokerPMF()
	{
		Properties propertiesRaw = new Properties();
		InputStream in = MessageBrokerPMF.class.getResourceAsStream("messagebroker-datanucleus.properties");
		try {
			propertiesRaw.load(in);
			in.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		for (Map.Entry<?, ?> me : System.getProperties().entrySet()) {
			String key = String.valueOf(me.getKey());
			if (key.startsWith(SYSTEM_PROPERTY_MESSAGE_BROKER_PMF_CONFIG_PREFIX))
				propertiesRaw.setProperty(key, String.valueOf(me.getValue()));
		}

		Map<String, Object> properties = new HashMap<String, Object>(propertiesRaw.size());
		for (Map.Entry<?, ?> me : propertiesRaw.entrySet())
			properties.put(String.valueOf(me.getKey()), SystemPropertyUtil.resolveSystemProperties(String.valueOf(me.getValue())));

		pmf = JDOHelper.getPersistenceManagerFactory(properties);
		PersistenceManager pm = pmf.getPersistenceManager();
		try {
			pm.currentTransaction().begin();
			pm.getExtent(PendingRequest.class);
			pm.currentTransaction().commit();
		} finally {
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}
	}

	protected PersistenceManager createTransactionalPersistenceManager()
	{
		PersistenceManager pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
		return pm;
	}

	@Override
	public <R extends Response> R query(Class<R> responseClass, Request request)
	throws TimeoutException, ErrorResponseException
	{
		String requestID = request.getRequestID();
		PersistenceManager pm = createTransactionalPersistenceManager();
		try {
			pm.makePersistent(new PendingRequest(request));
			pm.currentTransaction().commit();
		} finally {
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}
		request = null;

		// it would be nice if we could notify here, but this is not possible

		long beginTimestamp = System.currentTimeMillis();
		Response response = null;
		do {

			try {
				Thread.sleep(1000L); // TODO make configurable?!
			} catch (InterruptedException e) {
				// ignore
			}

			pm = createTransactionalPersistenceManager();
			try {
				pm.currentTransaction().setSerializeRead(true);

				pm.getFetchPlan().setGroups(new String[] { FetchPlan.DEFAULT, PendingRequest.FetchGroup.response });
				PendingRequest pendingRequest = PendingRequest.getPendingRequest(pm, requestID);
				if (pendingRequest == null)
					logger.warn("query: Request {} is not found in the list of table of PendingRequest objects anymore.", requestID);
				else {
					switch (pendingRequest.getStatus()) {
						case waitingForProcessing:
							// nothing to do => wait!
							break;
						case currentlyBeingProcessed:
							// nothing to do => wait!
							break;
						case completed:
							response = pendingRequest.getResponse();
							if (response == null)
								throw new IllegalStateException("pending.response is null, even though status is 'completed'!!!");
							break;
						default:
							throw new IllegalStateException("Unknown status: " + pendingRequest.getStatus());
					}

					if (response != null)
						pm.deletePersistent(pendingRequest);
				}

				if (response == null && System.currentTimeMillis() - beginTimestamp > timeoutQuery) {
					logger.warn(
							"query: Request {} for session {} was not answered within timeout. Current status is {}.",
							new Object[] {
									requestID,
									(pendingRequest == null ? null : pendingRequest.getRequest().getCryptoSessionID()),
									(pendingRequest == null ? null : pendingRequest.getStatus())
							}
					);

					if (pendingRequest != null)
						pm.deletePersistent(pendingRequest);

					pm.currentTransaction().commit();

					throw new TimeoutException("Request was not answered within timeout: " + requestID);
				}

				pm.currentTransaction().commit();
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();

				pm.close();
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
	protected Request pollRequestForProcessing(String cryptoSessionIDPrefix)
	{
		long beginTimestamp = System.currentTimeMillis();
		Request request = null;
		do {
			PersistenceManager pm = createTransactionalPersistenceManager();
			try {
				pm.currentTransaction().setSerializeRead(true);

				PendingRequest pendingRequest = PendingRequest.getOldestPendingRequest(
						pm, cryptoSessionIDPrefix, PendingRequestStatus.waitingForProcessing
				);

				if (pendingRequest != null) {
					pendingRequest.setStatus(PendingRequestStatus.currentlyBeingProcessed);
					request = pendingRequest.getRequest();
				}

				pm.currentTransaction().commit();
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();

				pm.close();
			}

			if (request == null) {
				if (System.currentTimeMillis() - beginTimestamp > timeoutPollRequestForProcessing)
					break;

				try {
					Thread.sleep(1000L); // TODO make configurable?!
				} catch (InterruptedException e) {
					// ignore - only log - and break loop.
					logger.warn("query: requestsWaitingForProcessing.wait(...) was interrupted with an InterruptedException.");
					break;
				}
			}
		} while (request == null);

		return request;
	}

	@Override
	protected void pushResponse(Response response)
	{
		if (response == null)
			throw new IllegalArgumentException("response == null");

		if (response.getRequestID() == null)
			throw new IllegalArgumentException("response.requestID == null");

		PersistenceManager pm = createTransactionalPersistenceManager();
		try {
			pm.currentTransaction().setSerializeRead(true);

			PendingRequest pendingRequest = PendingRequest.getPendingRequest(pm, response.getRequestID());
			if (pendingRequest == null || pendingRequest.getStatus() != PendingRequestStatus.currentlyBeingProcessed)
				logger.warn("pushResponse: There is no request currently being processed with requestID={}!!!", response.getRequestID());
			else {
				pendingRequest.setResponse(response);
				pendingRequest.setStatus(PendingRequestStatus.completed);
			}

			pm.currentTransaction().commit();
		} finally {
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}
	}

}
