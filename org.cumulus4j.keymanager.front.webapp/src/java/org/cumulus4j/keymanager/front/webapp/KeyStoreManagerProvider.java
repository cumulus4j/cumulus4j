package org.cumulus4j.keymanager.front.webapp;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

@Provider
public class KeyStoreManagerProvider
extends SingletonTypeInjectableProvider<Context, KeyStoreManager>
{
	public KeyStoreManagerProvider(KeyStoreManager keyStoreManager) {
		super(KeyStoreManager.class, keyStoreManager);
	}
}
