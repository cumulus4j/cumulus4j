package org.cumulus4j.store.crypto.keymanager.test;

import org.cumulus4j.store.crypto.CryptoManager;

public class TestKeyManagerCryptoSession_Twofish_GCM_NoPadding_NONE
extends AbstractKeyManagerCryptoSessionTest
{
	@Override
	protected String getEncryptionAlgorithm() {
		return "Twofish/GCM/NoPadding";
	}

	@Override
	protected String getMacAlgorithm() {
		return CryptoManager.MAC_ALGORITHM_NONE;
	}
}
