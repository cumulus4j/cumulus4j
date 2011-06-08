package org.cumulus4j.crypto.test;

import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.CipherParameters;
import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.CipherOperationMode;
import org.cumulus4j.crypto.CryptoRegistry;
import org.junit.Assert;
import org.junit.Test;

public class AsymmetricKeyTest
{
	private SecureRandom secureRandom = new SecureRandom();

	@Test
	public void encodeDecodeRSA()
	throws Exception
	{
		AsymmetricCipherKeyPairGenerator keyPairGenerator = CryptoRegistry.sharedInstance().createKeyPairGenerator("RSA");
		AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();

		byte[] encodedPrivateKey = CryptoRegistry.sharedInstance().encodePrivateKey(keyPair.getPrivate());
		byte[] encodedPublicKey = CryptoRegistry.sharedInstance().encodePublicKey(keyPair.getPublic());

		CipherParameters decodedPrivateKey = CryptoRegistry.sharedInstance().decodePrivateKey(encodedPrivateKey);
		CipherParameters decodedPublicKey = CryptoRegistry.sharedInstance().decodePublicKey(encodedPublicKey);

		byte[] plainText = new byte[100 + secureRandom.nextInt(40)];
		secureRandom.nextBytes(plainText);

		Cipher cipher = CryptoRegistry.sharedInstance().createCipher("RSA");

		cipher.init(CipherOperationMode.ENCRYPT, keyPair.getPublic());
		byte[] encrypted1 = cipher.doFinal(plainText);

		cipher.init(CipherOperationMode.ENCRYPT, decodedPublicKey);
		byte[] encrypted2 = cipher.doFinal(plainText);

		cipher.init(CipherOperationMode.DECRYPT, keyPair.getPrivate());
		byte[] decrypted1a = cipher.doFinal(encrypted1);
		byte[] decrypted2a = cipher.doFinal(encrypted2);

		cipher.init(CipherOperationMode.DECRYPT, decodedPrivateKey);
		byte[] decrypted1b = cipher.doFinal(encrypted1);
		byte[] decrypted2b = cipher.doFinal(encrypted2);

		Assert.assertArrayEquals(plainText, decrypted1a);
		Assert.assertArrayEquals(plainText, decrypted1b);
		Assert.assertArrayEquals(plainText, decrypted2a);
		Assert.assertArrayEquals(plainText, decrypted2b);
	}
}
