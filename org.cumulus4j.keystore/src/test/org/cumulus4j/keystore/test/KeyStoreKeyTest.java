package org.cumulus4j.keystore.test;

import java.io.File;
import java.security.Key;

import org.cumulus4j.keystore.GeneratedKey;
import org.cumulus4j.keystore.KeyStore;
import org.cumulus4j.keystore.LoginException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nightlabs.util.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreKeyTest
{
	private static final Logger logger = LoggerFactory.getLogger(KeyStoreKeyTest.class);

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
		keyStore.generateKey(USER, PASSWORD);
	}

	@Test(expected=LoginException.class)
	public void generateKeyWithWrongPassword()
	throws Exception
	{
		keyStore.createUser(null, null, USER, PASSWORD);
		keyStore.generateKey(USER, "testxxxx".toCharArray());
	}

	@Test
	public void generateKeyWithCorrectUser()
	throws Exception
	{
		keyStore.createUser(null, null, USER, PASSWORD);
		keyStore.generateKey(USER, PASSWORD);
	}

	private static final String USER = "marco";
	private static final char[] PASSWORD = "test12345".toCharArray();

	@Test
	public void generateKeyAndRetrieveKey()
	throws Exception
	{
		keyStore.createUser(null, null, USER, PASSWORD);

		keyStore = null;
		keyStore = new KeyStore(keyStoreFile);

		GeneratedKey generatedKey = keyStore.generateKey(USER, PASSWORD);

		keyStore = null;
		keyStore = new KeyStore(keyStoreFile);

		Key key = keyStore.getKey(USER, PASSWORD, generatedKey.getKeyID());

		Assert.assertArrayEquals("key.encoded not equal", generatedKey.getKey().getEncoded(), key.getEncoded());
		Assert.assertEquals("key.algorithm not equal", generatedKey.getKey().getAlgorithm(), key.getAlgorithm());
		Assert.assertEquals("key.format not equal", generatedKey.getKey().getFormat(), key.getFormat());
	}

//	@Test
//	public void generateManyKeys()
//	throws Exception
//	{
//		Stopwatch stopwatch = new Stopwatch();
//
//		keyStore.createUser(null, null, USER, PASSWORD);
//
//		stopwatch.start("00.generateManyKeys");
//
//		for (int i = 0; i < 100000; ++i)
//			keyStore.generateKey(USER, PASSWORD);
//
//		stopwatch.stop("00.generateManyKeys");
//
//		logger.info("generateManyKeys: " + stopwatch.createHumanReport(true));
//	}

	@Test
	public void generateAndReadManyKeys()
	throws Exception
	{
		Stopwatch stopwatch = new Stopwatch();

		keyStore.createUser(null, null, USER, PASSWORD);

		stopwatch.start("00.generateManyKeys");

		int keyCount = 10000;

		long firstKeyID = -1;
		long lastKeyID = -1;
		for (int i = 0; i < keyCount; ++i) {
			stopwatch.start("05.generateOneKey");
			GeneratedKey generatedKey = keyStore.generateKey(USER, PASSWORD);
			stopwatch.stop("05.generateOneKey");

			if (firstKeyID < 0)
				firstKeyID = generatedKey.getKeyID();

			lastKeyID = generatedKey.getKeyID();
		}

		stopwatch.stop("00.generateManyKeys");

		stopwatch.start("10.readManyKeys");

		for (long keyID = firstKeyID; keyID <= lastKeyID; ++keyID) {
			keyStore.getKey(USER, PASSWORD, keyID);
		}

		stopwatch.stop("10.readManyKeys");

		logger.info("generateAndReadManyKeys ({} keys): {}", keyCount, stopwatch.createHumanReport(true));
	}

//  The following method is not a real jUnit-test, but requires a developer to watch the DEBUG output.
//  There is not enough API available to make this a real jUnit-test. Shall we add this API? For now, I don't. Marco.
//	@Test
//	public void longRunningExpireCacheTest()
//	throws Exception
//	{
//		keyStore.createUser(null, null, USER, PASSWORD);
//		keyStore.createUser(USER, PASSWORD, "user1", "pw1".toCharArray());
//		keyStore.createUser(USER, PASSWORD, "user2", "pw2".toCharArray());
//		keyStore.createUser(USER, PASSWORD, "user3", "pw3".toCharArray());
//		keyStore.createUser(USER, PASSWORD, "user4", "pw4".toCharArray());
//
//		keyStore.generateKey("user1", "pw1".toCharArray());
//		keyStore.generateKey("user2", "pw2".toCharArray());
//		keyStore.generateKey("user3", "pw3".toCharArray());
//		keyStore.generateKey("user4", "pw4".toCharArray());
//
//
//		keyStore.generateKey("user1", "pw1".toCharArray());
//		keyStore.generateKey("user2", "pw2".toCharArray());
//
//		Thread.sleep(70000);
//
//		keyStore.generateKey("user3", "pw3".toCharArray());
//
//		Thread.sleep(90000);
//
//		keyStore.generateKey("user4", "pw4".toCharArray());
//
//		Thread.sleep(200000);
//	}
}
