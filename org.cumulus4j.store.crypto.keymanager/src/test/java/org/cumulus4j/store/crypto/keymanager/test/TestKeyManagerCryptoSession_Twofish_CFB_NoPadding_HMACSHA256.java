package org.cumulus4j.store.crypto.keymanager.test;

public class TestKeyManagerCryptoSession_Twofish_CFB_NoPadding_HMACSHA256
extends DefaultKeyManagerCryptoSessionTest
{
	@Override
	protected String getEncryptionAlgorithm() {
		return "Twofish/CFB/NoPadding";
	}

	@Override
	protected String getMACAlgorithm() {
		return "HMAC-SHA256"; // does not have a MAC-IV
	}
}
