package org.cumulus4j.store.crypto.keymanager.test;

import org.cumulus4j.store.crypto.CryptoManager;

public class TestKeyManagerCryptoSession_AES_GCM_NoPadding_NONE
extends AbstractKeyManagerCryptoSessionTest
{
	@Override
	protected String getEncryptionAlgorithm() {
		return "AES/GCM/NoPadding";
	}

	@Override
	protected String getMACAlgorithm() {
		return CryptoManager.MAC_ALGORITHM_NONE;
	}
}
