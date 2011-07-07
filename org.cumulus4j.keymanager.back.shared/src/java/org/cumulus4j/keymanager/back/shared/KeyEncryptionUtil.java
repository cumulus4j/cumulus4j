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
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class KeyEncryptionUtil
{
	private static final Logger logger = LoggerFactory.getLogger(KeyEncryptionUtil.class);

	private KeyEncryptionUtil() { }

	private static final String MAC_ALGORITHM = "HMAC-SHA1";

	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

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

	public static byte[] encryptKey(byte[] key, String keyEncryptionTransformation, byte[] keyEncryptionPublicKey)
	throws GeneralSecurityException, IOException, CryptoException
	{
		Cipher keyEncrypter = CryptoRegistry.sharedInstance().createCipher(keyEncryptionTransformation);
		CipherParameters publicKey = CryptoRegistry.sharedInstance().decodePublicKey(keyEncryptionPublicKey);
		keyEncrypter.init(CipherOperationMode.ENCRYPT, publicKey);
		byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, keyEncrypter);
		return keyEncodedEncrypted;
	}

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
			byte[] newMac = new byte[macCalculator.getMacSize()];
			macCalculator.update(out, dataOff, dataLength);
			macCalculator.doFinal(newMac, 0);

			if (newMac.length != macLength)
				throw new IOException("MACs have different length! Expected MAC has " + macLength + " bytes and newly calculated MAC has " + newMac.length + " bytes!");

			for (int i = 0; i < macLength; ++i) {
				byte expected = out[macOff + i];
				if (expected != newMac[i])
					throw new IOException("MAC mismatch! mac[" + i + "] was expected to be " + expected + " but was " + newMac[i]);
			}
		}

		byte[] decrypted = new byte[dataLength];
		System.arraycopy(out, dataOff, decrypted, 0, decrypted.length);

		return decrypted;
	}
}
