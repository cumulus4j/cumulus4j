package org.cumulus4j.store.crypto.keymanager;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;
import java.security.spec.RSAKeyGenParameterSpec;

import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
	private static final String keyEncryptionAlgorithm = "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING";

	private static KeyPair keyEncryptionKeyPair;

	private static javax.crypto.Cipher keyDecrypter;

	private static long keyDecrypterCreationTimestamp = Long.MIN_VALUE;

	private static final long keyDecrypterLifetimeMSec = 12L * 3600L * 1000L; // TODO make configurable! - 12 hours right now

	private static javax.crypto.Cipher getKeyDecrypter()
	{
		if (keyDecrypter == null || System.currentTimeMillis() - keyDecrypterCreationTimestamp > keyDecrypterLifetimeMSec) {
			try {
				String rawAlgo = KeyEncryptionUtil.getRawEncryptionAlgorithmWithoutModeAndPadding(keyEncryptionAlgorithm);
				KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(rawAlgo);
				if ("RSA".equals(rawAlgo)) {
					 RSAKeyGenParameterSpec rsaParamGenSpec = new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4);
					 keyPairGenerator.initialize(rsaParamGenSpec);
				}

				keyEncryptionKeyPair = keyPairGenerator.genKeyPair();

				keyDecrypter = javax.crypto.Cipher.getInstance(keyEncryptionAlgorithm);
				keyDecrypter.init(javax.crypto.Cipher.DECRYPT_MODE, keyEncryptionKeyPair.getPrivate());
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return keyDecrypter;
	}

	@Override
	public Ciphertext encrypt(Plaintext plaintext)
	{
		EncryptionAlgorithm activeEncryptionAlgorithm = getActiveEncryptionAlgorithm();
		ChecksumAlgorithm activeChecksumAlgorithm = getActiveChecksumAlgorithm();
		CipherCache cipherCache = ((KeyManagerCryptoManager)getCryptoManager()).getCipherCache();

		CipherCacheCipherEntry encrypter = null;
		try {
			long activeEncryptionKeyID = cipherCache.getActiveEncryptionKeyID();
			if (activeEncryptionKeyID >= 0)
				encrypter = cipherCache.acquireEncrypter(activeEncryptionAlgorithm, activeEncryptionKeyID);

			if (encrypter == null) {
				javax.crypto.Cipher keyDecrypter;
				KeyPair keyEncryptionKeyPair;
				synchronized (KeyManagerCryptoSession.class) {
					keyDecrypter = KeyManagerCryptoSession.getKeyDecrypter();
					keyEncryptionKeyPair = KeyManagerCryptoSession.keyEncryptionKeyPair;
				}

				GetActiveEncryptionKeyResponse getActiveEncryptionKeyResponse;
				try {
					GetActiveEncryptionKeyRequest getActiveEncryptionKeyRequest = new GetActiveEncryptionKeyRequest(
							getCryptoSessionID(), keyEncryptionAlgorithm, keyEncryptionKeyPair.getPublic().getEncoded()
					);
					getActiveEncryptionKeyResponse = getMessageBroker().query(
							GetActiveEncryptionKeyResponse.class,
							getActiveEncryptionKeyRequest
					);
				} catch (Exception e) {
					logger.warn("Could not query active encryption key: " + e, e);
					throw new RuntimeException(e);
				}

				byte[] keyEncodedPlain;
				synchronized (keyDecrypter) {
					keyEncodedPlain = KeyEncryptionUtil.decryptKey(keyDecrypter, getActiveEncryptionKeyResponse.getKeyEncodedEncrypted());
				}
				activeEncryptionKeyID = getActiveEncryptionKeyResponse.getKeyID();
				cipherCache.setActiveEncryptionKeyID(activeEncryptionKeyID, getActiveEncryptionKeyResponse.getActiveUntilExcl());
				encrypter = cipherCache.acquireEncrypter(activeEncryptionAlgorithm, activeEncryptionKeyID, keyEncodedPlain);
			}

			byte[] checksum = checksumCalculator.checksum(plaintext.getData(), activeChecksumAlgorithm);
			byte[] iv = ((ParametersWithIV)encrypter.getCipher().getParameters()).getIV();

			int outLength = (
					1 // encryption algorithm
					+ 1 // iv length in bytes
					+ iv.length
					+ encrypter.getCipher().getOutputSize(
							1 // checksum algorithm
							+ 1 // checksum length in bytes
							+ checksum.length
							+ plaintext.getData().length
					)
			);

			byte[] out = new byte[outLength];
			int outOff = 0;
			out[outOff++] = (byte)activeEncryptionAlgorithm.ordinal();
			out[outOff++] = (byte)iv.length;

			System.arraycopy(iv, 0, out, outOff, iv.length);
			outOff += iv.length;

			outOff += encrypter.getCipher().update(activeChecksumAlgorithm.toByte(), out, outOff);
			outOff += encrypter.getCipher().update((byte)checksum.length, out, outOff);
			outOff += encrypter.getCipher().update(checksum, 0, checksum.length, out, outOff);
			outOff += encrypter.getCipher().update(plaintext.getData(), 0, plaintext.getData().length, out, outOff);
			outOff += encrypter.getCipher().doFinal(out, outOff);

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
			cipherCache.releaseCipherEntry(encrypter);
		}
	}

	@Override
	public Plaintext decrypt(Ciphertext ciphertext)
	{
		CipherCache cipherCache = ((KeyManagerCryptoManager)getCryptoManager()).getCipherCache();

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
				javax.crypto.Cipher keyDecrypter;
				KeyPair keyEncryptionKeyPair;
				synchronized (KeyManagerCryptoSession.class) {
					keyDecrypter = KeyManagerCryptoSession.getKeyDecrypter();
					keyEncryptionKeyPair = KeyManagerCryptoSession.keyEncryptionKeyPair;
				}

				GetKeyResponse getKeyResponse;
				try {
					GetKeyRequest getKeyRequest = new GetKeyRequest(
							getCryptoSessionID(), ciphertext.getKeyID(),
							keyEncryptionAlgorithm, keyEncryptionKeyPair.getPublic().getEncoded()
					);
					getKeyResponse = getMessageBroker().query(
							GetKeyResponse.class, getKeyRequest
					);
				} catch (Exception e) {
					logger.warn("Could not query key " + ciphertext.getKeyID() + ": " + e, e);
					throw new RuntimeException(e);
				}

				byte[] keyEncodedPlain;
				synchronized (keyDecrypter) {
					keyEncodedPlain = KeyEncryptionUtil.decryptKey(keyDecrypter, getKeyResponse.getKeyEncodedEncrypted());
				}

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

			ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.valueOf(out[0]);
			int checksumLength = out[1] & 0xff;

			int checksumOff = 2; // after 1 byte checksumAlgoID + 1 byte checksumLength
			int dataOff = checksumOff + checksumLength;
			byte[] newChecksum = checksumCalculator.checksum(out, dataOff, outOff - dataOff, checksumAlgorithm);

			for (int i = 0; i < newChecksum.length; ++i) {
				if (newChecksum[i] != out[checksumOff + i])
					throw new IOException("Checksum mismatch! checksum[" + i + "] expected " + newChecksum[i] + " but was " + out[checksumOff + i]);
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
			cipherCache.releaseCipherEntry(decrypter);
		}
	}

	private ChecksumCalculator checksumCalculator = new ChecksumCalculator();

//	private byte[] encrypt(Cipher encrypter, byte[] input) throws GeneralSecurityException
//	{
//		int saltLength = getSaltLength(encrypter);
//		int resultSize = 1 /* encryptionAlgoID */ + encrypter.getOutputSize(saltLength + 1 /* checksum-algo-id */ + 4 /* checksum-length with CRC32 */ + input.length);
//
//		ByteBuffer resultBuf = ByteBuffer.allocate(resultSize);
//		resultBuf.position(1); // We'll write this 'encryptionAlgoID' outside of this method after encrypting.
//
//		{ // First, we put salt into the soup ;-)
//			// We start with the salt to make sure we have unpredictable plain-text already at the very beginning to
//			// make attacks based on known plain-text impossible.
//			byte[] salt = new byte[saltLength];
//			random.nextBytes(salt);
//			encrypter.update(ByteBuffer.wrap(salt), resultBuf);
//		}
//
//		// Now we add the check-sum.
//		// It always starts with the algo-identifier, followed by the checksum itself.
//		switch (activeChecksumAlgorithm) {
//			case none: {
//				checksum = new byte[1];
//				checksum[0] = (byte)activeChecksumAlgorithm.ordinal();
//				break;
//			}
//			case crc32: {
//				checksum = new byte[5]; // CRC32 has 32 bit, i.e. 4 bytes PLUS 1 byte for the checksumAlgoID
//				checksum[0] = (byte)activeChecksumAlgorithm.ordinal();
//				CRC32 crc32 = new CRC32();
//				crc32.update(input);
//				long crc32Value = crc32.getValue();
//				for (int i = 1; i < checksum.length; ++i)
//					checksum[i] = (byte)(crc32Value >>> ((i - 1) * 8));
//
//				break;
//			}
//			default:
//				throw new IllegalStateException("Unsupported ChecksumAlgorithm: " + activeChecksumAlgorithm);
//		}
//		encrypter.update(ByteBuffer.wrap(checksum), resultBuf);
//
//		encrypter.doFinal(ByteBuffer.wrap(input), resultBuf);
//		if (resultBuf.hasArray()) {
//			if (resultBuf.array().length == resultBuf.position())
//				return resultBuf.array();
//			else
//				logger.warn("Backing array cannot be directly used, because its size ({}) does not match the required size ({})!", resultBuf.array().length, resultBuf.position());
//		}
//		else
//			logger.warn("Backing array cannot be directly used, because there is no backing array!");
//
//		byte[] result = new byte[resultBuf.position()];
//		resultBuf.rewind();
//		resultBuf.get(result);
//		return result;
//	}
//
//	private int getSaltLength(Cipher cipher)
//	{
//		int saltLength = cipher.getBlockSize();
//		if (saltLength < 1)
//			saltLength = 16;
//
//		return saltLength;
//	}
//
//	private byte[] decrypt(Cipher decrypter, byte[] input) throws GeneralSecurityException, IOException
//	{
//		// Performance tests in CryptoAlgoBenchmark show that ByteBuffer is as fast as System.arraycopy(...) after
//		// the JVM had time for hotspot-compilation (only at the beginning there are slight performance differences).
//		// Hence we use the nicer API. Marco :-)
//		ByteBuffer inputBuf = ByteBuffer.wrap(input);
//
//		// Skip 1st byte containing the encryptionAlgoID (handled outside of this method).
//		inputBuf.position(1);
//
//		int rawPlainSize = decrypter.getOutputSize(input.length - inputBuf.position()); // '- inputBuf.position()' because of first skipped byte
//		ByteBuffer rawBuf = ByteBuffer.allocate(rawPlainSize);
//
//		int bytesDecryptedCount = decrypter.doFinal(inputBuf, rawBuf);
//
//		// Discard the salt, the first byte we're interested in is the checksum-id AFTER the salt.
//		int saltLength = getSaltLength(decrypter);
//		rawBuf.position(saltLength);
//
//		int checksumAlgoID = rawBuf.get() & 0xff; // the '& 0xff' is necessary to use the whole UNSIGNED range of a byte, i.e. 0...255.
//		if (checksumAlgoID > ChecksumAlgorithm.values().length - 1)
//			throw new IllegalArgumentException("input[" + (rawBuf.position() - 1) + "] == checksumAlgoID == " + checksumAlgoID + " is unknown!");
//
//		ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.values()[checksumAlgoID];
//		byte[] checksum;
//		switch (checksumAlgorithm) {
//			case none: {
//				checksum = new byte[0];
//				break;
//			}
//			case crc32: {
//				checksum = new byte[4]; // CRC32 has 32 bit, i.e. 4 bytes
////				System.arraycopy(raw, saltLength + 1, checksum, 0, checksum.length);
//				rawBuf.get(checksum);
//				break;
//			}
//			default:
//				throw new IllegalStateException("Unsupported ChecksumAlgorithm: " + activeChecksumAlgorithm);
//		}
//
////		byte[] result = new byte[raw.length - saltLength - 1 /* checksumAlgoID */ - checksum.length];
////		System.arraycopy(raw, saltLength + 1 + checksum.length, result, 0, result.length);
//		byte[] result = new byte[bytesDecryptedCount - rawBuf.position()];
//		rawBuf.get(result);
//
//		// And finally calculate a new checksum and verify if it matches the one we read above.
//		switch (checksumAlgorithm) {
//			case none: // nothing to do
//				break;
//			case crc32: {
//				CRC32 crc32 = new CRC32();
//				crc32.update(result);
//				long crc32Value = crc32.getValue();
//				for (int i = 0; i < checksum.length; ++i) {
//					if (checksum[i] != (byte)(crc32Value >>> (i * 8)))
//						throw new IOException("CRC32 checksum mismatch!");
//				}
//
//				break;
//			}
//			default:
//				throw new IllegalStateException("Unsupported ChecksumAlgorithm: " + activeChecksumAlgorithm);
//		}
//
//		return result;
//	}

	@Override
	public void close() {
		super.close();

		// TODO clear caches.
	}

}
