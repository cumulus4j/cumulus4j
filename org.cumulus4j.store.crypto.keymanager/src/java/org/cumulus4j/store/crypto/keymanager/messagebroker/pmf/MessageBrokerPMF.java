package org.cumulus4j.store.crypto.keymanager.messagebroker.pmf;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.TimeoutException;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;
import org.cumulus4j.keymanager.back.shared.Message;
import org.cumulus4j.keymanager.back.shared.NullResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.keymanager.back.shared.SystemPropertyUtil;
import org.cumulus4j.store.crypto.keymanager.messagebroker.AbstractMessageBroker;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBroker;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBrokerRegistry;
import org.cumulus4j.store.crypto.keymanager.rest.ErrorResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * {@link PersistenceManagerFactory}-backed implementation of {@link MessageBroker}.
 * </p>
 * <p>
 * All {@link Message messages} are transferred via a shared database.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class MessageBrokerPMF extends AbstractMessageBroker
{
	private static final Logger logger = LoggerFactory.getLogger(MessageBrokerPMF.class);

	public static final String SYSTEM_PROPERTY_PERSISTENCE_PROPERTIES_PREFIX = "cumulus4j.MessageBrokerPMF.persistenceProperties.";

	private PersistenceManagerFactory pmf;

	private Random random = new Random();

	/**
	 * Create an instance of <code>MessageBrokerPMF</code>. You should not call this constructor directly, but
	 * instead use {@link MessageBrokerRegistry#getActiveMessageBroker()} to obtain the currently active {@link MessageBroker}.
	 */
	public MessageBrokerPMF()
	{
		logger.info("Instantiating MessageBrokerPMF.");
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
			if (key.startsWith(SYSTEM_PROPERTY_PERSISTENCE_PROPERTIES_PREFIX))
				propertiesRaw.setProperty(key.substring(SYSTEM_PROPERTY_PERSISTENCE_PROPERTIES_PREFIX.length()), String.valueOf(me.getValue()));
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

		logger.debug("query[requestID={}]: Entered with request: {}", requestID, request);

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

		logger.debug("query[requestID={}]: Request persisted.", requestID);

		// it would be nice if we could notify here, but this is not possible


//		// BEGIN trying to produce collisions.
//		try {
//			Thread.sleep(1000L);
//		} catch (InterruptedException e) {
//			// ignore - only log - and break loop.
//			logger.warn("query: Thread.sleep(...) was interrupted with an InterruptedException.");
//		}
//		// END trying to produce collisions.


		long beginTimestamp = System.currentTimeMillis();
		Response response = null;
		do {

			try {
				Thread.sleep(100L);
//				Thread.sleep(100L + random.nextInt(900)); // TODO make configurable?!
			} catch (InterruptedException e) {
				// ignore
			}

			logger.trace("query[requestID={}]: Beginning tx.", requestID);

			pm = createTransactionalPersistenceManager();
			try {
				// We now use optimistic tx, hence setSerializeRead makes no sense anymore.
//				pm.currentTransaction().setSerializeRead(true);

				pm.getFetchPlan().setGroups(new String[] { FetchPlan.DEFAULT, PendingRequest.FetchGroup.response });
				PendingRequest pendingRequest = PendingRequest.getPendingRequest(pm, requestID);
				if (pendingRequest == null)
					logger.warn("query[requestID={}]: Request is not found in the list of table of PendingRequest objects anymore.", requestID);
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

				if (response == null && System.currentTimeMillis() - beginTimestamp > getQueryTimeout()) {
					logger.warn(
							"query[requestID={}]: Request for session {} was not answered within timeout. Current status is {}.",
							new Object[] {
									requestID,
									(pendingRequest == null ? null : pendingRequest.getRequest().getCryptoSessionID()),
									(pendingRequest == null ? null : pendingRequest.getStatus())
							}
					);

					if (pendingRequest != null)
						pm.deletePersistent(pendingRequest);

					pm.currentTransaction().commit();

					throw new TimeoutException("Request was not answered within timeout! requestID=" + requestID);
				}

				pm.currentTransaction().commit();
			} catch (Exception x) {
				response = null;
				logger.warn("query[requestID={}]: {}", requestID, x.toString());
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();

				pm.close();
			}

			logger.trace("query[requestID={}]: Ended tx. response={}", requestID, response);

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

//	private Set<String> testCollisionDetection = Collections.synchronizedSet(new HashSet<String>());

	@Override
	public Request pollRequest(String cryptoSessionIDPrefix)
	{
		logger.debug("pollRequestForProcessing[cryptoSessionIDPrefix={}]: Entered.", cryptoSessionIDPrefix);

		long beginTimestamp = System.currentTimeMillis();
		Request request = null;
		do {
			logger.trace("pollRequestForProcessing[cryptoSessionIDPrefix={}]: Beginning tx.", cryptoSessionIDPrefix);

			PersistenceManager pm = createTransactionalPersistenceManager();
			try {
			// We now use optimistic tx, hence the following makes no sense anymore.
//				pm.currentTransaction().setSerializeRead(true);

				PendingRequest pendingRequest = PendingRequest.getOldestPendingRequest(
						pm, cryptoSessionIDPrefix, PendingRequestStatus.waitingForProcessing
				);


//				// BEGIN trying to produce collisions.
//				try {
//					Thread.sleep(500L);
//				} catch (InterruptedException e) {
//					// ignore - only log - and break loop.
//					logger.warn("query: Thread.sleep(...) was interrupted with an InterruptedException.");
//				}
//				// END trying to produce collisions.


				if (pendingRequest != null) {
					pendingRequest.setStatus(PendingRequestStatus.currentlyBeingProcessed);
					request = pendingRequest.getRequest();
				}

				pm.currentTransaction().commit();
			} catch (Exception x) {
				request = null;
				logger.warn("pollRequestForProcessing[cryptoSessionIDPrefix={}]: {}", cryptoSessionIDPrefix, x.toString());
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();

				pm.close();
			}

			logger.trace("pollRequestForProcessing[cryptoSessionIDPrefix={}]: Ended tx. request={}", cryptoSessionIDPrefix, request);

			if (request == null) {
				if (System.currentTimeMillis() - beginTimestamp > getPollRequestTimeout())
					break;

				try {
					Thread.sleep(50L + random.nextInt(50)); // TODO make configurable?!
				} catch (InterruptedException e) {
					// ignore - only log - and break loop.
					logger.warn("query: Thread.sleep(...) was interrupted with an InterruptedException.");
					break;
				}
			}
		} while (request == null);

//		if (request != null && !testCollisionDetection.add(request.getRequestID()))
//			logger.error("pollRequestForProcessing[cryptoSessionIDPrefix={}]: COLLISION!!! At least two threads process the same request! requestID={}", request.getRequestID());

		logger.debug("pollRequestForProcessing[cryptoSessionIDPrefix={}]: Returning request: {}", cryptoSessionIDPrefix, request);

		return request;
	}

	@Override
	public void pushResponse(Response response)
	{
		if (response == null)
			throw new IllegalArgumentException("response == null");

		if (response.getRequestID() == null)
			throw new IllegalArgumentException("response.requestID == null");

		String requestID = response.getRequestID();

		logger.debug("pushResponse[requestID={}]: Entered.", requestID);

		List<Throwable> errors = new LinkedList<Throwable>();
		boolean successful;
		for (int tryCounter = 0; tryCounter < 10; ++tryCounter) {
			successful = false;
			PersistenceManager pm = createTransactionalPersistenceManager();
			try {
//				pm.currentTransaction().setSerializeRead(true); // Now using optimistic TX instead.

				PendingRequest pendingRequest = PendingRequest.getPendingRequest(pm, response.getRequestID());
				if (pendingRequest == null || pendingRequest.getStatus() != PendingRequestStatus.currentlyBeingProcessed)
					logger.warn("pushResponse[requestID={}]: There is no request currently being processed with this requestID!!!", requestID);
				else {
					pendingRequest.setResponse(response);
					pendingRequest.setStatus(PendingRequestStatus.completed);
				}

				pm.currentTransaction().commit(); successful = true;
			} catch (Exception x) {
				errors.add(x);
				logger.warn("pushResponse[requestID={}]: {}", requestID, x.toString());
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();

				pm.close();
			}

			if (successful) {
				errors.clear();
				break;
			}
			else {
				// In case of an error, we wait a bit before trying it again.
				try {
					Thread.sleep(500L);
				} catch (InterruptedException e) {
					// ignore - only log - and break loop.
					logger.warn("pushResponse: Thread.sleep(...) was interrupted with an InterruptedException.");
					break;
				}
			}
		}

		if (!errors.isEmpty()) {
			Throwable lastError = null;
			for (Throwable e : errors) {
				lastError = e;
				logger.warn("pushResponse[requestID={}]: " + e, e);
			}
			if (lastError instanceof RuntimeException)
				throw (RuntimeException)lastError;
			else
				throw new RuntimeException(lastError);
		}
	}

}
