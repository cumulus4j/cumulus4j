package org.cumulus4j.keymanager.back.shared;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.util.zip.CRC32;

import javax.crypto.Cipher;

import org.bouncycastle.crypto.CryptoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class KeyEncryptionUtil
{
	private static final Logger logger = LoggerFactory.getLogger(KeyEncryptionUtil.class);

	private KeyEncryptionUtil() { }

	public static byte[] encryptKey(byte[] key, Cipher encrypter) throws GeneralSecurityException
	{
		int resultSize = encrypter.getOutputSize(4 /* checksum-length with CRC32 */ + key.length);

		ByteBuffer resultBuf = ByteBuffer.allocate(resultSize);

		// Now we add the check-sum.
		// It always starts with the algo-identifier, followed by the checksum itself.
		byte[] checksum = new byte[4]; // CRC32 has 32 bit, i.e. 4 bytes
		CRC32 crc32 = new CRC32();
		crc32.update(key);
		long crc32Value = crc32.getValue();
		for (int i = 0; i < checksum.length; ++i)
			checksum[i] = (byte)(crc32Value >>> (i * 8));

		encrypter.update(ByteBuffer.wrap(checksum), resultBuf);

		encrypter.doFinal(ByteBuffer.wrap(key), resultBuf);
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

	public static byte[] encryptKey(byte[] key, String keyEncryptionAlgorithm, byte[] keyEncryptionPublicKey) throws GeneralSecurityException
	{
		Cipher keyEncrypter = Cipher.getInstance(keyEncryptionAlgorithm);
//		Key publicKey = keyEncrypter.unwrap(
//				keyEncryptionPublicKey,
//				KeyEncryptionUtil.getRawEncryptionAlgorithmWithoutModeAndPadding(keyEncryptionAlgorithm),
//				Cipher.PUBLIC_KEY
//		);
		X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyEncryptionPublicKey);
		Key publicKey = KeyFactory.getInstance(getRawEncryptionAlgorithmWithoutModeAndPadding(keyEncryptionAlgorithm)).generatePublic(keySpec);
		keyEncrypter.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] keyEncodedEncrypted = KeyEncryptionUtil.encryptKey(key, keyEncrypter);
		return keyEncodedEncrypted;
	}

	public static byte[] decryptKey(org.cumulus4j.crypto.Cipher decrypter, byte[] keyEncodedEncrypted) throws CryptoException, IOException
	{
		byte[] rawDecrypted = decrypter.doFinal(keyEncodedEncrypted);

		int off = 0;
		byte[] checksum = new byte[4]; // CRC32 has 32 bit, i.e. 4 bytes
		System.arraycopy(rawDecrypted, off, checksum, 0, checksum.length);
		off += checksum.length;

		byte[] result = new byte[rawDecrypted.length - checksum.length];
		System.arraycopy(rawDecrypted, off, result, 0, result.length);

		// And finally calculate a new checksum and verify if it matches the one we read above.
		CRC32 crc32 = new CRC32();
		crc32.update(result);
		long crc32Value = crc32.getValue();
		for (int i = 0; i < checksum.length; ++i) {
			if (checksum[i] != (byte)(crc32Value >>> (i * 8)))
				throw new IOException("CRC32 checksum mismatch!");
		}

		return result;
	}

	/**
	 * Get the first part of the full algorithm-descriptor.
	 * <p>
	 * When working with a {@link Cipher},
	 * an algorithm is usually described together with a mode (e.g. ECB, CBC, CFB) and a padding
	 * like in the examples "AES/CBC/PKCS5Padding" or "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING". However,
	 * it is often necessary to extract only the first part (e.g. "AES" or "RSA") from it.
	 * </p>
	 * <p>
	 * This method checks the given <code>fullAlgorithm</code> whether it contains a slash ("/"). If not,
	 * the argument is returned as is. Otherwise, the part before the first slash is returned.
	 * </p>
	 *
	 * @param fullAlgorithm the algorithm together with mode and padding (where mode and padding are optional).
	 * @return the algorithm without mode and padding.
	 */
	public static String getRawEncryptionAlgorithmWithoutModeAndPadding(String fullAlgorithm)
	{
		int slashIdx = fullAlgorithm.indexOf('/');
		if (slashIdx < 0)
			return fullAlgorithm;

		return fullAlgorithm.substring(0, slashIdx);
	}
}
