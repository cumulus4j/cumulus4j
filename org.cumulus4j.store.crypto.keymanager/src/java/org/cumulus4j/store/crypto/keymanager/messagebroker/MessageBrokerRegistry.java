package org.cumulus4j.store.crypto.keymanager.messagebroker;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class MessageBrokerRegistry
{
	private static MessageBrokerRegistry sharedInstance = new MessageBrokerRegistry();

	public static MessageBrokerRegistry sharedInstance() { return sharedInstance; }

}
