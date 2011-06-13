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

package org.cumulus4j.store;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.cumulus4j.store.crypto.AbstractCryptoManager;
import org.cumulus4j.store.crypto.AbstractCryptoSession;
import org.cumulus4j.store.crypto.Ciphertext;
import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.crypto.Plaintext;

/**
 * Dummy crypto-manager for debugging and testing.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class DummyCryptoManager extends AbstractCryptoManager
{
	@Override
	protected CryptoSession createCryptoSession() {
		return new DummySession();
	}

	private static final class DummySession
	extends AbstractCryptoSession
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
		public Ciphertext encrypt(Plaintext plaintext)
		{
			// First get the required resources (that are cleared in close()).
			Cipher c = encrypter;

			// Then assert that we are not yet closed. This makes sure that we definitely can continue
			// even if close() is called right now simultaneously.
			assertNotClosed();

			Ciphertext result = new Ciphertext();
			result.setKeyID(12345);

			synchronized (c) {
				try {
					result.setData(
							c.doFinal(plaintext.getData())
					);
				} catch (IllegalBlockSizeException e) {
					throw new RuntimeException(e);
				} catch (BadPaddingException e) {
					throw new RuntimeException(e);
				}
			}

			return result;
		}

		@Override
		public Plaintext decrypt(Ciphertext ciphertext)
		{
			if (ciphertext.getKeyID() != 12345)
				throw new IllegalArgumentException("No key with this keyID: " + ciphertext.getKeyID());

			// First get the required resources (that are cleared in close()).
			Cipher c = decrypter;

			// Then assert that we are not yet closed. This makes sure that we definitely can continue
			// even if close() is called right now simultaneously.
			assertNotClosed();

			Plaintext result = new Plaintext();

			synchronized (c) {
				try {
					result.setData(
							c.doFinal(ciphertext.getData())
					);
				} catch (IllegalBlockSizeException e) {
					throw new RuntimeException(e);
				} catch (BadPaddingException e) {
					throw new RuntimeException(e);
				}
			}

			return result;
		}

		@Override
		public void close() {
			super.close();
			encrypter = null;
			decrypter = null;
		}
	}

}
