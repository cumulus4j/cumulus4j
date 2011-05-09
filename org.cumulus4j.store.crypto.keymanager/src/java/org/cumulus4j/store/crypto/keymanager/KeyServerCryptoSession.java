package org.cumulus4j.store.crypto.keymanager;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.cumulus4j.store.crypto.AbstractCryptoSession;
import org.cumulus4j.store.crypto.Ciphertext;
import org.cumulus4j.store.crypto.Plaintext;
import org.cumulus4j.keymanager.back.shared.GetActiveEncryptionKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyRequest;
import org.cumulus4j.keymanager.back.shared.GetKeyResponse;
import org.cumulus4j.store.crypto.keymanager.rest.RequestResponseBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyServerCryptoSession
extends AbstractCryptoSession
{
	private static final Logger logger = LoggerFactory.getLogger(KeyServerCryptoSession.class);

	private static final String ALGORITHM_WITH_PARAMS = "AES/CBC/PKCS5Padding"; // TODO this should be configurable!
	private static final IvParameterSpec NULL_IV = new IvParameterSpec(new byte[16]); // TODO this should be determined based on the configured algorithm!

	private SecureRandom random = new SecureRandom();

	@Override
	public Ciphertext encrypt(Plaintext plaintext)
	{
		// TODO use a cache for this!!!
		GetKeyResponse getKeyResponse;
		try {
			getKeyResponse = RequestResponseBroker.sharedInstance().query(
					GetKeyResponse.class, new GetActiveEncryptionKeyRequest(getCryptoSessionID())
			);
		} catch (Exception e) {
			logger.warn("Could not query active encryption key: " + e, e);
			throw new RuntimeException(e);
		}

		// TODO cache ciphers/keys in general!
		try {
			SecretKeySpec key = new SecretKeySpec(getKeyResponse.getKeyEncoded(), getKeyResponse.getKeyAlgorithm());
			Cipher encrypter = Cipher.getInstance(ALGORITHM_WITH_PARAMS);
			encrypter.init(Cipher.ENCRYPT_MODE, key, NULL_IV);
			byte[] encrypted = encrypt(encrypter, plaintext.getData());
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
			getKeyResponse = RequestResponseBroker.sharedInstance().query(
					GetKeyResponse.class, new GetKeyRequest(getCryptoSessionID(), ciphertext.getKeyID())
			);
		} catch (Exception e) {
			logger.warn("Could not query key " + ciphertext.getKeyID() + ": " + e, e);
			throw new RuntimeException(e);
		}

		try {
			SecretKeySpec key = new SecretKeySpec(getKeyResponse.getKeyEncoded(), getKeyResponse.getKeyAlgorithm());
			Cipher decrypter = Cipher.getInstance(ALGORITHM_WITH_PARAMS);
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
		int inputWithSaltSize = input.length + saltLength; // encrypted salt length should be the same as plain salt length
		int blockSize = encrypter.getBlockSize();
		int blockQty = inputWithSaltSize / blockSize;
		int resultSize = blockQty * blockSize;
		while (resultSize < inputWithSaltSize)
			resultSize += blockSize;

		ByteBuffer resultBuf = ByteBuffer.allocate(resultSize);

		ByteBuffer inputBuf = ByteBuffer.wrap(input);

		{ // putting salt into the soup
			byte[] salt = new byte[saltLength];
			random.nextBytes(salt);
			encrypter.update(ByteBuffer.wrap(salt), resultBuf);
		}

		encrypter.doFinal(inputBuf, resultBuf);
		if (resultBuf.hasArray()) {
			if (resultBuf.array().length == resultBuf.position())
				return resultBuf.array();
			else
				logger.warn("Backing array cannot be directly used, because its size ({}) does not match the required size ({})!", resultBuf.array().length, resultBuf.position());
		}
		else
			logger.warn("Backing array cannot be directly used, because there is no backing array!");

		byte[] result = new byte[resultBuf.position()];
		resultBuf.position(0);
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

	private byte[] decrypt(Cipher decrypter, byte[] input) throws GeneralSecurityException
	{
		byte[] raw = decrypter.doFinal(input);

		int saltLength = getSaltLength(decrypter);
		// discarding the salt.
		byte[] result = new byte[raw.length - saltLength];
		System.arraycopy(raw, saltLength, result, 0, result.length);
		return result;
	}

	@Override
	public void close() {
		super.close();

		// TODO clear caches.
	}

}
