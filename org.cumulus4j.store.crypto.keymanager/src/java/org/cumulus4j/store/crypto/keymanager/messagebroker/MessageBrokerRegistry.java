package org.cumulus4j.store.crypto.keymanager.messagebroker;

import org.cumulus4j.store.crypto.keymanager.messagebroker.pmf.MessageBrokerPMF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class MessageBrokerRegistry
{
	private static Logger logger = LoggerFactory.getLogger(MessageBrokerRegistry.class);

	private static MessageBrokerRegistry sharedInstance = new MessageBrokerRegistry();

	public static MessageBrokerRegistry sharedInstance() { return sharedInstance; }


	/**
	 * The system property configuring which message-broker-implementation is to be used.
	 * If it is not specified, a list of known implementations is tried out and the first one which
	 * could be instantiated successfully is used.
	 */
	public static final String SYSTEM_PROPERTY_ACTIVE_MESSAGE_BROKER = "cumulus4j.MessageBrokerRegistry.activeMessageBroker";

	private static final Class<?>[] MESSAGE_BROKER_IMPLEMENTATION_CLASSES = {
//		MessageBrokerHttpPmf.class
//		MessageBrokerJVMSingleton.class
		MessageBrokerPMF.class
	};


	private volatile MessageBroker activeMessageBroker;

	public MessageBroker getActiveMessageBroker()
	{
		MessageBroker result = activeMessageBroker;
		if (result == null) {
			synchronized (this) { // correct & fast double-checked-locking with both 'volatile' and local variable 'result'.
				result = activeMessageBroker;
				if (result == null) {
					String messageBrokerImplClassName = System.getProperty(SYSTEM_PROPERTY_ACTIVE_MESSAGE_BROKER);
					if (messageBrokerImplClassName == null || messageBrokerImplClassName.trim().isEmpty()) {
						logger.info("sharedInstance: System property '{}' was not specified. Auto-detecting appropriate MessageBroker-implementation.", SYSTEM_PROPERTY_ACTIVE_MESSAGE_BROKER);

						for (Class<?> c : MESSAGE_BROKER_IMPLEMENTATION_CLASSES) {
							try {
								MessageBroker mb = (MessageBroker) c.newInstance();
								result = mb;
								break;
							} catch (Exception e) {
								logger.warn("sharedInstance: Could not instantiate " + c.getName() + ": " + e, e);
							}
						}

						if (result == null)
							throw new IllegalStateException("None of the available MessageBroker implementations could be successfully instantiated!");
					}
					else {
						try {
							Class<?> messageBrokerImplClass = Class.forName(messageBrokerImplClassName);
							result = (MessageBroker) messageBrokerImplClass.newInstance();
						} catch (ClassNotFoundException e) {
							throw new RuntimeException(e);
						} catch (InstantiationException e) {
							throw new RuntimeException(e);
						} catch (IllegalAccessException e) {
							throw new RuntimeException(e);
						}
					}

					activeMessageBroker = result;
					logger.info("getActiveMessageBroker: New activeMessageBroker={}", result);
				}
			}
		}

		return result;
	}

	public void setActiveMessageBroker(MessageBroker messageBroker)
	{
		MessageBroker amb = this.activeMessageBroker;
		if (amb != null && amb != messageBroker) {
			Exception x = new IllegalStateException("An active MessageBroker already exists! Changing the active MessageBroker now is highly discouraged as it may cause errors!");
			logger.warn("setActiveMessageBroker: " + x, x);
		}

		this.activeMessageBroker = messageBroker;
		logger.info("setActiveMessageBroker: New activeMessageBroker={}", messageBroker);
	}
}
