package org.cumulus4j.store.crypto.keymanager;

import org.cumulus4j.core.crypto.AbstractCryptoManager;
import org.cumulus4j.core.crypto.CryptoSession;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyServerCryptoManager extends AbstractCryptoManager {

	@Override
	protected CryptoSession createCryptoSession() {
		return new KeyServerCryptoSession();
	}

}
