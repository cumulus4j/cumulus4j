package org.cumulus4j.keystore.test;

import java.io.File;
import java.security.Key;
import java.util.List;

import org.cumulus4j.keystore.AuthenticationException;
import org.cumulus4j.keystore.GeneratedKey;
import org.cumulus4j.keystore.KeyStore;
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

	protected static final String USER = "marco";
	protected static final char[] PASSWORD = "test12345".toCharArray();

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

	@Test(expected=AuthenticationException.class)
	public void generateKeyWithoutAnyExistingUser()
	throws Exception
	{
		keyStore.generateKey(USER, PASSWORD);
	}

	@Test(expected=AuthenticationException.class)
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
		keyStore.clearCache(null);
		keyStore.getUsers(USER, PASSWORD);
		keyStore.clearCache(null);
		keyStore.getUsers(USER, PASSWORD);
		keyStore.clearCache(null);
		GeneratedKey firstGeneratedKey = keyStore.generateKey(USER, PASSWORD);
		keyStore.clearCache(null);
		keyStore.getUsers(USER, PASSWORD);
		keyStore.clearCache(null);
		keyStore.generateKey(USER, PASSWORD);
		keyStore.clearCache(null);
		keyStore.getKey(USER, PASSWORD, firstGeneratedKey.getKeyID());
	}

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

	@Test
	public void generateAndReadManyKeysIndividually()
	throws Exception
	{
		Stopwatch stopwatch = new Stopwatch();

		keyStore.createUser(null, null, USER, PASSWORD);

		keyStore.clearCache(null);

		stopwatch.start("00.generateManyKeysIndividually");

		int keyCount = 5000;

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
		logger.info("generateAndReadManyKeysIndividually: firstKeyID={} lastKeyID={}", firstKeyID, lastKeyID);

		stopwatch.stop("00.generateManyKeysIndividually");

		stopwatch.start("07.readKeyStore");
		keyStore = new KeyStore(keyStoreFile);
		stopwatch.stop("07.readKeyStore");

		stopwatch.start("10.readManyKeys");

		for (long keyID = firstKeyID; keyID <= lastKeyID; ++keyID) {
			try {
				keyStore.getKey(USER, PASSWORD, keyID);
			} catch (Exception x) {
				logger.error("generateAndReadManyKeysIndividually: Getting key " + keyID + " failed: " + x, x);
				throw x;
			}
		}

		stopwatch.stop("10.readManyKeys");

		logger.info("generateAndReadManyKeysIndividually ({} keys): {}", keyCount, stopwatch.createHumanReport(true));
	}

	@Test
	public void generateAndReadManyKeysBulk()
	throws Exception
	{
		Stopwatch stopwatch = new Stopwatch();

		keyStore.createUser(null, null, USER, PASSWORD);
		keyStore.createUser(USER, PASSWORD, "alex", PASSWORD);
		keyStore.createUser(USER, PASSWORD, "daniel", "VerySecretPassword".toCharArray());

		stopwatch.start("00.generateManyKeysBulk");

		int keyCount = 35000;

		long firstKeyID = -1;
		long lastKeyID = -1;
		List<GeneratedKey> generatedKeys = keyStore.generateKeys(USER, PASSWORD, keyCount);
		firstKeyID = generatedKeys.get(0).getKeyID();
		lastKeyID = generatedKeys.get(generatedKeys.size() - 1).getKeyID();

		stopwatch.stop("00.generateManyKeysBulk");


		stopwatch.start("07.readKeyStore");
		keyStore = new KeyStore(keyStoreFile);
		stopwatch.stop("07.readKeyStore");


		stopwatch.start("10.readManyKeys");

		for (long keyID = firstKeyID; keyID <= lastKeyID; ++keyID) {
			keyStore.getKey(USER, PASSWORD, keyID);
		}

		stopwatch.stop("10.readManyKeys");

		logger.info("generateAndReadManyKeysBulk ({} keys): {}", keyCount, stopwatch.createHumanReport(true));
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
