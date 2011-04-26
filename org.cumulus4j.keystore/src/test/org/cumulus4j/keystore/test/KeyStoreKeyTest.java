package org.cumulus4j.keystore.test;

import java.io.File;

import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.LoginException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KeyStoreKeyTest
{
	private File keyStoreFile;
	private KeyStore keyStore;

	@Before
	public void before()
	throws Exception
	{
		keyStoreFile = File.createTempFile("test-", ".keystore");
		keyStore = new KeyStore(keyStoreFile);
	}

	@After
	public void after()
	throws Exception
	{
		keyStore = null;
		File f = keyStoreFile;
		if (f != null)
			f.delete();
	}

	@Test(expected=LoginException.class)
	public void generateKeyWithoutAnyExistingUser()
	throws Exception
	{
		keyStore.generateKey("marco", "test12345".toCharArray());
	}

	@Test(expected=LoginException.class)
	public void generateKeyWithWrongPassword()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.generateKey("marco", "testxxxx".toCharArray());
	}

	@Test
	public void generateKeyWithCorrectUser()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.generateKey("marco", "test12345".toCharArray());
	}
}
