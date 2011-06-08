package org.cumulus4j.keymanager.back.shared;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.CipherOperationMode;
import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.crypto.util.ChecksumAlgorithm;
import org.cumulus4j.crypto.util.ChecksumCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class KeyEncryptionUtil
{
	private static final Logger logger = LoggerFactory.getLogger(KeyEncryptionUtil.class);

	private KeyEncryptionUtil() { }

	private static final ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.SHA1;
	private static final ChecksumCalculator checksumCalculator = new ChecksumCalculator();

	public static byte[] encryptKey(byte[] key, Cipher encrypter) throws CryptoException
	{
		byte[] checksum = checksumCalculator.checksum(key, checksumAlgorithm);

		int resultSize = (
				2 // checksum identifier + checksum length NOT encrypted
				+ encrypter.getOutputSize(checksum.length + key.length)
		);

		byte[] out = new byte[2 + resultSize];

		if (checksum.length > 255)
			throw new IllegalStateException("Checksum length too long!");

		int outOff = 0;
		out[outOff++] = checksumAlgorithm.toByte();
		out[outOff++] = (byte)checksum.length;

		outOff += encrypter.update(checksum, 0, checksum.length, out, outOff);
		outOff += encrypter.update(key,      0,      key.length, out, outOff);
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

	public static byte[] decryptKey(Cipher decrypter, byte[] keyEncodedEncrypted) throws CryptoException, IOException
	{
		int encryptedOff = 0;
		ChecksumAlgorithm checksumAlgorithm = ChecksumAlgorithm.valueOf(keyEncodedEncrypted[encryptedOff++]);
		int checksumLength = keyEncodedEncrypted[encryptedOff++] & 0xff;

		int outputSize = decrypter.getOutputSize(keyEncodedEncrypted.length - encryptedOff);
		byte[] out = new byte[outputSize];

		int outOff = 0;
		outOff += decrypter.update(keyEncodedEncrypted, encryptedOff, keyEncodedEncrypted.length - encryptedOff, out, outOff);
		outOff += decrypter.doFinal(out, outOff);

		outOff = 0;
		byte[] checksum = new byte[checksumLength];
		System.arraycopy(out, outOff, checksum, 0, checksum.length);
		outOff += checksum.length;

		byte[] result = new byte[out.length - outOff];
		System.arraycopy(out, outOff, result, 0, result.length);

		// And finally calculate a new checksum and verify if it matches the one we read above.
		byte[] newChecksum = checksumCalculator.checksum(result, checksumAlgorithm);
		if (newChecksum.length != checksumLength)
			throw new IOException("Checksums have different length! Expected checksum has " + checksumLength + " bytes and newly calculated checksum has " + newChecksum.length + " bytes!");

		for (int i = 0; i < checksum.length; ++i) {
			if (checksum[i] != newChecksum[i])
				throw new IOException("Checksum mismatch!");
		}

		return result;
	}

//	/**
//	 * Get the first part of the full algorithm-descriptor.
//	 * <p>
//	 * When working with a {@link Cipher},
//	 * an algorithm is usually described together with a mode (e.g. ECB, CBC, CFB) and a padding
//	 * like in the examples "AES/CBC/PKCS5Padding" or "RSA/ECB/OAEPWITHSHA1ANDMGF1PADDING". However,
//	 * it is often necessary to extract only the first part (e.g. "AES" or "RSA") from it.
//	 * </p>
//	 * <p>
//	 * This method checks the given <code>fullAlgorithm</code> whether it contains a slash ("/"). If not,
//	 * the argument is returned as is. Otherwise, the part before the first slash is returned.
//	 * </p>
//	 *
//	 * @param fullAlgorithm the algorithm together with mode and padding (where mode and padding are optional).
//	 * @return the algorithm without mode and padding.
//	 */
//	public static String getRawEncryptionAlgorithmWithoutModeAndPadding(String fullAlgorithm)
//	{
//		int slashIdx = fullAlgorithm.indexOf('/');
//		if (slashIdx < 0)
//			return fullAlgorithm;
//
//		return fullAlgorithm.substring(0, slashIdx);
//	}
}
