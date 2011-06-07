package org.cumulus4j.store.crypto.keymanager.test;

import java.security.SecureRandom;
import java.util.UUID;

import org.cumulus4j.store.crypto.Ciphertext;
import org.cumulus4j.store.crypto.Plaintext;
import org.cumulus4j.store.crypto.keymanager.KeyManagerCryptoManager;
import org.cumulus4j.store.crypto.keymanager.KeyManagerCryptoSession;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBrokerRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class KeyManagerCryptoSessionTest
{
	private static SecureRandom random = new SecureRandom();

	@BeforeClass
	public static void beforeClass()
	{
		MockMessageBroker.setMockSharedInstance();
		if (!(MessageBrokerRegistry.sharedInstance().getActiveMessageBroker() instanceof MockMessageBroker))
			Assert.fail("Setting MockMessageBroker failed!");
	}

	@Before
	public void before()
	{
		KeyManagerCryptoManager cryptoManager = new KeyManagerCryptoManager();
//		session = new KeyManagerCryptoSession();
		session = (KeyManagerCryptoSession) cryptoManager.getCryptoSession(UUID.randomUUID().toString());
//		session.setCryptoManager(cryptoManager);
//		session.setCryptoSessionID(UUID.randomUUID().toString());
	}

	private KeyManagerCryptoSession session;

	@Test
	public void encryptDecryptWithHelpfulDebugData()
	{
		Plaintext plaintext = new Plaintext();
		{
			byte[] data = new byte[64];
			for (int i = 0; i < data.length; ++i)
				data[i] = (byte)( data.length - i );

			data[data.length - 1] = (byte)255;
			data[data.length - 2] = (byte)255;
			data[data.length - 3] = (byte)255;

			plaintext.setData(data);
		}

		Ciphertext ciphertext = session.encrypt(plaintext);
		Plaintext decrypted = session.decrypt(ciphertext);

		Assert.assertArrayEquals(plaintext.getData(), decrypted.getData());
	}

	@Test
	public void encryptDecryptWithRandomData()
	{
		Plaintext plaintext = new Plaintext();
		{
			byte[] data = new byte[random.nextInt(1024 * 1024)];
			random.nextBytes(data);
			plaintext.setData(data);
		}

		Ciphertext ciphertext = session.encrypt(plaintext);

		// Clear cache in order to test more code (i.e. ask the MockMessageBroker with a GetKeyRequest).
		((KeyManagerCryptoManager)session.getCryptoManager()).getCipherCache().clear();

		Plaintext decrypted = session.decrypt(ciphertext);

		Assert.assertArrayEquals(plaintext.getData(), decrypted.getData());
	}

}
