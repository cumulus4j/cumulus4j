/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
