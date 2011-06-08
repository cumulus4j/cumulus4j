package org.cumulus4j.keymanager.back.shared.test;

import org.bouncycastle.crypto.AsymmetricCipherKeyPair;
import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.CipherOperationMode;
import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.keymanager.back.shared.KeyEncryptionUtil;
import org.junit.Assert;
import org.junit.Test;

public class KeyEncryptionUtilTest
{
	@Test
	public void testEncryptAndDecrypt()
	throws Exception
	{
		byte[] key = new byte[32];
		for (int i = 0; i < key.length; ++i)
			key[i] = (byte)(50 + i);

		String transformation = "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING";
		AsymmetricCipherKeyPairGenerator keyPairGenerator = CryptoRegistry.sharedInstance().createKeyPairGenerator(CryptoRegistry.splitTransformation(transformation)[0]);
		AsymmetricCipherKeyPair keyPair = keyPairGenerator.generateKeyPair();

		byte[] publicKey = CryptoRegistry.sharedInstance().encodePublicKey(keyPair.getPublic());
		Cipher cipher = CryptoRegistry.sharedInstance().createCipher(transformation);
		cipher.init(CipherOperationMode.ENCRYPT, keyPair.getPublic());
		byte[] encryptedKey1 = KeyEncryptionUtil.encryptKey(key, cipher);
		byte[] encryptedKey2 = KeyEncryptionUtil.encryptKey(key, transformation, publicKey);

		cipher.init(CipherOperationMode.DECRYPT, keyPair.getPrivate());
		byte[] decryptedKey1 = KeyEncryptionUtil.decryptKey(cipher, encryptedKey1);
		byte[] decryptedKey2 = KeyEncryptionUtil.decryptKey(cipher, encryptedKey2);

		Assert.assertArrayEquals(key, decryptedKey1);
		Assert.assertArrayEquals(key, decryptedKey2);
	}
}
