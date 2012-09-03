package org.cumulus4j.store.crypto.keymanager.test;

public class TestKeyManagerCryptoSession_Twofish_CBC_PKCS5Padding_HMACSHA1
extends DefaultKeyManagerCryptoSessionTest
{
	@Override
	protected String getEncryptionAlgorithm() {
		return "Twofish/CBC/PKCS5Padding";
	}

	@Override
	protected String getMACAlgorithm() {
		return "HMAC-SHA1"; // does not have a MAC-IV
	}
}
