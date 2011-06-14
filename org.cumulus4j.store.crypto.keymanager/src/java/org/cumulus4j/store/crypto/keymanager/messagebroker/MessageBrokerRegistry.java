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
package org.cumulus4j.store.crypto.keymanager.messagebroker;

import org.cumulus4j.keymanager.back.shared.Message;
import org.cumulus4j.store.crypto.keymanager.messagebroker.inmemory.MessageBrokerInMemory;
import org.cumulus4j.store.crypto.keymanager.messagebroker.pmf.MessageBrokerPMF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JVM-singleton to access the {@link #getActiveMessageBroker() active message-broker}.
 * It handles the registration and instantiation of {@link MessageBroker} implementations.
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
		MessageBrokerPMF.class,
		MessageBrokerInMemory.class
	};

	private volatile MessageBroker activeMessageBroker;

	/**
	 * Get the active {@link MessageBroker}. All {@link Message}s are transmitted over this active instance.
	 * If there is no active <code>MessageBroker</code>, yet, this method will
	 * check the system property {@value #SYSTEM_PROPERTY_ACTIVE_MESSAGE_BROKER} and either instantiate the
	 * class configured there or iterate a list of known <code>MessageBroker</code>-implementation-classes.
	 * @return the active <code>MessageBroker</code>; never <code>null</code>. If no active message-broker is set
	 * and none can be instantiated, an exception is thrown.
	 */
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

	/**
	 * Set the active {@link MessageBroker}. Whatever is passed here will be returned by {@link #getActiveMessageBroker()}
	 * except for <code>null</code>. Setting <code>null</code> will cause {@link #getActiveMessageBroker()} to perform
	 * a re-initialisation (i.e. instantiate a <code>MessageBroker</code> as needed).
	 * @param messageBroker the {@link MessageBroker} instance to be set or <code>null</code> to clear the current one.
	 */
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
