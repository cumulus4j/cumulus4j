package org.cumulus4j.store.crypto.keymanager;

import org.cumulus4j.store.crypto.AbstractCryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyServerCryptoManager extends AbstractCryptoManager {

	@Override
	protected CryptoSession createCryptoSession() {
		return new KeyServerCryptoSession();
	}

}