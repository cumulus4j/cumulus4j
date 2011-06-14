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
//package org.cumulus4j.store.crypto.keymanager.messagebroker.httppmf;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Map;
//import java.util.Properties;
//import java.util.Set;
//import java.util.concurrent.TimeoutException;
//
//import javax.jdo.JDOHelper;
//import javax.jdo.PersistenceManager;
//import javax.jdo.PersistenceManagerFactory;
//
//import org.cumulus4j.keymanager.back.shared.IdentifierUtil;
//import org.cumulus4j.keymanager.back.shared.Request;
//import org.cumulus4j.keymanager.back.shared.Response;
//import org.cumulus4j.keymanager.back.shared.SystemPropertyUtil;
//import org.cumulus4j.store.crypto.keymanager.messagebroker.ActiveKeyManagerChannelRegistration;
//import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBrokerJVMSingleton;
//import org.cumulus4j.store.crypto.keymanager.rest.ErrorResponseException;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
// * @deprecated This is unfinished work - an experiment so to say - and probably will never be finished as this doesn't work in GAE anyway.
// */
//@Deprecated
//public class MessageBrokerHttpPmf extends MessageBrokerInMemory
//{
//	private static final Logger logger = LoggerFactory.getLogger(MessageBrokerHttpPmf.class);
//
//	public static final String SYSTEM_PROPERTY_MESSAGE_BROKER_PMF_CONFIG_PREFIX = "cumulus4j.MessageBrokerHttpPmf.persistenceProperties.";
//
//	private PersistenceManagerFactory pmf;
//
//	public MessageBrokerHttpPmf()
//	{
//		Properties propertiesRaw = new Properties();
//		InputStream in = MessageBrokerHttpPmf.class.getResourceAsStream("messagebroker-datanucleus.properties");
//		try {
//			propertiesRaw.load(in);
//			in.close();
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//
//		for (Map.Entry<?, ?> me : System.getProperties().entrySet()) {
//			String key = String.valueOf(me.getKey());
//			if (key.startsWith(SYSTEM_PROPERTY_MESSAGE_BROKER_PMF_CONFIG_PREFIX))
//				propertiesRaw.setProperty(key, String.valueOf(me.getValue()));
//		}
//
//		Map<String, Object> properties = new HashMap<String, Object>(propertiesRaw.size());
//		for (Map.Entry<?, ?> me : propertiesRaw.entrySet())
//			properties.put(String.valueOf(me.getKey()), SystemPropertyUtil.resolveSystemProperties(String.valueOf(me.getValue())));
//
//		pmf = JDOHelper.getPersistenceManagerFactory(properties);
//		PersistenceManager pm = pmf.getPersistenceManager();
//		try {
//			pm.currentTransaction().begin();
//			pm.getExtent(ActiveKeyManagerChannel.class);
//			pm.currentTransaction().commit();
//		} finally {
//			if (pm.currentTransaction().isActive())
//				pm.currentTransaction().rollback();
//
//			pm.close();
//		}
//	}
//
//	protected PersistenceManager createTransactionalPersistenceManager()
//	{
//		PersistenceManager pm = pmf.getPersistenceManager();
//		pm.currentTransaction().begin();
//		return pm;
//	}
//
//	private String clusterNodeID;
//
//	/**
//	 * For performance reasons we suppress the re-persisting of an {@link ActiveKeyManagerChannel}
//	 * when its {@link ActiveKeyManagerChannel#getRegistrationTimestamp() registrationTimestamp} is newer than this age
//	 * in millisec.
//	 */
//	private long suppressRepersistActiveKeyManagerChannelMSec = 3L * 60L * 1000L; // 3 minutes - TODO make configurable
//
//	/**
//	 * After which time in millisec is an {@link ActiveKeyManagerChannel} considered 'expired'.
//	 * In other words, when to automatically delete instances of ActiveKeyManagerChannel.
//	 * This must be considerably longer than {@link #suppressRepersistActiveKeyManagerChannelMSec}!!!
//	 */
//	private long expirePersistentActiveKeyManagerChannelMSec = 15L * 60L * 1000L; // 15 minutes - TODO make configurable
//
//	private Set<ActiveKeyManagerChannelRegistration> activeKeyManagerChannelRegistrations = new HashSet<ActiveKeyManagerChannelRegistration>();
//	private Map<String, Integer> cryptoSessionIDPrefix2activeKeyManagerChannelRegistrationCount = new HashMap<String, Integer>();
//
//	@Override
//	public ActiveKeyManagerChannelRegistration registerActiveKeyManagerChannel(String cryptoSessionIDPrefix, String internalKeyManagerChannelURL)
//	{
//		if (cryptoSessionIDPrefix == null)
//			throw new IllegalArgumentException("cryptoSessionIDPrefix == null");
//
//		if (internalKeyManagerChannelURL == null)
//			throw new IllegalArgumentException("internalKeyManagerChannelURL == null");
//
//		ActiveKeyManagerChannelRegistration registration;
//		synchronized (this) {
//			if (clusterNodeID == null)
//				clusterNodeID = internalKeyManagerChannelURL.replaceAll("^[^:]*://([^/]*)/.*$", "$1") + ':' + IdentifierUtil.createRandomID(10);
//
//			registration = new ActiveKeyManagerChannelRegistration(clusterNodeID, cryptoSessionIDPrefix);
//			activeKeyManagerChannelRegistrations.add(registration);
//
//			Integer count = cryptoSessionIDPrefix2activeKeyManagerChannelRegistrationCount.get(cryptoSessionIDPrefix);
//			if (count == null)
//				count = 1;
//			else
//				count = count + 1;
//
//			cryptoSessionIDPrefix2activeKeyManagerChannelRegistrationCount.put(cryptoSessionIDPrefix, count);
//		}
//
//		PersistenceManager pm = createTransactionalPersistenceManager();
//		try {
//			ActiveKeyManagerChannel activeKeyManagerChannel = ActiveKeyManagerChannel.getActiveKeyManagerChannel(
//					pm, clusterNodeID, cryptoSessionIDPrefix
//			);
//
//			if (activeKeyManagerChannel == null) {
//				logger.debug(
//						"registerActiveKeyManagerChannel: Creating new ActiveKeyManagerChannel with clusterNodeID={} and cryptoSessionIDPrefix={}.",
//						clusterNodeID, cryptoSessionIDPrefix
//				);
//				activeKeyManagerChannel = new ActiveKeyManagerChannel(clusterNodeID, cryptoSessionIDPrefix);
//			}
//			else if (activeKeyManagerChannel.getRegistrationTimestamp().after(new Date(System.currentTimeMillis() - suppressRepersistActiveKeyManagerChannelMSec))) {
//				logger.debug(
//						"registerActiveKeyManagerChannel: ActiveKeyManagerChannel with clusterNodeID={} and cryptoSessionIDPrefix={} was registered in persistent storage at {} which is new enough to suppress re-persisting now.",
//						new Object[] { clusterNodeID, cryptoSessionIDPrefix, activeKeyManagerChannel.getRegistrationTimestamp() }
//				);
//				return registration;
//			}
//
//			activeKeyManagerChannel.setInternalKeyManagerChannelURL(internalKeyManagerChannelURL);
//			activeKeyManagerChannel.setRegistrationTimestamp(new Date());
//			activeKeyManagerChannel.setExpiryTimestamp(new Date(System.currentTimeMillis() + expirePersistentActiveKeyManagerChannelMSec));
//
//			pm.makePersistent(activeKeyManagerChannel); // In case it is not yet persistent, we do this now. If it already is, this is a no-op.
//
//			pm.currentTransaction().commit();
//		} finally {
//			if (pm.currentTransaction().isActive())
//				pm.currentTransaction().rollback();
//
//			pm.close();
//		}
//		return registration;
//	}
//
//	@Override
//	public void unregisterActiveKeyManagerChannel(ActiveKeyManagerChannelRegistration registration)
//	{
//		synchronized (this) {
//			if (!activeKeyManagerChannelRegistrations.remove(registration))
//				throw new IllegalStateException("This registration is unknown: " + registration);
//
//			String cryptoSessionIDPrefix = registration.getCryptoSessionIDPrefix();
//
//			Integer count = cryptoSessionIDPrefix2activeKeyManagerChannelRegistrationCount.get(cryptoSessionIDPrefix);
//			if (count == null)
//				throw new IllegalStateException("Why is there no count for this registration?! " + registration);
//
//			if (count.intValue() == 1)
//				cryptoSessionIDPrefix2activeKeyManagerChannelRegistrationCount.remove(cryptoSessionIDPrefix);
//			else {
//				count = count - 1;
//				cryptoSessionIDPrefix2activeKeyManagerChannelRegistrationCount.put(cryptoSessionIDPrefix, count);
//			}
//		}
//	}
//
//	@Override
//	protected <R extends Response> R query(Class<R> responseClass, Request request, long queryTimeoutMSec)
//	throws TimeoutException, ErrorResponseException
//	{
//		String cryptoSessionIDPrefix = request.getCryptoSessionIDPrefix();
//		long beginTimestamp = System.currentTimeMillis();
//		while (true) {
//			if (System.currentTimeMillis() - beginTimestamp > queryTimeoutMSec)
//				throw new TimeoutException("Request was not answered within timeout: " + request);
//
//			boolean tryLocally = true;
//			synchronized (this) {
//				Integer count = cryptoSessionIDPrefix2activeKeyManagerChannelRegistrationCount.get(cryptoSessionIDPrefix);
//				if (count == null)
//					tryLocally = false;
//			}
//
//			if (tryLocally) {
//				try {
//					R result = super.query(responseClass, request, 10L * 1000L);
//					return result;
//				} catch (TimeoutException x) {
//					logger.warn("query: Tried locally and got TimeoutException.");
//				}
//			}
//			else {
//				// search a proxy and delegate there
//
//
//			}
//		}
//	}
//
//
////	@Override
////	public <R extends Response> R query(Class<R> responseClass, Request request)
////	throws TimeoutException, ErrorResponseException
////	{
////		String requestID = request.getRequestID();
////		PersistenceManager pm = createTransactionalPersistenceManager();
////		try {
////			pm.makePersistent(new PendingRequest(request));
////			pm.currentTransaction().commit();
////		} finally {
////			if (pm.currentTransaction().isActive())
////				pm.currentTransaction().rollback();
////
////			pm.close();
////		}
////		request = null;
////
////		// it would be nice if we could notify here, but this is not possible
////
////		long beginTimestamp = System.currentTimeMillis();
////		Response response = null;
////		do {
////
////			try {
////				Thread.sleep(1000L); // TODO make configurable?!
////			} catch (InterruptedException e) {
////				// ignore
////			}
////
////			pm = createTransactionalPersistenceManager();
////			try {
////				pm.currentTransaction().setSerializeRead(true);
////
////				pm.getFetchPlan().setGroups(new String[] { FetchPlan.DEFAULT, PendingRequest.FetchGroup.response });
////				PendingRequest pendingRequest = PendingRequest.getPendingRequest(pm, requestID);
////				if (pendingRequest == null)
////					logger.warn("query: Request {} is not found in the list of table of PendingRequest objects anymore.", requestID);
////				else {
////					switch (pendingRequest.getStatus()) {
////						case waitingForProcessing:
////							// nothing to do => wait!
////							break;
////						case currentlyBeingProcessed:
////							// nothing to do => wait!
////							break;
////						case completed:
////							response = pendingRequest.getResponse();
////							if (response == null)
////								throw new IllegalStateException("pending.response is null, even though status is 'completed'!!!");
////							break;
////						default:
////							throw new IllegalStateException("Unknown status: " + pendingRequest.getStatus());
////					}
////
////					if (response != null)
////						pm.deletePersistent(pendingRequest);
////				}
////
////				if (response == null && System.currentTimeMillis() - beginTimestamp > timeoutQuery) {
////					logger.warn(
////							"query: Request {} for session {} was not answered within timeout. Current status is {}.",
////							new Object[] {
////									requestID,
////									(pendingRequest == null ? null : pendingRequest.getRequest().getCryptoSessionID()),
////									(pendingRequest == null ? null : pendingRequest.getStatus())
////							}
////					);
////
////					if (pendingRequest != null)
////						pm.deletePersistent(pendingRequest);
////
////					pm.currentTransaction().commit();
////
////					throw new TimeoutException("Request was not answered within timeout: " + requestID);
////				}
////
////				pm.currentTransaction().commit();
////			} finally {
////				if (pm.currentTransaction().isActive())
////					pm.currentTransaction().rollback();
////
////				pm.close();
////			}
////
////		} while (response == null);
////
////		// A NullResponse which has a requestID assigned is forwarded to the requester and must be transformed into null here.
////		if (response instanceof NullResponse)
////			return null;
////
////		if (response instanceof ErrorResponse)
////			throw new ErrorResponseException((ErrorResponse)response);
////
////		try {
////			return responseClass.cast(response);
////		} catch (ClassCastException x) { // this exception has no nice message (according to source code), hence we throw our own below.
////			throw new ClassCastException("Expected a response of type " + responseClass + " but got an instance of " + response.getClass().getName() + "!");
////		}
////	}
////
////	@Override
////	protected Request pollRequestForProcessing(String cryptoSessionIDPrefix)
////	{
////		long beginTimestamp = System.currentTimeMillis();
////		Request request = null;
////		do {
////			PersistenceManager pm = createTransactionalPersistenceManager();
////			try {
//////				pm.currentTransaction().setSerializeRead(true);
////
////				PendingRequest pendingRequest = PendingRequest.getOldestPendingRequest(
////						pm, cryptoSessionIDPrefix, PendingRequestStatus.waitingForProcessing
////				);
////
////				if (pendingRequest != null) {
////					pendingRequest.setStatus(PendingRequestStatus.currentlyBeingProcessed);
////					request = pendingRequest.getRequest();
////				}
////
////				pm.currentTransaction().commit();
////			} finally {
////				if (pm.currentTransaction().isActive())
////					pm.currentTransaction().rollback();
////
////				pm.close();
////			}
////
////			if (request == null) {
////				if (System.currentTimeMillis() - beginTimestamp > timeoutPollRequestForProcessing)
////					break;
////
////				try {
////					Thread.sleep(1000L); // TODO make configurable?!
////				} catch (InterruptedException e) {
////					// ignore - only log - and break loop.
////					logger.warn("query: requestsWaitingForProcessing.wait(...) was interrupted with an InterruptedException.");
////					break;
////				}
////			}
////		} while (request == null);
////
////		return request;
////	}
////
////	@Override
////	protected void pushResponse(Response response)
////	{
////		if (response == null)
////			throw new IllegalArgumentException("response == null");
////
////		if (response.getRequestID() == null)
////			throw new IllegalArgumentException("response.requestID == null");
////
////		PersistenceManager pm = createTransactionalPersistenceManager();
////		try {
//////			pm.currentTransaction().setSerializeRead(true);
////
////			PendingRequest pendingRequest = PendingRequest.getPendingRequest(pm, response.getRequestID());
////			if (pendingRequest == null || pendingRequest.getStatus() != PendingRequestStatus.currentlyBeingProcessed)
////				logger.warn("pushResponse: There is no request currently being processed with requestID={}!!!", response.getRequestID());
////			else {
////				pendingRequest.setResponse(response);
////				pendingRequest.setStatus(PendingRequestStatus.completed);
////			}
////
////			pm.currentTransaction().commit();
////		} finally {
////			if (pm.currentTransaction().isActive())
////				pm.currentTransaction().rollback();
////
////			pm.close();
////		}
////	}
//
//}
