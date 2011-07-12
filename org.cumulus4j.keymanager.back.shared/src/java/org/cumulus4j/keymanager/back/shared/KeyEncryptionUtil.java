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
package org.cumulus4j.keymanager.back.shared;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.CipherOperationMode;
import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.crypto.MACCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Utility class to en- &amp; decrypt symmetric secret keys using asymmetric encryption.
 * </p>
 * <p>
 * TODO the MAC algorithm should be communicated between key-manager and app-server (maybe
 * the app-server specifies it, but with the possibility that the key-manager can override, i.e. use another one?!
 * thus requiring the GetKeyResponse to tell the app-server, which one was actually used - or maybe encode this into the
 * binary result here? Or maybe only specify it here on the key-manager-side (and encode in the binary)?
 * less work and probably sufficient).
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class KeyEncryptionUtil
{
	private static final Logger logger = LoggerFactory.getLogger(KeyEncryptionUtil.class);

	private KeyEncryptionUtil() { }

	private static final String MAC_ALGORITHM = "HMAC-SHA1";

	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/**
	 * Encrypt the given symmetric secret <code>key</code> with the given {@link Cipher}.
	 * The key will be protected against manipulation/corruption by a MAC.
	 *
	 * @param key the symmetric secret key to be encrypted.
	 * @param encrypter the cipher used for encryption.
	 * @return the key together with the MAC's key + IV - all encrypted.
	 * @throws CryptoException in case the encryption fails.
	 * @throws NoSuchAlgorithmException in case a crypto algorithm's name (e.g. for the MAC) does not exist in the {@link CryptoRegistry}.
	 * @see #encryptKey(byte[], String, byte[])
	 */
	public static byte[] encryptKey(byte[] key, Cipher encrypter) throws CryptoException, NoSuchAlgorithmException
	{
		byte[] mac = EMPTY_BYTE_ARRAY;
		byte[] macKey = EMPTY_BYTE_ARRAY;
		byte[] macIV = EMPTY_BYTE_ARRAY;

		MACCalculator macCalculator = CryptoRegistry.sharedInstance().createMACCalculator(MAC_ALGORITHM, true);
		mac = macCalculator.doFinal(key);
		if (macCalculator.getParameters() instanceof ParametersWithIV) {
			ParametersWithIV pwiv = (ParametersWithIV) macCalculator.getParameters();
			macIV = pwiv.getIV();
			macKey = ((KeyParameter)pwiv.getParameters()).getKey();
		}
		else if (macCalculator.getParameters() instanceof KeyParameter) {
			macKey = ((KeyParameter)macCalculator.getParameters()).getKey();
		}
		else
			throw new IllegalStateException("macCalculator.getParameters() returned an instance of an unknown type: " + (macCalculator.getParameters() == null ? null : macCalculator.getParameters().getClass().getName()));

		int resultSize = (
				1 // version
				+ 3 // macKeySize, macIVSize, macSize
				+ encrypter.getOutputSize(macKey.length + macIV.length + key.length + mac.length)
		);

		byte[] out = new byte[resultSize];

		if (macKey.length > 255)
			throw new IllegalStateException("MAC key length too long!");

		if (macIV.length > 255)
			throw new IllegalStateException("MAC IV length too long!");

		if (mac.length > 255)
			throw new IllegalStateException("MAC length too long!");

		int outOff = 0;
		out[outOff++] = (byte)1; // version
		out[outOff++] = (byte)macKey.length;
		out[outOff++] = (byte)macIV.length;
		out[outOff++] = (byte)mac.length;

		outOff += encrypter.update(macKey, 0, macKey.length, out, outOff);
		outOff += encrypter.update(macIV,  0,  macIV.length, out, outOff);
		outOff += encrypter.update(key,    0,    key.length, out, outOff);
		outOff += encrypter.update(mac,    0,    mac.length, out, outOff);
		outOff += encrypter.doFinal(out, outOff);

		if (out.length == outOff)
			return out;

		logger.warn("Precalculated size ({}) does not match the actually written size ({})! Truncating byte array.", out.length, outOff);

		byte[] result = new byte[outOff];
		System.arraycopy(out, 0, result, 0, result.length);
		return result;
	}

	/**
	 * Encrypt the given symmetric secret <code>key</code>.
	 * The key will be protected against manipulation/corruption by a MAC (the algorithm is currently hard-coded, but this might be changed, soon).
	 *
	 * @param key the symmetric secret key to be encrypted.
	 * @param keyEncryptionTransformation the transformation to be used to encrypt (see {@link CryptoRegistry#createCipher(String)}).
	 * @param keyEncryptionPublicKey the public key to be used to encrypt the given <code>key</code>.
	 * @return the key together with the MAC's key + IV - all encrypted.
	 * @throws GeneralSecurityException if there's a problem {@link CryptoRegistry#createCipher(String) obtaining the cipher from the CryptoRegistry}.
	 * @throws IOException if decoding the public key from its binary representation fails.
	 * @throws CryptoException in case the encryption fails.
	 * @see #encryptKey(byte[], Cipher)
	 * @see #decryptKey(Cipher, byte[])
	 */
	public static byte[] encryptKey(byte[] key, String keyEncryptionTransformation, byte[] keyEncryptionPublicKey)
	throws GeneralSecurityException, IOException, CryptoException
	{
		Cipher keyEncrypter = CryptoRegistry.sharedInstance().createCipher(keyEncryptionTransformation);
		CipherParameters publicKey = CryptoRegistry.sharedInstance().decodePublicKey(keyEncryptionPublicKey);
		keyEncrypter.init(CipherOperationMode.ENCRYPT, publicKey);
		byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, keyEncrypter);
		return keyEncodedEncrypted;
	}

	/**
	 * Decrypt a previously {@link #encryptKey(byte[], String, byte[]) encrypted} secret key and verify its integrity
	 * via a MAC.
	 *
	 * @param decrypter the cipher to be used for decryption (already initialised with key + IV).
	 * @param keyEncodedEncrypted the encrypted key as produced by {@link #encryptKey(byte[], Cipher)}
	 * @return the decrypted secret key (as originally passed to {@link #encryptKey(byte[], Cipher)}.
	 * @throws CryptoException if decryption failed.
	 * @throws IOException if data cannot be read or is corrupted - e.g. if MAC verification failed.
	 * @throws NoSuchAlgorithmException if the {@link CryptoRegistry} does not know the (MAC) algorithm.
	 * @see #encryptKey(byte[], Cipher)
	 * @see #encryptKey(byte[], String, byte[])
	 */
	public static byte[] decryptKey(Cipher decrypter, byte[] keyEncodedEncrypted) throws CryptoException, IOException, NoSuchAlgorithmException
	{
		int encryptedOff = 0;
		int version = keyEncodedEncrypted[encryptedOff++] & 0xff;
		if (version != 1)
			throw new IllegalArgumentException("keyEncodedEncrypted is of version " + version + " which is not supported!");

		int macKeyLength = keyEncodedEncrypted[encryptedOff++] & 0xff;
		int macIVLength = keyEncodedEncrypted[encryptedOff++] & 0xff;
		int macLength = keyEncodedEncrypted[encryptedOff++] & 0xff;

		int outputSize = decrypter.getOutputSize(keyEncodedEncrypted.length - encryptedOff);
		byte[] out = new byte[outputSize];

		int outOff = 0;
		outOff += decrypter.update(keyEncodedEncrypted, encryptedOff, keyEncodedEncrypted.length - encryptedOff, out, outOff);
		outOff += decrypter.doFinal(out, outOff);

		int dataOff = 0;
		MACCalculator macCalculator = CryptoRegistry.sharedInstance().createMACCalculator(MAC_ALGORITHM, false);

		CipherParameters macKeyParam = new KeyParameter(out, 0, macKeyLength);
		dataOff += macKeyLength;

		CipherParameters macParams;
		if (macIVLength == 0)
			macParams = macKeyParam;
		else {
			macParams = new ParametersWithIV(macKeyParam, out, dataOff, macIVLength);
			dataOff += macIVLength;
		}

		macCalculator.init(macParams);

		int dataLength = outOff - dataOff - macLength;
		int macOff = dataOff + dataLength;

		if (macCalculator != null) {
			byte[] newMAC = new byte[macCalculator.getMacSize()];
			macCalculator.update(out, dataOff, dataLength);
			macCalculator.doFinal(newMAC, 0);

			if (newMAC.length != macLength)
				throw new IOException("MACs have different length! Expected MAC has " + macLength + " bytes and newly calculated MAC has " + newMAC.length + " bytes!");

			for (int i = 0; i < macLength; ++i) {
				byte expected = out[macOff + i];
				if (expected != newMAC[i])
					throw new IOException("MAC mismatch! mac[" + i + "] was expected to be " + expected + " but was " + newMAC[i]);
			}
		}

		byte[] decrypted = new byte[dataLength];
		System.arraycopy(out, dataOff, decrypted, 0, decrypted.length);

		return decrypted;
	}
}
