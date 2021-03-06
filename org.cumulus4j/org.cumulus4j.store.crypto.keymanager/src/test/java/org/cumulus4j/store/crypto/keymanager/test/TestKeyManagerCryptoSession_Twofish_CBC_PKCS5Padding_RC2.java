package org.cumulus4j.store.crypto.keymanager.test;

public class TestKeyManagerCryptoSession_Twofish_CBC_PKCS5Padding_RC2
extends DefaultKeyManagerCryptoSessionTest
{
	@Override
	protected String getEncryptionAlgorithm() {
		return "Twofish/CBC/PKCS5Padding";
	}

	@Override
	protected String getMACAlgorithm() {
		return "RC2"; // has a MAC-IV
	}
}
