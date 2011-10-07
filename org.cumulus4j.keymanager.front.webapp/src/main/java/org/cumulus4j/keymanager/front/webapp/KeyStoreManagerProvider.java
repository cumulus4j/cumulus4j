package org.cumulus4j.keymanager.front.webapp;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

/**
 * Jersey provider making {@link KeyStoreManager} injectable.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Provider
public class KeyStoreManagerProvider
extends SingletonTypeInjectableProvider<Context, KeyStoreManager>
{
	/**
	 * Create a provider instance with the specified {@link KeyStoreManager}-singleton.
	 * @param keyStoreManager the singleton to be provided via {@link Context}.
	 */
	public KeyStoreManagerProvider(KeyStoreManager keyStoreManager) {
		super(KeyStoreManager.class, keyStoreManager);
	}
}
