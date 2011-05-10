package org.cumulus4j.store.crypto.keymanager.test;

import java.security.SecureRandom;
import java.util.UUID;

import org.cumulus4j.store.crypto.Ciphertext;
import org.cumulus4j.store.crypto.Plaintext;
import org.cumulus4j.store.crypto.keymanager.KeyManagerCryptoSession;
import org.cumulus4j.store.crypto.keymanager.rest.RequestResponseBroker;
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
		MockRequestResponseBroker.setMockSharedInstance();
		if (!(RequestResponseBroker.sharedInstance() instanceof MockRequestResponseBroker))
			Assert.fail("Setting MockRequestResponseBroker failed!");
	}

	@Before
	public void before()
	{
		session = new KeyManagerCryptoSession();
		session.setCryptoSessionID(UUID.randomUUID().toString());
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
	public void encryptDecrypt_aes_cbc_pkcs5padding()
	{
		Plaintext plaintext = new Plaintext();
		{
			byte[] data = new byte[random.nextInt(1024 * 1024)];
			random.nextBytes(data);
			plaintext.setData(data);
		}

		Ciphertext ciphertext = session.encrypt(plaintext);
		Plaintext decrypted = session.decrypt(ciphertext);

		Assert.assertArrayEquals(plaintext.getData(), decrypted.getData());
	}

}
