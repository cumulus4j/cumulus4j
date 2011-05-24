package org.cumulus4j.store.crypto.keymanager.rest;

import java.util.UUID;
import java.util.concurrent.TimeoutException;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;
import org.cumulus4j.keymanager.back.shared.NullResponse;
import org.cumulus4j.keymanager.back.shared.Request;
import org.cumulus4j.keymanager.back.shared.Response;
import org.cumulus4j.store.crypto.keymanager.rest.messagebrokerhttppmf.MessageBrokerHttpPmf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class MessageBroker
{
	private static final Logger logger = LoggerFactory.getLogger(MessageBroker.class);

	private static MessageBroker sharedInstance;

	/**
	 * The system property configuring which message-broker-implementation is to be used.
	 * If it is not specified, a list of known implementations is tried out and the first one which
	 * could be instantiated successfully is used.
	 */
	public static final String SYSTEM_PROPERTY_MESSAGE_BROKER = "org.cumulus4j.store.crypto.keymanager.rest.MessageBroker";

	private static final Class<?>[] MESSAGE_BROKER_IMPLEMENTATION_CLASSES = {
		MessageBrokerHttpPmf.class
//		MessageBrokerJVMSingleton.class
	};

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

	protected ActiveKeyManagerChannelRegistration registerActiveKeyManagerChannel(String cryptoSessionIDPrefix, String internalKeyManagerChannelURL)
	{
		// no-op
		return new ActiveKeyManagerChannelRegistration(UUID.randomUUID().toString(), cryptoSessionIDPrefix);
	}

	protected void unregisterActiveKeyManagerChannel(ActiveKeyManagerChannelRegistration registration)
	{
		// no-op
	}

	/**
	 * Get the singleton.
	 * @return the single shared instance.
	 */
	public static synchronized MessageBroker sharedInstance()
	{
		if (sharedInstance == null) {
			String messageBrokerImplClassName = System.getProperty(SYSTEM_PROPERTY_MESSAGE_BROKER);
			if (messageBrokerImplClassName == null || messageBrokerImplClassName.trim().isEmpty()) {
				logger.info("sharedInstance: System property '{}' was not specified. Auto-detecting appropriate MessageBroker-implementation.", SYSTEM_PROPERTY_MESSAGE_BROKER);

				for (Class<?> c : MESSAGE_BROKER_IMPLEMENTATION_CLASSES) {
					try {
						MessageBroker mb = (MessageBroker) c.newInstance();
						sharedInstance = mb;
						return mb;
					} catch (Exception e) {
						logger.warn("sharedInstance: Could not instantiate " + c.getName() + ": " + e, e);
					}
				}

				throw new IllegalStateException("None of the available MessageBroker implementations could be successfully instantiated!");
			}
			else {
				try {
					Class<?> messageBrokerImplClass = Class.forName(messageBrokerImplClassName);
					sharedInstance = (MessageBroker) messageBrokerImplClass.newInstance();
				} catch (ClassNotFoundException e) {
					throw new RuntimeException(e);
				} catch (InstantiationException e) {
					throw new RuntimeException(e);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
			}
		}

		return sharedInstance;
	}

	public static synchronized void setSharedInstance(MessageBroker messageBroker)
	{
		if (sharedInstance != null && sharedInstance == messageBroker) {
			Exception x = new IllegalStateException("A shared instance already exists! Changing the shared instance now is highly discouraged as it may cause errors!");
			logger.warn("setSharedInstance: " + x, x);
		}
		sharedInstance = messageBroker;
	}

	/**
	 * Send <code>request</code> to the key-manager (embedded in client or separate in key-server) and return its response.
	 * @param responseClass the type of the expected response.
	 * @param request the request to be sent to the key-manager.
	 * @return the response from the key-manager. Will be <code>null</code>, if the key-manager replied with a {@link NullResponse}
	 * (if valid for the given request).
	 * @throws TimeoutException if the request was not replied within the query-timeout.
	 * @throws ErrorResponseException if the key-manager (either running embedded on the remote client or
	 * in a separate key-server) sent an {@link ErrorResponse}.
	 */
	public abstract <R extends Response> R query(Class<R> responseClass, Request request)
	throws TimeoutException, ErrorResponseException;

	protected abstract Request pollRequestForProcessing(String cryptoSessionIDPrefix);

	protected abstract void pushResponse(Response response);
}
