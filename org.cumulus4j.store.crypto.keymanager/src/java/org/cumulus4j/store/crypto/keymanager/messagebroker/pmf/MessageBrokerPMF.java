/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.store.crypto.keymanager.messagebroker.pmf;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

import javax.jdo.FetchPlan;
import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.IdentifierUtil;
import org.cumulus4j.keymanager.back.shared.Message;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.keymanager.back.shared.SystemPropertyUtil;
import org.cumulus4j.store.crypto.AbstractCryptoManager;
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
 * All {@link Message messages} are transferred via a shared database. Which database to be used can be
 * configured by {@link #SYSTEM_PROPERTY_PERSISTENCE_PROPERTIES_PREFIX system properties}.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class MessageBrokerPMF extends AbstractMessageBroker
{
	private static final Logger logger = LoggerFactory.getLogger(MessageBrokerPMF.class);

	/**
	 * Prefix for system properties used to configure the {@link PersistenceManagerFactory}.
	 * <p>
	 * Every system property that begins with {@value #SYSTEM_PROPERTY_PERSISTENCE_PROPERTIES_PREFIX}
	 * is passed (after truncating this prefix, of course) to the {@link JDOHelper#getPersistenceManagerFactory(Map)}.
	 * </p>
	 * <p>
	 * For example, to set the property "javax.jdo.option.ConnectionURL", you have to define the system
	 * property "cumulus4j.MessageBrokerPMF.persistenceProperties.javax.jdo.option.ConnectionURL".
	 * </p>
	 * <p>
	 * A set of defaults is loaded from a resource file, hence you do not need to configure everything, but
	 * without setting some basic coordinates (e.g. the JDBC URL), it is unlikely that your database server can be
	 * contacted. Of course, you could add an appropriate host record to your "/etc/hosts"
	 * and create a database with the name from our defaults on this host, but very likely you want to override these default
	 * coordinates:
	 * </p>
	 * <ul>
	 * <li>javax.jdo.option.ConnectionDriverName=com.mysql.jdbc.Driver</li>
	 * <li>javax.jdo.option.ConnectionURL=jdbc:mysql://cumulus4j-db/cumulus4jmessagebroker</li>
	 * </ul>
	 * <p>
	 * These defaults might be changed with a future version.
	 * </p>
	 */
	public static final String SYSTEM_PROPERTY_PERSISTENCE_PROPERTIES_PREFIX = "cumulus4j.MessageBrokerPMF.persistenceProperties.";

	/**
	 * <p>
	 * System property to control when the timer for cleaning up old {@link PendingRequest}s is called. The
	 * value configured here is a period, i.e. the timer will be triggered every X ms (roughly).
	 * </p><p>
	 * If this system property is not present (or not a valid number), the default is 3600000 (1 hour), which means
	 * the timer will wake up once every hour and call {@link #removeExpiredPendingRequests(boolean)} with <code>force = true</code>.
	 * </p><p>
	 * All <code>PendingRequest</code>s with a {@link PendingRequest#getLastStatusChangeTimestamp() lastStatusChangeTimestamp}
	 * being older than the {@link AbstractMessageBroker#getQueryTimeout() queryTimeout} (plus a safety margin of currently
	 * this period) are deleted.
	 * </p>
	 * @see #SYSTEM_PROPERTY_CLEANUP_TIMER_ENABLED
	 */
	public static final String SYSTEM_PROPERTY_CLEANUP_TIMER_PERIOD = "cumulus4j.MessageBrokerPMF.cleanupTimer.period";

	/**
	 * <p>
	 * System property to control whether the timer for cleaning up old {@link PendingRequest}s should be enabled. The
	 * value configured here is a boolean value, i.e. it can be "true" or "false".
	 * </p><p>
	 * If it is disabled, the "normal" threads will do the clean-up-work periodically, when they run through
	 * {@link #_query(Class, Request)} or {@link #_pollRequest(String)}.
	 * </p>
	 * @see #SYSTEM_PROPERTY_CLEANUP_TIMER_PERIOD
	 */
	public static final String SYSTEM_PROPERTY_CLEANUP_TIMER_ENABLED = "cumulus4j.MessageBrokerPMF.cleanupTimer.enabled";

	private long cleanupTimerPeriod = Long.MIN_VALUE;

	private Boolean cleanupTimerEnabled = null;

	protected long getCleanupTimerPeriod()
	{
		if (cleanupTimerPeriod < 0) {
			final String propName = SYSTEM_PROPERTY_CLEANUP_TIMER_PERIOD;
			String property = System.getProperty(propName);
			long timeout = -1;
			if (property != null && !property.isEmpty()) {
				try {
					timeout = Long.parseLong(property);
				} catch (NumberFormatException x) {
					logger.warn("Value \"{}\" of system property '{}' is not valid, because it cannot be parsed as number!", property, propName);
				}
				if (timeout <= 0)
					logger.warn("Value \"{}\" of system property '{}' is not valid, because it is less than or equal to 0!", property, propName);
				else {
					logger.info("System property '{}' is specified with value {}.", propName, timeout);
					cleanupTimerPeriod = timeout;
				}
			}

			if (cleanupTimerPeriod < 0) {
				timeout = 60L * 60L * 1000L;
				cleanupTimerPeriod = timeout;
				logger.info("System property '{}' is not specified; using default value {}.", propName, timeout);
			}
		}

		return cleanupTimerPeriod;
	}

	/**
	 * <p>
	 * Get the enabled status of the timer used to cleanup.
	 * </p>
	 * <p>
	 * This value can be configured using the system property {@value #SYSTEM_PROPERTY_CLEANUP_TIMER_ENABLED}.
	 * </p>
	 *
	 * @return the enabled status.
	 * @see #SYSTEM_PROPERTY_CLEANUP_TIMER_PERIOD
	 * @see #SYSTEM_PROPERTY_CLEANUP_TIMER_ENABLED
	 */
	protected boolean getCleanupTimerEnabled()
	{
		Boolean val = cleanupTimerEnabled;
		if (val == null) {
			String propName = SYSTEM_PROPERTY_CLEANUP_TIMER_ENABLED;
			String propVal = System.getProperty(propName);
			propVal = propVal == null ? null : propVal.trim();
			if (propVal != null && !propVal.isEmpty()) {
				if (propVal.equalsIgnoreCase(Boolean.TRUE.toString()))
					val = Boolean.TRUE;
				else if (propVal.equalsIgnoreCase(Boolean.FALSE.toString()))
					val = Boolean.FALSE;

				if (val == null)
					logger.warn("System property '{}' is set to '{}', which is an ILLEGAL value. Falling back to default value.", propName, propVal);
				else
					logger.info("System property '{}' is set to '{}'.", propName, val);
			}

			if (val == null) {
				val = Boolean.TRUE;
				logger.info("System property '{}' is not set. Using default value '{}'.", propName, val);
			}

			cleanupTimerEnabled = val;
		}
		return val;
	}

	private static volatile Timer cleanupTimer = null;
	private static volatile boolean cleanupTimerInitialised = false;
	private volatile boolean cleanupTaskInitialised = false;

	private static class CleanupTask extends TimerTask
	{
		private final Logger logger = LoggerFactory.getLogger(CleanupTask.class);

		private WeakReference<MessageBrokerPMF> messageBrokerPMFRef;
		private final long expiryTimerPeriodMSec;

		public CleanupTask(MessageBrokerPMF messageBrokerPMF, long expiryTimerPeriodMSec)
		{
			if (messageBrokerPMF == null)
				throw new IllegalArgumentException("messageBrokerPMF == null");

			this.messageBrokerPMFRef = new WeakReference<MessageBrokerPMF>(messageBrokerPMF);
			this.expiryTimerPeriodMSec = expiryTimerPeriodMSec;
		}

		@Override
		public void run() {
			try {
				logger.debug("run: entered");
				final MessageBrokerPMF messageBrokerPMF = messageBrokerPMFRef.get();
				if (messageBrokerPMF == null) {
					logger.info("run: MessageBrokerPMF was garbage-collected. Cancelling this TimerTask.");
					this.cancel();
					return;
				}

				messageBrokerPMF.removeExpiredPendingRequests(true);

				long currentPeriodMSec = messageBrokerPMF.getCleanupTimerPeriod();
				if (currentPeriodMSec != expiryTimerPeriodMSec) {
					logger.info(
							"run: The expiryTimerPeriodMSec changed (oldValue={}, newValue={}). Re-scheduling this task.",
							expiryTimerPeriodMSec, currentPeriodMSec
					);
					this.cancel();

					cleanupTimer.schedule(new CleanupTask(messageBrokerPMF, currentPeriodMSec), currentPeriodMSec, currentPeriodMSec);
				}
			} catch (Throwable x) {
				// The TimerThread is cancelled, if a task throws an exception. Furthermore, they are not logged at all.
				// Since we do not want the TimerThread to die, we catch everything (Throwable - not only Exception) and log
				// it here. IMHO there's nothing better we can do. Marco :-)
				logger.error("run: " + x, x);
			}
		}
	};

	private final void initTimerTaskOrRemoveExpiredPendingRequestsPeriodically()
	{
		if (!cleanupTimerInitialised) {
			synchronized (AbstractCryptoManager.class) {
				if (!cleanupTimerInitialised) {
					if (getCleanupTimerEnabled())
						cleanupTimer = new Timer();

					cleanupTimerInitialised = true;
				}
			}
		}

		if (!cleanupTaskInitialised) {
			synchronized (this) {
				if (!cleanupTaskInitialised) {
					if (cleanupTimer != null) {
						long periodMSec = getCleanupTimerPeriod();
						cleanupTimer.schedule(new CleanupTask(this, periodMSec), periodMSec, periodMSec);
					}
					cleanupTaskInitialised = true;
				}
			}
		}

		if (cleanupTimer == null) {
			logger.trace("initTimerTaskOrRemoveExpiredPendingRequestsPeriodically: No timer enabled => calling removeExpiredEntries(false) now.");
			removeExpiredPendingRequests(false);
		}
	}

	private Date lastRemoveExpiredPendingRequestsTimestamp = null;

	private void removeExpiredPendingRequests(boolean force)
	{
		synchronized (this) {
			if (
					!force && (
							lastRemoveExpiredPendingRequestsTimestamp != null &&
							lastRemoveExpiredPendingRequestsTimestamp.after(new Date(System.currentTimeMillis() - getCleanupTimerPeriod()))
					)
			)
			{
				logger.trace("removeExpiredPendingRequests: force == false and period not yet elapsed. Skipping.");
				return;
			}

			lastRemoveExpiredPendingRequestsTimestamp = new Date();
		}

		Date removePendingRequestsBeforeThisTimestamp = new Date(
				System.currentTimeMillis() - getQueryTimeout()
				// We use this cleanupTimerPeriod as a margin to prevent collisions with the code that still uses a PendingRequest
				// and might right now (after the query-timeout) be about to delete it. Even though this time might thus
				// be pretty long, it doesn't matter, if entries linger in the DB for a while as most are immediately cleaned up, anyway.
				// This cleanup is only required for rare situations (e.g. when a JVM crashes). Otherwise our code should already
				// ensure that objects are deleted immediately when they're not needed anymore.
				// We might in the future replace the 'getCleanupTimerPeriod()' by a new system-property-controllable
				// value (e.g. 'getCleanupDelay()'), though, to make it really nice & clean. But that's not important at all, IMHO.
				// Marco :-)
				- getCleanupTimerPeriod()
		);

		try {

			Integer deletedCount = null;

			PersistenceManager pm = createTransactionalPersistenceManager();
			try {
				Collection<PendingRequest> c = PendingRequest.getPendingRequestsWithLastStatusChangeTimestampOlderThanTimestamp(
						pm, removePendingRequestsBeforeThisTimestamp
				);

				if (logger.isDebugEnabled())
					deletedCount = c.size();

				pm.deletePersistentAll(c);

				pm.currentTransaction().commit();
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();

				pm.close();
			}

			logger.debug("removeExpiredPendingRequests: Deleted {} expired PendingRequest instances.", deletedCount);

		} catch (Exception x) {
			String errMsg = "removeExpiredPendingRequests: Deleting the expired pending requests failed. This might *occasionally* happen due to the optimistic transaction handling (=> collisions). ";
			if (logger.isDebugEnabled())
				logger.warn(errMsg + x, x);
			else
				logger.warn(errMsg + "Enable DEBUG logging to see the stack trace. " + x);
		}
	}

	private PersistenceManagerFactory pmf;

	private Random random = new Random();

	private final String thisID = Long.toString(System.identityHashCode(this), 36);

	/**
	 * Create an instance of <code>MessageBrokerPMF</code>. You should not call this constructor directly, but
	 * instead use {@link MessageBrokerRegistry#getActiveMessageBroker()} to obtain the currently active {@link MessageBroker}.
	 */
	public MessageBrokerPMF()
	{
		logger.info("[{}] Instantiating MessageBrokerPMF.", thisID);
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

		logger.info("[{}] javax.jdo.option.ConnectionDriverName={}", thisID, properties.get("javax.jdo.option.ConnectionDriverName"));
		logger.info("[{}] javax.jdo.option.ConnectionURL={}", thisID, properties.get("javax.jdo.option.ConnectionURL"));

		pmf = JDOHelper.getPersistenceManagerFactory(properties);
		// First create the structure in a separate tx (in case, the underlying DB/configuration requires this.
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

		// Now test the DB access.
		pm = pmf.getPersistenceManager();
		try {
			pm.currentTransaction().begin();
			// Testing WRITE and READ access.
			String cryptoSessionIDPrefix = IdentifierUtil.createRandomID(50); // using a length that is not used normally to prevent collisions with absolute certainty.
			String cryptoSessionID = cryptoSessionIDPrefix + '.' + IdentifierUtil.createRandomID(10);
			GetKeyRequest dummyRequest = new GetKeyRequest(cryptoSessionID, 1, "RSA", new byte[16]);
			PendingRequest pendingRequest = new PendingRequest(dummyRequest);
			pendingRequest = pm.makePersistent(pendingRequest);
			pm.flush(); // Make sure, things are written NOW.

			PendingRequest queriedPendingRequest = PendingRequest.getOldestPendingRequest(pm, cryptoSessionIDPrefix, PendingRequestStatus.waitingForProcessing);
			if (!pendingRequest.equals(queriedPendingRequest))
				throw new IllegalStateException("Query did not find the PendingRequest instance, we just persisted for testing!");

			// And delete the garbage immediately again.
			pm.deletePersistent(pendingRequest);

			pm.currentTransaction().commit();
		} finally {
			if (pm.currentTransaction().isActive())
				pm.currentTransaction().rollback();

			pm.close();
		}
		logger.info("[{}] Successfully instantiated and tested MessageBrokerPMF.", thisID);
	}

	protected PersistenceManager createTransactionalPersistenceManager()
	{
		PersistenceManager pm = pmf.getPersistenceManager();
		pm.currentTransaction().begin();
		return pm;
	}

	@Override
	protected Response _query(Class<? extends Response> responseClass, Request request)
	throws TimeoutException, ErrorResponseException
	{
		String requestID = request.getRequestID();

		logger.debug("_query[requestID={}]: Entered with request: {}", requestID, request);

		initTimerTaskOrRemoveExpiredPendingRequestsPeriodically();

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

		logger.debug("_query[requestID={}]: Request persisted.", requestID);

		// it would be nice if we could notify here, but this is not possible


//		// BEGIN trying to produce collisions.
//		try {
//			Thread.sleep(1000L);
//		} catch (InterruptedException e) {
//			// ignore - only log - and break loop.
//			logger.warn("_query: Thread.sleep(...) was interrupted with an InterruptedException.");
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

			logger.trace("_query[requestID={}]: Beginning tx.", requestID);

			pm = createTransactionalPersistenceManager();
			try {
				// We now use optimistic tx, hence setSerializeRead makes no sense anymore.
//				pm.currentTransaction().setSerializeRead(true);

				pm.getFetchPlan().setGroups(new String[] { FetchPlan.DEFAULT, PendingRequest.FetchGroup.response });
				PendingRequest pendingRequest = PendingRequest.getPendingRequest(pm, requestID);
				if (pendingRequest == null)
					logger.warn("_query[requestID={}]: Request is not found in the list of table of PendingRequest objects anymore.", requestID);
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
							"_query[requestID={}]: Request for session {} was not answered within timeout. Current status is {}.",
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
				logger.warn("_query[requestID={}]: {}", requestID, x.toString());
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();

				pm.close();
			}

			logger.trace("_query[requestID={}]: Ended tx. response={}", requestID, response);

		} while (response == null);

		return response;
	}

//	private Set<String> testCollisionDetection = Collections.synchronizedSet(new HashSet<String>());

	@Override
	protected Request _pollRequest(String cryptoSessionIDPrefix)
	{
		logger.debug("_pollRequest[cryptoSessionIDPrefix={}]: Entered.", cryptoSessionIDPrefix);

		long beginTimestamp = System.currentTimeMillis();

		initTimerTaskOrRemoveExpiredPendingRequestsPeriodically();

		Request request = null;
		do {
			logger.trace("_pollRequest[cryptoSessionIDPrefix={}]: Beginning tx.", cryptoSessionIDPrefix);

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
//					logger.warn("_pollRequest[cryptoSessionIDPrefix={}]: Thread.sleep(...) was interrupted with an InterruptedException.");
//				}
//				// END trying to produce collisions.


				if (pendingRequest != null) {
					pendingRequest.setStatus(PendingRequestStatus.currentlyBeingProcessed);
					request = pendingRequest.getRequest();
				}

				pm.currentTransaction().commit();
			} catch (Exception x) {
				request = null;
				logger.warn("_pollRequest[cryptoSessionIDPrefix={}]: {}", cryptoSessionIDPrefix, x.toString());
			} finally {
				if (pm.currentTransaction().isActive())
					pm.currentTransaction().rollback();

				pm.close();
			}

			logger.trace("_pollRequest[cryptoSessionIDPrefix={}]: Ended tx. request={}", cryptoSessionIDPrefix, request);

			if (request == null) {
				if (System.currentTimeMillis() - beginTimestamp > getPollRequestTimeout())
					break;

				try {
					Thread.sleep(50L + random.nextInt(50)); // TODO make configurable?!
				} catch (InterruptedException e) {
					// ignore - only log - and break loop.
					logger.warn("_pollRequest[cryptoSessionIDPrefix={}]: Thread.sleep(...) was interrupted with an InterruptedException.");
					break;
				}
			}
		} while (request == null);

//		if (request != null && !testCollisionDetection.add(request.getRequestID()))
//			logger.error("_pollRequest[cryptoSessionIDPrefix={}]: COLLISION!!! At least two threads process the same request! requestID={}", request.getRequestID());

		logger.debug("_pollRequest[cryptoSessionIDPrefix={}]: Returning request: {}", cryptoSessionIDPrefix, request);

		return request;
	}

	@Override
	protected void _pushResponse(Response response)
	{
		if (response == null)
			throw new IllegalArgumentException("response == null");

		if (response.getRequestID() == null)
			throw new IllegalArgumentException("response.requestID == null");

		String requestID = response.getRequestID();

		logger.debug("_pushResponse[requestID={}]: Entered.", requestID);

		List<Throwable> errors = new LinkedList<Throwable>();
		boolean successful;
		for (int tryCounter = 0; tryCounter < 10; ++tryCounter) {
			successful = false;
			PersistenceManager pm = createTransactionalPersistenceManager();
			try {
//				pm.currentTransaction().setSerializeRead(true); // Now using optimistic TX instead.

				PendingRequest pendingRequest = PendingRequest.getPendingRequest(pm, response.getRequestID());
				if (pendingRequest == null || pendingRequest.getStatus() != PendingRequestStatus.currentlyBeingProcessed)
					logger.warn("_pushResponse[requestID={}]: There is no request currently being processed with this requestID!!!", requestID);
				else {
					pendingRequest.setResponse(response);
					pendingRequest.setStatus(PendingRequestStatus.completed);
				}

				pm.currentTransaction().commit(); successful = true;
			} catch (Exception x) {
				errors.add(x);
				logger.warn("_pushResponse[requestID={}]: {}", requestID, x.toString());
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
					logger.warn("_pushResponse[requestID={}]: Thread.sleep(...) was interrupted with an InterruptedException.", requestID);
					break;
				}
			}
		}

		if (!errors.isEmpty()) {
			Throwable lastError = null;
			for (Throwable e : errors) {
				lastError = e;
				logger.warn("_pushResponse[requestID=" + requestID + "]: " + e, e);
			}
			if (lastError instanceof RuntimeException)
				throw (RuntimeException)lastError;
			else
				throw new RuntimeException(lastError);
		}
	}

}
