package org.cumulus4j.keyserver.front.webapp;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.cumulus4j.keystore.KeyStore;

import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

@Provider
public class KeyStoreProvider
extends SingletonTypeInjectableProvider<Context, KeyStore>
{
	public KeyStoreProvider(KeyStore keyStore) {
		super(KeyStore.class, keyStore);
	}
}
