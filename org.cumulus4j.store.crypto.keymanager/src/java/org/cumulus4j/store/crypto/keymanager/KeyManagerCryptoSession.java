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
package org.cumulus4j.store.crypto.keymanager;

import java.io.IOException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.crypto.MacCalculator;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyResponse;
import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.keymanager.back.shared.KeyEncryptionUtil;
import org.cumulus4j.store.crypto.AbstractCryptoSession;
import org.cumulus4j.store.crypto.Ciphertext;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.Plaintext;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBroker;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBrokerRegistry;
import org.cumulus4j.store.model.EncryptionCoordinateSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Implementation of {@link org.cumulus4j.store.crypto.CryptoSession CryptoSession} working with a
 * key-manager as shown in <a href="http://cumulus4j.org/documentation/deployment-scenarios.html">Deployment scenarios</a>.
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyManagerCryptoSession
extends AbstractCryptoSession
{
	private static final Logger logger = LoggerFactory.getLogger(KeyManagerCryptoSession.class);

//	private static final BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
//	static {
//		Security.insertProviderAt(bouncyCastleProvider, 2);
//	}

//	private static final ChecksumAlgorithm _activeChecksumAlgorithm = ChecksumAlgorithm.SHA1;
//	private ChecksumAlgorithm getActiveChecksumAlgorithm()
//	{
//		return _activeChecksumAlgorithm;
//	}
//
//	private static final EncryptionAlgorithm _activeEncryptionAlgorithm = EncryptionAlgorithm.Twofish_CBC_PKCS5Padding; // TODO this should be configurable!
//	private EncryptionAlgorithm getActiveEncryptionAlgorithm()
//	{
//		return _activeEncryptionAlgorithm;
//	}

	private MessageBroker getMessageBroker() {
		return MessageBrokerRegistry.sharedInstance().getActiveMessageBroker();
	}

	/**
	 * <p>
	 * The <b>a</b>symmetric encryption algorithm used to encrypt the keys when they are sent from key-manager
	 * to here (app-server).
	 * </p>
	 * <p>
	 * Alternatively, we could use "EC": http://en.wikipedia.org/wiki/Elliptic_curve_cryptography
	 * </p>
	 */
	private static final String keyEncryptionTransformation = "RSA//OAEPWITHSHA1ANDMGF1PADDING";

	private static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

	/**
	 * {@inheritDoc}
	 * <p>
	 * The implementation in {@link KeyManagerCryptoSession} stores every plaintext
	 * encoded in the following form:
	 * </p>
	 * <table border="1">
	 * <tbody>
	 * 	<tr>
	 * 		<td align="right" valign="top"><b>Bytes</b></td><td><b>Description</b></td>
	 * 	</tr><tr>
	 * 		<td align="right" valign="top">1</td><td>Version number</td>
	 * 	</tr><tr>
	 * 		<td align="right" valign="top">2</td><td>{@link EncryptionCoordinateSet#getEncryptionCoordinateSetID()} (only 2 bytes, thus limiting to 65K possible values)</td>
	 * 	</tr><tr>
	 * 		<td align="right" valign="top">1</td><td><i>ivLen</i>: Length of the IV in bytes</td>
	 * 	</tr><tr>
	 * 		<td align="right" valign="top"><i>ivLen</i></td><td>Actual IV (random initialisation vector).</td>
	 * 	</tr><tr>
	 *		<td align="right" valign="top">1</td><td><i>macKeyLen</i>: <a href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a>'s key length in bytes</td>
	 * 	</tr><tr>
	 *		<td align="right" valign="top">1</td><td><i>macIVLen</i>: MAC's IV length in bytes</td>
	 *	</tr><tr>
	 *		<td align="right" valign="top">1</td><td><i>macLen</i>: Actual MAC's length in bytes</td>
	 * 	</tr><tr>
	 * 		<td colspan="2">
	 * 			<table bgcolor="#F0F0F0" border="1" width="100%">
	 * 			<tbody>
	 * 				<tr>
	 * 					<td bgcolor="#D0D0D0" colspan="2"><b>ENCRYPTED</b></td>
	 * 				</tr><tr>
	 * 					<td align="right" valign="top"><b>Bytes</b></td><td><b>Description</b></td>
	 *				</tr><tr>
	 *					<td align="right" valign="top"><i>macKeyLen</i></td><td>MAC's key (random)</td>
	 *				</tr><tr>
	 *					<td align="right" valign="top"><i>macIVLen</i></td><td>MAC's IV (random)</td>
	 *				</tr><tr>
	 *					<td align="right" valign="top"><i>all until MAC</i></td><td>Actual data</td>
	 * 				</tr><tr>
	 *					<td align="right" valign="top"><i>macLen</i></td><td>Actual MAC</td>
	 *				</tr>
	 * 			</tbody>
	 * 			</table>
	 * 		</td>
	 * 	</tr>
	 * </tbody>
	 * </table>
	 */
	@Override
	public Ciphertext encrypt(CryptoContext cryptoContext, Plaintext plaintext)
	{
		EncryptionCoordinateSet encryptionCoordinateSet = cryptoContext.getEncryptionCoordinateSetManager().createEncryptionCoordinateSet(
				cryptoContext.getPersistenceManagerConnection(),
				getCryptoManager().getEncryptionAlgorithm(),
				getCryptoManager().getMacAlgorithm()
		);
		String activeEncryptionAlgorithm = encryptionCoordinateSet.getCipherTransformation();

		if (encryptionCoordinateSet.getEncryptionCoordinateSetID() < 0)
			throw new IllegalStateException("The encryptionCoordinateSetID is out of range! It must be >= 0!!!");

		if (encryptionCoordinateSet.getEncryptionCoordinateSetID() > (2 * Short.MAX_VALUE))
			throw new IllegalStateException("The encryptionCoordinateSetID is out of range! The maximum is " + (2 * Short.MAX_VALUE) + ", because the value is encoded as UNsigned 2-byte-number! This means, you changed the encryption algorithm or the MAC algorithm too often. Switch back to settings you already used before!");

		CipherCache cipherCache = ((KeyManagerCryptoManager)getCryptoManager()).getCipherCache();

		CipherCacheKeyDecrypterEntry keyDecryptor = null;
		CipherCacheCipherEntry encrypter = null;
		try {
			long activeEncryptionKeyID = cipherCache.getActiveEncryptionKeyID();
			if (activeEncryptionKeyID >= 0)
				encrypter = cipherCache.acquireEncrypter(activeEncryptionAlgorithm, activeEncryptionKeyID);

			if (encrypter == null) {
				keyDecryptor = cipherCache.acquireKeyDecryptor(keyEncryptionTransformation);

				GetActiveEncryptionKeyResponse getActiveEncryptionKeyResponse;
				try {
					GetActiveEncryptionKeyRequest getActiveEncryptionKeyRequest = new GetActiveEncryptionKeyRequest(
							getCryptoSessionID(), keyEncryptionTransformation, keyDecryptor.getKeyEncryptionKey().getEncodedPublicKey()
					);
					getActiveEncryptionKeyResponse = getMessageBroker().query(
							GetActiveEncryptionKeyResponse.class,
							getActiveEncryptionKeyRequest
					);
				} catch (Exception e) {
					logger.warn("Could not query active encryption key: " + e, e);
					throw new RuntimeException(e);
				}

				byte[] keyEncodedPlain = KeyEncryptionUtil.decryptKey(keyDecryptor.getKeyDecryptor(), getActiveEncryptionKeyResponse.getKeyEncodedEncrypted());

				activeEncryptionKeyID = getActiveEncryptionKeyResponse.getKeyID();
				cipherCache.setActiveEncryptionKeyID(activeEncryptionKeyID, getActiveEncryptionKeyResponse.getActiveUntilExcl());
				encrypter = cipherCache.acquireEncrypter(activeEncryptionAlgorithm, activeEncryptionKeyID, keyEncodedPlain);
			}

			Cipher cipher = encrypter.getCipher();

			byte[] mac = EMPTY_BYTE_ARRAY;
			byte[] macKey = EMPTY_BYTE_ARRAY;
			byte[] macIV = EMPTY_BYTE_ARRAY;

			if (!CryptoManager.MAC_ALGORITHM_NONE.equals(encryptionCoordinateSet.getMacAlgorithm())) {
				MacCalculator macCalculator = CryptoRegistry.sharedInstance().createMacCalculator(encryptionCoordinateSet.getMacAlgorithm(), true);
				mac = macCalculator.doFinal(plaintext.getData());

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
			}

			byte[] iv = ((ParametersWithIV)cipher.getParameters()).getIV();

			if (iv.length > 255)
				throw new IllegalStateException("IV too long! Cannot encode length in 1 byte!");

			if (macKey.length > 255)
				throw new IllegalStateException("macKey too long! Cannot encode length in 1 byte!");

			if (macIV.length > 255)
				throw new IllegalStateException("macKey too long! Cannot encode length in 1 byte!");

			if (mac.length > 255)
				throw new IllegalStateException("mac too long! Cannot encode length in 1 byte!");

			int outLength = (
					1 // version
					+ 2 // encryptionCoordinateSetID
					+ 1 // IV length in bytes
					+ iv.length // actual IV
					+ 1 // macKeyLength in bytes
					+ 1 // macIVLength in bytes
					+ 1 // MAC length in bytes
					+ cipher.getOutputSize(
							macKey.length // actual MAC key
							+ macIV.length // actual MAC IV
							+ plaintext.getData().length // actual plaintext
							+ mac.length // actual MAC
					)
			);

			byte[] out = new byte[outLength];
			int outOff = 0;
			out[outOff++] = 1; // version 1

			// encryptionCoordinateSetID as UNsigned short
			out[outOff++] = (byte)(encryptionCoordinateSet.getEncryptionCoordinateSetID() >>> 8);
			out[outOff++] = (byte)encryptionCoordinateSet.getEncryptionCoordinateSetID();

			// IV length
			out[outOff++] = (byte)iv.length;

			// actual IV
			System.arraycopy(iv, 0, out, outOff, iv.length);
			outOff += iv.length;

			out[outOff++] = (byte)macKey.length;
			out[outOff++] = (byte)macIV.length;
			out[outOff++] = (byte)mac.length;

			outOff += cipher.update(macKey, 0, macKey.length, out, outOff);
			outOff += cipher.update(macIV, 0, macIV.length, out, outOff);
			outOff += cipher.update(plaintext.getData(), 0, plaintext.getData().length, out, outOff);
			outOff += cipher.update(mac, 0, mac.length, out, outOff);
			outOff += cipher.doFinal(out, outOff);

			if (outOff < outLength) {
				logger.warn(
						"encrypt: Output byte array was created bigger than necessary. Will shrink it now. outOff={} encryptedLength={}",
						outOff, outLength
				);
				byte tmp[] = new byte[outOff];
				System.arraycopy(out, 0, tmp, 0, tmp.length);
				out = tmp;
			}

			Ciphertext ciphertext = new Ciphertext();
			ciphertext.setData(out);
			ciphertext.setKeyID(activeEncryptionKeyID);
			return ciphertext;
		} catch (RuntimeException e) {
			logger.error("encrypt: " + e, e);
			throw e;
		} catch (Exception e) {
			logger.error("encrypt: " + e, e);
			throw new RuntimeException(e);
		} finally {
			cipherCache.releaseKeyDecryptor(keyDecryptor);
			cipherCache.releaseCipherEntry(encrypter);
		}
	}

	@Override
	public Plaintext decrypt(CryptoContext cryptoContext, Ciphertext ciphertext)
	{
		CipherCache cipherCache = ((KeyManagerCryptoManager)getCryptoManager()).getCipherCache();

		CipherCacheKeyDecrypterEntry keyDecryptor = null;
		CipherCacheCipherEntry decrypter = null;
		try {
			long keyID = ciphertext.getKeyID();
			int inOff = 0;
			byte[] in = ciphertext.getData();
			int version = in[inOff++] & 0xff;
			if (version != 1)
				throw new IllegalArgumentException("Ciphertext is of version " + version + " which is not supported!");

			int encryptionCoordinateSetID = (in[inOff++] << 8) & 0xffff;
			encryptionCoordinateSetID += in[inOff++] & 0xff;

			EncryptionCoordinateSet encryptionCoordinateSet = cryptoContext.getEncryptionCoordinateSetManager().getEncryptionCoordinateSet(
					cryptoContext.getPersistenceManagerConnection(), encryptionCoordinateSetID
			);
			if (encryptionCoordinateSet == null)
				throw new IllegalStateException("There is no EncryptionCoordinateSet with encryptionCoordinateSetID=" + encryptionCoordinateSetID + "!");

			int ivLength = in[inOff++] & 0xff;
			byte[] iv = new byte[ivLength];
			System.arraycopy(in, inOff, iv, 0, iv.length);
			inOff += iv.length;

			int macKeyLength = in[inOff++] & 0xff;
			int macIVLength = in[inOff++] & 0xff;
			int macLength = in[inOff++] & 0xff;

			decrypter = cipherCache.acquireDecrypter(encryptionCoordinateSet.getCipherTransformation(), keyID, iv);
			if (decrypter == null) {
				keyDecryptor = cipherCache.acquireKeyDecryptor(keyEncryptionTransformation);

				GetKeyResponse getKeyResponse;
				try {
					GetKeyRequest getKeyRequest = new GetKeyRequest(
							getCryptoSessionID(), ciphertext.getKeyID(),
							keyEncryptionTransformation, keyDecryptor.getKeyEncryptionKey().getEncodedPublicKey()
					);
					getKeyResponse = getMessageBroker().query(
							GetKeyResponse.class, getKeyRequest
					);
				} catch (Exception e) {
					logger.warn("Could not query key " + ciphertext.getKeyID() + ": " + e, e);
					throw new RuntimeException(e);
				}

				byte[] keyEncodedPlain = KeyEncryptionUtil.decryptKey(keyDecryptor.getKeyDecryptor(), getKeyResponse.getKeyEncodedEncrypted());

				decrypter = cipherCache.acquireDecrypter(encryptionCoordinateSet.getCipherTransformation(), keyID, keyEncodedPlain, iv);
			}

			int inCryptLength = in.length - inOff;
			int outLength = decrypter.getCipher().getOutputSize(inCryptLength);
			byte[] out = new byte[outLength];
			int outOff = 0;
			outOff += decrypter.getCipher().update(in, inOff, inCryptLength, out, 0);
			outOff += decrypter.getCipher().doFinal(out, outOff);

			if (logger.isDebugEnabled() && outOff != outLength)
				logger.debug("decrypt: precalculated output-size does not match actually written output: expected={} actual={}", outLength, outOff);

			int dataOff = 0;
			MacCalculator macCalculator = null;
			if (!CryptoManager.MAC_ALGORITHM_NONE.equals(encryptionCoordinateSet.getMacAlgorithm())) {
				macCalculator = CryptoRegistry.sharedInstance().createMacCalculator(encryptionCoordinateSet.getMacAlgorithm(), false);

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
			}

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
			Plaintext plaintext = new Plaintext();
			plaintext.setData(decrypted);
			return plaintext;
		} catch (RuntimeException e) {
			logger.error("decrypt: " + e, e);
			throw e;
		} catch (Exception e) {
			logger.error("decrypt: " + e, e);
			throw new RuntimeException(e);
		} finally {
			cipherCache.releaseKeyDecryptor(keyDecryptor);
			cipherCache.releaseCipherEntry(decrypter);
		}
	}

	@Override
	public void close() {
		super.close();

		// Our caches are used across multiple sessions for performance reasons,
		// hence we cannot close the caches here (maybe we might consider closing the
		// cache when the last session is closed, later).

		doNothing(); // suppress PMD warning - I want this overridden method here in this class for documentation reasons. Marco :-)
	}

	private static final void doNothing() { }
}
