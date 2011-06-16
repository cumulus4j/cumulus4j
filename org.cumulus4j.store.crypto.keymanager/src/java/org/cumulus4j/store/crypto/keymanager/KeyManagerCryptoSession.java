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

import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.util.ChecksumAlgorithm;
import org.cumulus4j.crypto.util.ChecksumCalculator;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyResponse;
import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.keymanager.back.shared.KeyEncryptionUtil;
import org.cumulus4j.store.crypto.AbstractCryptoSession;
import org.cumulus4j.store.crypto.Ciphertext;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.crypto.Plaintext;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBroker;
import org.cumulus4j.store.crypto.keymanager.messagebroker.MessageBrokerRegistry;
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

	private static final ChecksumAlgorithm _activeChecksumAlgorithm = ChecksumAlgorithm.SHA1;
	private ChecksumAlgorithm getActiveChecksumAlgorithm()
	{
		return _activeChecksumAlgorithm;
	}

	private static final EncryptionAlgorithm _activeEncryptionAlgorithm = EncryptionAlgorithm.Twofish_CBC_PKCS5Padding; // TODO this should be configurable!
	private EncryptionAlgorithm getActiveEncryptionAlgorithm()
	{
		return _activeEncryptionAlgorithm;
	}

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
	private static final String keyEncryptionTransformation = "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING";

	/**
	 * {@inheritDoc}
	 * <p>
	 * The implementation in {@link KeyManagerCryptoManager} stores every plaintext
	 * encoded in the following form:
	 * </p>
	 * <table border="1">
	 * <tbody>
	 * 	<tr>
	 * 		<td align="right" valign="top"><b>Bytes</b></td><td><b>Description</b></td>
	 * 	</tr><tr>
	 * 		<td align="right" valign="top">1</td><td>Version number</td>
	 * 	</tr><tr>
	 * 		<td align="right" valign="top">1</td><td>{@link EncryptionCoordinateSet}'s ordinal value</td>
	 * 	</tr><tr>
	 * 		<td align="right" valign="top">1</td><td><i>ivLen</i>: Length of the IV in bytes</td>
	 * 	</tr><tr>
	 * 		<td align="right" valign="top"><i>ivLen</i></td><td>Actual IV (initialisation vector).</td>
	 * 	</tr><tr>
	 *		<td align="right" valign="top">1</td><td>{@link ChecksumAlgorithm#toByte()}</td>
	 *	</tr><tr>
	 *		<td align="right" valign="top">1</td><td><i>checksumLen</i>: Length of the checksum in bytes</td>
	 * 	</tr><tr>
	 * 		<td colspan="2">
	 * 			<table bgcolor="#F0F0F0" border="1" width="100%">
	 * 			<tbody>
	 * 				<tr>
	 * 					<td bgcolor="#D0D0D0" colspan="2"><b>ENCRYPTED</b></td>
	 * 				</tr><tr>
	 * 					<td align="right" valign="top"><b>Bytes</b></td><td><b>Description</b></td>
	 *				</tr><tr>
	 *					<td align="right" valign="top"><i>checksumLen</i></td><td>Actual checksum</td>
	 *				</tr><tr>
	 *					<td align="right" valign="top"><i>all following</i></td><td>Actual data</td>
	 * 				</tr>
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
		EncryptionAlgorithm activeEncryptionAlgorithm = getActiveEncryptionAlgorithm();
		ChecksumAlgorithm activeChecksumAlgorithm = getActiveChecksumAlgorithm();
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
			byte[] checksum = checksumCalculator.checksum(plaintext.getData(), activeChecksumAlgorithm);
			byte[] iv = ((ParametersWithIV)cipher.getParameters()).getIV();

			if (checksum.length > 255)
				throw new IllegalStateException("checksum.length > 255");

			int outLength = (
					1 // version
					+ 1 // encryption algorithm
					+ 1 // IV length in bytes
					+ iv.length // actual IV
					+ 1 // checksum algorithm
					+ 1 // checksum length in bytes
					+ cipher.getOutputSize(
							checksum.length // actual checksum
							+ plaintext.getData().length // actual plaintext
					)
			);

			byte[] out = new byte[outLength];
			int outOff = 0;
			out[outOff++] = 1; // version 1
			out[outOff++] = activeEncryptionAlgorithm.toByte();

			// IV length
			out[outOff++] = (byte)iv.length;

			// actual IV
			System.arraycopy(iv, 0, out, outOff, iv.length);
			outOff += iv.length;

			out[outOff++] = activeChecksumAlgorithm.toByte();
			out[outOff++] = (byte)checksum.length;

			outOff += cipher.update(checksum, 0, checksum.length, out, outOff);
			outOff += cipher.update(plaintext.getData(), 0, plaintext.getData().length, out, outOff);
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

			EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.valueOf(in[inOff++]);

			int ivLength = in[inOff++] & 0xff;
			byte[] iv = new byte[ivLength];
			System.arraycopy(in, inOff, iv, 0, iv.length);
			inOff += iv.length;

			ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.valueOf(in[inOff++]);
			int checksumLength = (in[inOff++]) & 0xff;

			decrypter = cipherCache.acquireDecrypter(encryptionAlgorithm, keyID, iv);
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

				decrypter = cipherCache.acquireDecrypter(encryptionAlgorithm, keyID, keyEncodedPlain, iv);
			}

			int inCryptLength = in.length - inOff;
			int outLength = decrypter.getCipher().getOutputSize(inCryptLength);
			byte[] out = new byte[outLength];
			int outOff = 0;
			outOff += decrypter.getCipher().update(in, inOff, inCryptLength, out, 0);
			outOff += decrypter.getCipher().doFinal(out, outOff);

			if (logger.isDebugEnabled() && outOff != outLength)
				logger.debug("decrypt: precalculated output-size does not match actually written output: expected={} actual={}", outLength, outOff);

			int checksumOff = 0;
			int dataOff = checksumOff + checksumLength;
			int dataLength = outOff - dataOff;
			byte[] newChecksum = checksumCalculator.checksum(out, dataOff, dataLength, checksumAlgorithm);

			if (newChecksum.length != checksumLength)
				throw new IOException("Checksums have different length! Expected checksum has " + checksumLength + " bytes and newly calculated checksum has " + newChecksum.length + " bytes!");

			for (int i = 0; i < newChecksum.length; ++i) {
				byte expected = out[checksumOff + i];
				if (expected != newChecksum[i])
					throw new IOException("Checksum mismatch! checksum[" + i + "] expected " + expected + " but was " + newChecksum[i]);
			}
			newChecksum = null;

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

	private ChecksumCalculator checksumCalculator = new ChecksumCalculator();

	@Override
	public void close() {
		super.close();

		// Our caches are used across multiple sessions for performance reasons,
		// hence we cannot close the caches here.
	}

}
