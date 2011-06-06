package org.cumulus4j.store.crypto.keymanager;

import org.cumulus4j.store.crypto.AbstractCryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;

/**
 * <p>
 * Implementation of {@link org.cumulus4j.store.crypto.CryptoManager CryptoManager} working with a
 * key-manager as shown in <a href="http://cumulus4j.org/documentation/deployment-scenarios.html">Deployment scenarios</a>.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyManagerCryptoManager extends AbstractCryptoManager
{
	private CipherCache cipherCache = new CipherCache();

	@Override
	protected CryptoSession createCryptoSession() {
		return new KeyManagerCryptoSession();
	}

	public CipherCache getCipherCache() {
		return cipherCache;
	}
}
