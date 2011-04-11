package org.cumulus4j.core;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.cumulus4j.api.keymanagement.AbstractKeyManager;
import org.cumulus4j.api.keymanagement.AbstractKeyManagerSession;
import org.cumulus4j.api.keymanagement.KeyManagerSession;

/**
 * Dummy key-manager for debugging and testing.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class DummyKeyManager extends AbstractKeyManager
{
	@Override
	protected KeyManagerSession createSession() {
		return new DummySession();
	}

	private static final class DummySession
	extends AbstractKeyManagerSession
	{
		// key length: 128 bits
		private static final byte[] dummyKey = { 'D', 'e', 'r', ' ', 'F', 'e', 'r', 'd', ' ', 'h', 'a', 't', ' ', 'v', 'i', 'e' };
		// initialization vector length: 128 bits
		private static final IvParameterSpec iv = new IvParameterSpec(new byte[] {'b', 'l', 'a', 't', 'r', 'u', 'l', 'l', 'a', 'l', 'a', 't', 'r', 'a', 'r', 'a'});

		private static final String ALGORITHM = "AES";
		private static final String ALGORITHM_WITH_PARAMS = ALGORITHM + "/CBC/PKCS5Padding";

		private Cipher encrypter;
		private Cipher decrypter;
		{
			try {
				SecretKeySpec key = new SecretKeySpec(dummyKey, ALGORITHM);
				encrypter = Cipher.getInstance(ALGORITHM_WITH_PARAMS);
				encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
				decrypter = Cipher.getInstance(ALGORITHM_WITH_PARAMS);
				decrypter.init(Cipher.DECRYPT_MODE, key, iv);
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public Cipher getEncrypter(long keyID) {
			return encrypter;
		}

		@Override
		public Cipher getDecrypter(long keyID) {
			return decrypter;
		}
	}
}
