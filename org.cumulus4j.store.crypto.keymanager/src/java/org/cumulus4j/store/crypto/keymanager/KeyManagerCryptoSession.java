package org.cumulus4j.store.crypto.keymanager;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.Security;

import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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

	private static final BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
	static {
		Security.insertProviderAt(bouncyCastleProvider, 2);
	}

	private static final ChecksumAlgorithm _activeChecksumAlgorithm = ChecksumAlgorithm.CRC32;
	private ChecksumAlgorithm getActiveChecksumAlgorithm()
	{
		return _activeChecksumAlgorithm;
	}

	private static final EncryptionAlgorithm _activeEncryptionAlgorithm = EncryptionAlgorithm.AES_CBC_PKCS5Padding; // TODO this should be configurable!
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

	private SecureRandom secureRandom = new SecureRandom();

//	private static KeyPair keyEncryptionKeyPair;
//
//	private static javax.crypto.Cipher keyDecrypter;
//
//	private static long keyDecrypterCreationTimestamp = Long.MIN_VALUE;
//
//	private static final long keyDecrypterLifetimeMSec = 12L * 3600L * 1000L; // TODO make configurable! - 12 hours right now
//
//	private static javax.crypto.Cipher getKeyDecrypter()
//	{
//		if (keyDecrypter == null || System.currentTimeMillis() - keyDecrypterCreationTimestamp > keyDecrypterLifetimeMSec) {
//			try {
//				String rawAlgo = KeyEncryptionUtil.getRawEncryptionAlgorithmWithoutModeAndPadding(keyEncryptionAlgorithm);
//				KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(rawAlgo);
//				if ("RSA".equals(rawAlgo)) {
//					 RSAKeyGenParameterSpec rsaParamGenSpec = new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4);
//					 keyPairGenerator.initialize(rsaParamGenSpec);
//				}
//
//				keyEncryptionKeyPair = keyPairGenerator.genKeyPair();
//
//				keyDecrypter = javax.crypto.Cipher.getInstance(keyEncryptionAlgorithm);
//				keyDecrypter.init(javax.crypto.Cipher.DECRYPT_MODE, keyEncryptionKeyPair.getPrivate());
//			} catch (Exception e) {
//				throw new RuntimeException(e);
//			}
//		}
//		return keyDecrypter;
//	}

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
	 * 	</tr>
	 * 	<tr>
	 * 		<td align="right" valign="top">1</td><td>{@link EncryptionAlgorithm}'s ordinal value</td>
	 * 	</tr><tr>
	 * 		<td align="right" valign="top">1</td><td><i>ivLen</i>: Length of the IV in bytes.</td>
	 * 	</tr><tr>
	 * 		<td align="right" valign="top"><i>ivLen</i></td><td>The actual IV (initialisation vector).</td>
	 * 	</tr><tr>
	 * 		<td colspan="2">
	 * 			<table bgcolor="#F0F0F0" border="1" width="100%">
	 * 			<tbody>
	 * 				<tr>
	 * 					<td bgcolor="#D0D0D0" colspan="2"><b>ENCRYPTED</b></td>
	 * 				</tr><tr>
	 * 					<td align="right" valign="top"><b>Bytes</b></td><td><b>Description</b></td>
	 * 				</tr><tr>
	 *					<td align="right" valign="top">1</td><td><i>salt0</i>: Salt for checksum algorithm</td>
	 *				</tr><tr>
	 *					<td align="right" valign="top">1</td><td><i>salt1</i>: Salt for checksum length</td>
	 * 				</tr><tr>
	 *					<td align="right" valign="top">1</td><td>{@link ChecksumAlgorithm#toByte()} XORed with <i>salt0</i>.</td>
	 *				</tr><tr>
	 *					<td align="right" valign="top">1</td><td><i>checksumLen</i>: Length of the checksum in bytes XORed with <i>salt1</i>.</td>
	 *				</tr><tr>
	 *					<td align="right" valign="top"><i>checksumLen</i></td><td>The actual checksum.</td>
	 *				</tr><tr>
	 *					<td align="right" valign="top"><i>all following</i></td><td>The actual data.</td>
	 * 				</tr>
	 * 			</tbody>
	 * 			</table>
	 * 		</td>
	 * 	</tr>
	 * </tbody>
	 * </table>
	 */
	@Override
	public Ciphertext encrypt(Plaintext plaintext)
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
//				javax.crypto.Cipher keyDecrypter;
//				KeyPair keyEncryptionKeyPair;
//				synchronized (KeyManagerCryptoSession.class) {
//					keyDecrypter = KeyManagerCryptoSession.getKeyDecrypter();
//					keyEncryptionKeyPair = KeyManagerCryptoSession.keyEncryptionKeyPair;
//				}
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

			int outLength = (
					1 // encryption algorithm
					+ 1 // iv length in bytes
					+ iv.length
					+ cipher.getOutputSize(
							1 // random salt to xor into checksum algorithm
							+ 1 // random salt to xor into checksum length
							+ 1 // checksum algorithm
							+ 1 // checksum length in bytes
							+ checksum.length // actual checksum
							+ plaintext.getData().length
					)
			);

			byte[] out = new byte[outLength];
			int outOff = 0;
			out[outOff++] = (byte)activeEncryptionAlgorithm.ordinal();
			out[outOff++] = (byte)iv.length;

			System.arraycopy(iv, 0, out, outOff, iv.length);
			outOff += iv.length;

			byte[] saltForChecksumMeta = new byte[2];
			secureRandom.nextBytes(saltForChecksumMeta);

			outOff += cipher.update(saltForChecksumMeta, 0, saltForChecksumMeta.length, out, outOff);
			outOff += cipher.update((byte)(saltForChecksumMeta[0] ^ activeChecksumAlgorithm.toByte()), out, outOff);
			outOff += cipher.update((byte)(saltForChecksumMeta[1] ^ checksum.length), out, outOff);
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
	public Plaintext decrypt(Ciphertext ciphertext)
	{
		CipherCache cipherCache = ((KeyManagerCryptoManager)getCryptoManager()).getCipherCache();

		CipherCacheKeyDecrypterEntry keyDecryptor = null;
		CipherCacheCipherEntry decrypter = null;
		try {
			long keyID = ciphertext.getKeyID();
			int inOff = 0;
			int encryptionAlgoID = ciphertext.getData()[inOff++] & 0xff;
			EncryptionAlgorithm encryptionAlgorithm = EncryptionAlgorithm.values()[encryptionAlgoID];

			int ivLength = ciphertext.getData()[inOff++] & 0xff;
			byte[] iv = new byte[ivLength];
			System.arraycopy(ciphertext.getData(), inOff, iv, 0, iv.length);
			inOff += iv.length;

			decrypter = cipherCache.acquireDecrypter(encryptionAlgorithm, keyID, iv);
			if (decrypter == null) {
//				javax.crypto.Cipher keyDecrypter;
//				KeyPair keyEncryptionKeyPair;
//				synchronized (KeyManagerCryptoSession.class) {
//					keyDecrypter = KeyManagerCryptoSession.getKeyDecrypter();
//					keyEncryptionKeyPair = KeyManagerCryptoSession.keyEncryptionKeyPair;
//				}
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

			int inLength = ciphertext.getData().length - inOff;
			int outLength = decrypter.getCipher().getOutputSize(inLength);
			byte[] out = new byte[outLength];
			int outOff = 0;
			outOff += decrypter.getCipher().update(ciphertext.getData(), inOff, inLength, out, 0);
			outOff += decrypter.getCipher().doFinal(out, outOff);

			if (logger.isDebugEnabled() && outOff != outLength)
				logger.debug("decrypt: precalculated output-size does not match actually written output: expected={} actual={}", outLength, outOff);

			ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.valueOf((byte)(out[0] ^ out[2]));
			int checksumLength = (out[1] ^ out[3]) & 0xff;

			int checksumOff = 4; // after 2 bytes salt + 1 byte checksumAlgoID + 1 byte checksumLength
			int dataOff = checksumOff + checksumLength;
			byte[] newChecksum = checksumCalculator.checksum(out, dataOff, outOff - dataOff, checksumAlgorithm);

			for (int i = 0; i < newChecksum.length; ++i) {
				byte expected = out[checksumOff + i];
				if (expected != newChecksum[i])
					throw new IOException("Checksum mismatch! checksum[" + i + "] expected " + expected + " but was " + newChecksum[i]);
			}
			newChecksum = null;

			byte[] decrypted = new byte[outOff - dataOff];
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
