package org.cumulus4j.store.crypto.keymanager;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.zip.CRC32;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
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

	private static final ChecksumAlgorithm encryptWithChecksum = ChecksumAlgorithm.crc32;
	private static final EncryptionAlgorithm DATA_ENCRYPTION_ALGORITHM = EncryptionAlgorithm.AES_CBC_PKCS5Padding; // TODO this should be configurable!
	private static final IvParameterSpec NULL_IV = new IvParameterSpec(new byte[16]); // TODO this should be determined based on the configured algorithm!

	private SecureRandom random = new SecureRandom();

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

	private static final KeyPair keyEncryptionKeyPair;

	private static final Cipher keyDecrypter;
//	private static final Cipher keyWrapper;

	static {
		try {
			String rawAlgo = KeyEncryptionUtil.getRawEncryptionAlgorithmWithoutModeAndPadding(keyEncryptionAlgorithm);
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(rawAlgo);
			if ("RSA".equals(rawAlgo)) {
				 RSAKeyGenParameterSpec rsaParamGenSpec = new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4);
				 keyPairGenerator.initialize(rsaParamGenSpec);
			}

			keyEncryptionKeyPair = keyPairGenerator.genKeyPair();

			keyDecrypter = Cipher.getInstance(keyEncryptionAlgorithm);
			keyDecrypter.init(Cipher.DECRYPT_MODE, keyEncryptionKeyPair.getPrivate());

//			keyWrapper = Cipher.getInstance(keyEncryptionAlgorithm);
//			keyWrapper.init(Cipher.WRAP_MODE, keyEncryptionKeyPair.getPrivate());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

//	public static void main(String[] args)
//	throws Exception
//	{
//		Key symmetricKey = KeyGenerator.getInstance("AES").generateKey();
//		byte[] encryptedKey = KeyEncryptionUtil.encryptKey(symmetricKey, keyEncryptionAlgorithm, keyEncryptionKeyPair.getPublic().getEncoded());
//		byte[] decryptedKey = KeyEncryptionUtil.decryptKey(keyDecrypter, encryptedKey);
//	}

	@Override
	public Ciphertext encrypt(Plaintext plaintext)
	{
		// TODO use a cache for this!!!
		GetKeyResponse getKeyResponse;
		try {
			GetActiveEncryptionKeyRequest getActiveEncryptionKeyRequest = new GetActiveEncryptionKeyRequest(
					getCryptoSessionID(), keyEncryptionAlgorithm, keyEncryptionKeyPair.getPublic().getEncoded()
			);
			getKeyResponse = getMessageBroker().query(
					GetKeyResponse.class,
					getActiveEncryptionKeyRequest
			);
		} catch (Exception e) {
			logger.warn("Could not query active encryption key: " + e, e);
			throw new RuntimeException(e);
		}

		// TODO cache ciphers/keys in general!
		try {
			byte[] keyEncodedPlain = KeyEncryptionUtil.decryptKey(keyDecrypter, getKeyResponse.getKeyEncodedEncrypted());

			EncryptionAlgorithm activeEncryptionAlgorithm = DATA_ENCRYPTION_ALGORITHM;
			SecretKeySpec key = new SecretKeySpec(keyEncodedPlain, getKeyResponse.getKeyAlgorithm());
			Cipher encrypter = Cipher.getInstance(activeEncryptionAlgorithm.getTransformation());
			encrypter.init(Cipher.ENCRYPT_MODE, key, NULL_IV);
			byte[] encrypted = encrypt(encrypter, plaintext.getData());
			encrypted[0] = (byte)activeEncryptionAlgorithm.ordinal();
			Ciphertext ciphertext = new Ciphertext();
			ciphertext.setData(encrypted);
			ciphertext.setKeyID(getKeyResponse.getKeyID());
			return ciphertext;
		} catch (Exception e) {
			logger.warn("Encryption failed: " + e, e);
			throw new RuntimeException(e);
		}
	}

	@Override
	public Plaintext decrypt(Ciphertext ciphertext)
	{
		// TODO use a cache for keys (or more precisely Cipher instances)!
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

		try {
			byte[] keyEncodedPlain = KeyEncryptionUtil.decryptKey(keyDecrypter, getKeyResponse.getKeyEncodedEncrypted());

			SecretKeySpec key = new SecretKeySpec(keyEncodedPlain, getKeyResponse.getKeyAlgorithm());
			int encryptionAlgoID = ciphertext.getData()[0] & 0xff;
			EncryptionAlgorithm activeEncryptionAlgorithm = EncryptionAlgorithm.values()[encryptionAlgoID];
			Cipher decrypter = Cipher.getInstance(activeEncryptionAlgorithm.getTransformation());
			decrypter.init(Cipher.DECRYPT_MODE, key, NULL_IV);
			byte[] decrypted = decrypt(decrypter, ciphertext.getData());
			Plaintext plaintext = new Plaintext();
			plaintext.setData(decrypted);
			return plaintext;
		} catch (Exception e) {
			logger.warn("Decryption failed: " + e, e);
			throw new RuntimeException(e);
		}
	}


	private byte[] encrypt(Cipher encrypter, byte[] input) throws GeneralSecurityException
	{
		int saltLength = getSaltLength(encrypter);
		int resultSize = 1 /* encryptionAlgoID */ + encrypter.getOutputSize(saltLength + 1 /* checksum-algo-id */ + 4 /* checksum-length with CRC32 */ + input.length);

		ByteBuffer resultBuf = ByteBuffer.allocate(resultSize);
		resultBuf.position(1); // We'll write this 'encryptionAlgoID' outside of this method after encrypting.

		{ // First, we put salt into the soup ;-)
			// We start with the salt to make sure we have unpredictable plain-text already at the very beginning to
			// make attacks based on known plain-text impossible.
			byte[] salt = new byte[saltLength];
			random.nextBytes(salt);
			encrypter.update(ByteBuffer.wrap(salt), resultBuf);
		}

		// Now we add the check-sum.
		// It always starts with the algo-identifier, followed by the checksum itself.
		byte[] checksum;
		switch (encryptWithChecksum) {
			case none: {
				checksum = new byte[1];
				checksum[0] = (byte)encryptWithChecksum.ordinal();
				break;
			}
			case crc32: {
				checksum = new byte[5]; // CRC32 has 32 bit, i.e. 4 bytes PLUS 1 byte for the checksumAlgoID
				checksum[0] = (byte)encryptWithChecksum.ordinal();
				CRC32 crc32 = new CRC32();
				crc32.update(input);
				long crc32Value = crc32.getValue();
				for (int i = 1; i < checksum.length; ++i)
					checksum[i] = (byte)(crc32Value >>> ((i - 1) * 8));

				break;
			}
			default:
				throw new IllegalStateException("Unsupported ChecksumAlgorithm: " + encryptWithChecksum);
		}
		encrypter.update(ByteBuffer.wrap(checksum), resultBuf);

		encrypter.doFinal(ByteBuffer.wrap(input), resultBuf);
		if (resultBuf.hasArray()) {
			if (resultBuf.array().length == resultBuf.position())
				return resultBuf.array();
			else
				logger.warn("Backing array cannot be directly used, because its size ({}) does not match the required size ({})!", resultBuf.array().length, resultBuf.position());
		}
		else
			logger.warn("Backing array cannot be directly used, because there is no backing array!");

		byte[] result = new byte[resultBuf.position()];
		resultBuf.rewind();
		resultBuf.get(result);
		return result;
	}

	private int getSaltLength(Cipher cipher)
	{
		int saltLength = cipher.getBlockSize();
		if (saltLength < 1)
			saltLength = 16;

		return saltLength;
	}

	private byte[] decrypt(Cipher decrypter, byte[] input) throws GeneralSecurityException, IOException
	{
		// Performance tests in CryptoAlgoBenchmark show that ByteBuffer is as fast as System.arraycopy(...) after
		// the JVM had time for hotspot-compilation (only at the beginning there are slight performance differences).
		// Hence we use the nicer API. Marco :-)
		ByteBuffer inputBuf = ByteBuffer.wrap(input);

		// Skip 1st byte containing the encryptionAlgoID (handled outside of this method).
		inputBuf.position(1);

		int rawPlainSize = decrypter.getOutputSize(input.length - inputBuf.position()); // '- inputBuf.position()' because of first skipped byte
		ByteBuffer rawBuf = ByteBuffer.allocate(rawPlainSize);

		int bytesDecryptedCount = decrypter.doFinal(inputBuf, rawBuf);

		// Discard the salt, the first byte we're interested in is the checksum-id AFTER the salt.
		int saltLength = getSaltLength(decrypter);
		rawBuf.position(saltLength);

		int checksumAlgoID = rawBuf.get() & 0xff; // the '& 0xff' is necessary to use the whole UNSIGNED range of a byte, i.e. 0...255.
		if (checksumAlgoID > ChecksumAlgorithm.values().length - 1)
			throw new IllegalArgumentException("input[" + (rawBuf.position() - 1) + "] == checksumAlgoID == " + checksumAlgoID + " is unknown!");

		ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.values()[checksumAlgoID];
		byte[] checksum;
		switch (checksumAlgorithm) {
			case none: {
				checksum = new byte[0];
				break;
			}
			case crc32: {
				checksum = new byte[4]; // CRC32 has 32 bit, i.e. 4 bytes
//				System.arraycopy(raw, saltLength + 1, checksum, 0, checksum.length);
				rawBuf.get(checksum);
				break;
			}
			default:
				throw new IllegalStateException("Unsupported ChecksumAlgorithm: " + encryptWithChecksum);
		}

//		byte[] result = new byte[raw.length - saltLength - 1 /* checksumAlgoID */ - checksum.length];
//		System.arraycopy(raw, saltLength + 1 + checksum.length, result, 0, result.length);
		byte[] result = new byte[bytesDecryptedCount - rawBuf.position()];
		rawBuf.get(result);

		// And finally calculate a new checksum and verify if it matches the one we read above.
		switch (checksumAlgorithm) {
			case none: // nothing to do
				break;
			case crc32: {
				CRC32 crc32 = new CRC32();
				crc32.update(result);
				long crc32Value = crc32.getValue();
				for (int i = 0; i < checksum.length; ++i) {
					if (checksum[i] != (byte)(crc32Value >>> (i * 8)))
						throw new IOException("CRC32 checksum mismatch!");
				}

				break;
			}
			default:
				throw new IllegalStateException("Unsupported ChecksumAlgorithm: " + encryptWithChecksum);
		}

		return result;
	}

	@Override
	public void close() {
		super.close();

		// TODO clear caches.
	}

}
