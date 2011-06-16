package org.cumulus4j.store.crypto.keymanager.test;

public class TestKeyManagerCryptoSession_Twofish_CBC_PKCS5Padding_RC2
extends AbstractKeyManagerCryptoSessionTest
{
	@Override
	protected String getEncryptionAlgorithm() {
		return "Twofish/CBC/PKCS5Padding";
	}

	@Override
	protected String getMacAlgorithm() {
		return "RC2"; // has a MAC-IV
	}
}
