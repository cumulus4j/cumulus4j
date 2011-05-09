package org.cumulus4j.keymanager.front.webapp;

import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.cumulus4j.keystore.KeyStore;

import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@Provider
public class KeyStoreProvider
extends SingletonTypeInjectableProvider<Context, KeyStore>
{
	public KeyStoreProvider(KeyStore keyStore) {
		super(KeyStore.class, keyStore);
	}
}
