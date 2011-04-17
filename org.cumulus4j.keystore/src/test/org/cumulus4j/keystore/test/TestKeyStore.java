package org.cumulus4j.keystore.test;

import java.io.File;

import org.cumulus4j.keystore.KeyStore;
import org.junit.Test;

public class TestKeyStore
{
	@Test
	public void createUserTest()
	throws Exception
	{
		File keyStoreFile = File.createTempFile("test-", ".keystore");
		KeyStore keyStore = new KeyStore(keyStoreFile);
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
	}


}
