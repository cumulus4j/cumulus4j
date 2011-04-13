package org.cumulus4j.keyserver.back.plugin;

import org.cumulus4j.api.crypto.AbstractCryptoManager;
import org.cumulus4j.api.crypto.CryptoSession;

public class KeyServerCryptoManager extends AbstractCryptoManager {

	@Override
	protected CryptoSession createCryptoSession() {
		return new KeyServerCryptoSession();
	}

}
