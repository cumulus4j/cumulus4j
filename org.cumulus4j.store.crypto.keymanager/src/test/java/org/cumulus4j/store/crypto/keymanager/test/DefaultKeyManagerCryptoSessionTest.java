package org.cumulus4j.store.crypto.keymanager.test;

import org.cumulus4j.store.crypto.Ciphertext;
import org.cumulus4j.store.crypto.Plaintext;
import org.cumulus4j.store.crypto.keymanager.KeyManagerCryptoManager;
import org.junit.Assert;
import org.junit.Test;

public abstract class DefaultKeyManagerCryptoSessionTest
extends AbstractKeyManagerCryptoSessionTest
{
	@Test
	public void encryptDecryptWithHelpfulDebugData() {
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

		Ciphertext ciphertext = cryptoSession.encrypt(cryptoContext, plaintext);
		Plaintext decrypted = cryptoSession.decrypt(cryptoContext, ciphertext);

		Assert.assertArrayEquals(plaintext.getData(), decrypted.getData());
	}

	@Test
	public void encryptDecryptWithRandomData() {
		Plaintext plaintext = new Plaintext();
		{
			byte[] data = new byte[random.nextInt(1024 * 1024)];
			random.nextBytes(data);
			plaintext.setData(data);
		}

		Ciphertext ciphertext = cryptoSession.encrypt(cryptoContext, plaintext);
		// NOT clearing cache in order to test the cached scenario.
		Ciphertext ciphertext1 = cryptoSession.encrypt(cryptoContext, plaintext);

		// Clear cache in order to test more code (i.e. ask the MockMessageBroker with a GetKeyRequest).
		((KeyManagerCryptoManager)cryptoSession.getCryptoManager()).getCryptoCache().clear();

		Ciphertext ciphertext2 = cryptoSession.encrypt(cryptoContext, plaintext);

		int c = countDifferentBits(ciphertext.getData(), ciphertext1.getData());
		Assert.assertTrue("Not enough bits different between ciphertext and ciphertext1! Only " + c + " bits differ!", c > 1024);

		c = countDifferentBits(ciphertext.getData(), ciphertext2.getData());
		Assert.assertTrue("Not enough bits different between ciphertext and ciphertext2! Only " + c + " bits differ!", c > 1024);

		c = countDifferentBits(ciphertext1.getData(), ciphertext2.getData());
		Assert.assertTrue("Not enough bits different between ciphertext1 and ciphertext2! Only " + c + " bits differ!", c > 1024);

		// Clear cache in order to test more code (i.e. ask the MockMessageBroker with a GetKeyRequest).
		((KeyManagerCryptoManager)cryptoSession.getCryptoManager()).getCryptoCache().clear();

		Plaintext decrypted = cryptoSession.decrypt(cryptoContext, ciphertext);

		Assert.assertArrayEquals(plaintext.getData(), decrypted.getData());

		// NOT clearing cache in order to test the cached scenario.
		Plaintext decrypted1 = cryptoSession.decrypt(cryptoContext, ciphertext1);

		// Clear cache in order to test more code (i.e. ask the MockMessageBroker with a GetKeyRequest).
		((KeyManagerCryptoManager)cryptoSession.getCryptoManager()).getCryptoCache().clear();
		Plaintext decrypted2 = cryptoSession.decrypt(cryptoContext, ciphertext2);

		c = countDifferentBits(decrypted.getData(), decrypted1.getData());
		Assert.assertEquals("decrypted does not match decrypted1! " + c + " bits differ!", 0, c);

		c = countDifferentBits(decrypted.getData(), decrypted2.getData());
		Assert.assertEquals("decrypted does not match decrypted2! " + c + " bits differ!", 0, c);
	}

}
