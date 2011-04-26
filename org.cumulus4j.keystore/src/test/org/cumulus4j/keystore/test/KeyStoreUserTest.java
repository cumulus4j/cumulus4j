package org.cumulus4j.keystore.test;

import java.io.File;

import org.cumulus4j.keystore.CannotDeleteLastUserException;
import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.LoginException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class KeyStoreUserTest
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

	@Test
	public void createFirstUser()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
	}

	@Test
	public void create2ndUser()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser("marco", "test12345".toCharArray(), "bieber", "test6789".toCharArray());
	}

	@Test(expected=LoginException.class)
	public void create2ndUserWithoutAuthentication()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser(null, null, "bieber", "test6789".toCharArray());
	}

	@Test(expected=LoginException.class)
	public void create2ndUserWithWrongAuthentication()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser("marco", "test00000".toCharArray(), "bieber", "test6789".toCharArray());
	}

	@Test
	public void create2UsersAndDelete1()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser("marco", "test12345".toCharArray(), "bieber", "test6789".toCharArray());
		keyStore.deleteUser("bieber", "test6789".toCharArray(), "marco");
	}

	@Test
	public void create2UsersAndDeleteSelf()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser("marco", "test12345".toCharArray(), "bieber", "test6789".toCharArray());
		keyStore.deleteUser("bieber", "test6789".toCharArray(), "bieber");
	}

	@Test(expected=CannotDeleteLastUserException.class)
	public void create2UsersAndDeleteBoth()
	throws Exception
	{
		keyStore.createUser(null, null, "marco", "test12345".toCharArray());
		keyStore.createUser("marco", "test12345".toCharArray(), "bieber", "test6789".toCharArray());
		keyStore.deleteUser("bieber", "test6789".toCharArray(), "marco");
		keyStore.deleteUser("bieber", "test6789".toCharArray(), "bieber");
	}
}
