package org.cumulus4j.store.crypto.keymanager.test;

public class TestKeyManagerCryptoSession_default
extends AbstractKeyManagerCryptoSessionTest
{
	@Override
	protected String getEncryptionAlgorithm() {
		return null;
	}

	@Override
	protected String getMACAlgorithm() {
		return null;
	}
}
