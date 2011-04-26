package org.cumulus4j.keystore.test;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.Assert;

import org.junit.Test;
import org.nightlabs.util.Stopwatch;
import org.nightlabs.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoAlgoBenchmark
{
//	private static final int ITERATION_COUNT = 1000000;
//	private static final int ITERATION_COUNT = 500000;
	private static final int ITERATION_COUNT = 3000;

	private static final Logger logger = LoggerFactory.getLogger(CryptoAlgoBenchmark.class);

//	private static final char[] KEY_STORE_PASSWORD = { 'a', 'b', 'c', 's', 'e', 'c' };
	private Stopwatch stopwatch = new Stopwatch();

	private static SecureRandom random = new SecureRandom();

	@Test
	public void benchmarkAES128_cbc_noSalt()
	throws Exception
	{
		benchmark("AES", 128, "AES", 128, "AES/CBC/PKCS5Padding", false);
	}

	@Test
	public void benchmarkAES128_cbc_withSalt()
	throws Exception
	{
		benchmark("AES", 128, "AES", 128, "AES/CBC/PKCS5Padding", true);
	}

	@Test
	public void benchmarkAES128_cfb_noSalt()
	throws Exception
	{
		benchmark("AES", 128, "AES", 128, "AES/CFB/PKCS5Padding", false);
	}

	@Test
	public void benchmarkAES128_cfb_withSalt()
	throws Exception
	{
		benchmark("AES", 128, "AES", 128, "AES/CFB/PKCS5Padding", true);
	}

	@Test
	public void benchmarkAES256_noSalt()
	throws Exception
	{
		benchmark("AES", 128, "AES", 256, "AES/CBC/PKCS5Padding", false);
	}

	@Test
	public void benchmarkAES256_withSalt()
	throws Exception
	{
		benchmark("AES", 128, "AES", 256, "AES/CBC/PKCS5Padding", true);
	}

	@Test
	public void benchmarkBlowfish128_noSalt()
	throws Exception
	{
		benchmark("Blowfish", 64, "Blowfish", 128, "Blowfish/CBC/PKCS5Padding", false);
	}

	@Test
	public void benchmarkBlowfish128_withSalt()
	throws Exception
	{
		benchmark("Blowfish", 64, "Blowfish", 128, "Blowfish/CBC/PKCS5Padding", true);
	}

	@Test
	public void benchmarkBlowfish256_noSalt()
	throws Exception
	{
		benchmark("Blowfish", 64, "Blowfish", 256, "Blowfish/CBC/PKCS5Padding", false);
	}

	@Test
	public void benchmarkBlowfish256_withSalt()
	throws Exception
	{
		benchmark("Blowfish", 64, "Blowfish", 256, "Blowfish/CBC/PKCS5Padding", true);
	}

	@Test
	public void benchmarkBlowfish448_noSalt()
	throws Exception
	{
		benchmark("Blowfish", 64, "Blowfish", 448, "Blowfish/CBC/PKCS5Padding", false);
	}

	@Test
	public void benchmarkBlowfish448_withSalt()
	throws Exception
	{
		benchmark("Blowfish", 64, "Blowfish", 448, "Blowfish/CBC/PKCS5Padding", true);
	}



//	@Test
//	public void test0()
//	throws Exception
//	{
//
//		File keyStoreFile = File.createTempFile("test-", ".keystore");
//
//		KeyStore ks = load(keyStoreFile);
//	}

	private void benchmark(String ivGenAlgo, int ivLengthBit, String keyGenAlgo, int keyLengthBit, String cryptoAlgo, boolean salty)
	throws Exception
	{
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		logger.info("ITERATION_COUNT={}", ITERATION_COUNT);
		logger.info(
				"ivGenAlgo={} ivLengthBit={}",
				new Object[] { ivGenAlgo, ivLengthBit }
		);
		logger.info(
				"keyGenAlgo={} keyLengthBit={}",
				new Object[] { keyGenAlgo, keyLengthBit }
		);
		logger.info(
				"cryptoAlgo={} salty={}",
				new Object[] { cryptoAlgo, salty }
		);

		// initialization vector length: 128 bits
		KeyGenerator keyGeneratorIV = KeyGenerator.getInstance(ivGenAlgo);
		keyGeneratorIV.init(ivLengthBit);

		KeyGenerator keyGeneratorKey = KeyGenerator.getInstance(keyGenAlgo);
		keyGeneratorKey.init(keyLengthBit);

		stopwatch.start("00.keyGeneratorIVManyTimes");
		for (int i = 0; i < ITERATION_COUNT; i++) {
			keyGeneratorIV.generateKey();
		}
		stopwatch.stop("00.keyGeneratorIVManyTimes");

		stopwatch.start("01.secureRandomIVManyTimes");
		byte[] altIV = new byte[ivLengthBit / 8];
		for (int i = 0; i < ITERATION_COUNT; i++) {
			random.nextBytes(altIV);
		}
		stopwatch.stop("01.secureRandomIVManyTimes");

		stopwatch.start("05.generateKeyManyTimes");
		for (int i = 0; i < ITERATION_COUNT; i++) {
			keyGeneratorKey.generateKey();
		}
		stopwatch.stop("05.generateKeyManyTimes");

		byte[] encodedIV = keyGeneratorIV.generateKey().getEncoded();
		IvParameterSpec iv = new IvParameterSpec(encodedIV);
		logger.info("iv (" + encodedIV.length * 8 + " bit): " +Util.encodeHexStr(encodedIV));

		byte[] encodedKey = keyGeneratorKey.generateKey().getEncoded();
		logger.info("key (" + encodedKey.length * 8 + " bit): " +Util.encodeHexStr(encodedKey));
		SecretKeySpec key = new SecretKeySpec(encodedKey, keyGenAlgo);

		Cipher encrypter;
		Cipher decrypter;
		encrypter = Cipher.getInstance(cryptoAlgo);
		decrypter = Cipher.getInstance(cryptoAlgo);

		stopwatch.start("10.init2CiphersWith1NewIVManyTimes");
		for (int i = 0; i < ITERATION_COUNT; i++) {
//			IvParameterSpec tmpIV = new IvParameterSpec(keyGeneratorIV.generateKey().getEncoded());
			random.nextBytes(altIV);
			IvParameterSpec tmpIV = new IvParameterSpec(altIV);
			encrypter.init(Cipher.ENCRYPT_MODE, key, tmpIV);
			decrypter.init(Cipher.DECRYPT_MODE, key, tmpIV);
		}
		stopwatch.stop("10.init2CiphersWith1NewIVManyTimes");

		encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
		decrypter.init(Cipher.DECRYPT_MODE, key, iv);

		byte[] input = new byte[12345];
		random.nextBytes(input);
//		logger.info("input (" + input.length + " Byte): " +Util.encodeHexStr(input));

		byte[] encrypted = encrypt(encrypter, salty, input); // encrypter.doFinal(input);
//		logger.info("encrypted (" + encrypted.length + " Byte): " +Util.encodeHexStr(encrypted));

		byte[] encrypted2 = encrypt(encrypter, salty, input); // encrypter.doFinal(input);
//		logger.info("encrypted2 (" + encrypted2.length + " Byte): " +Util.encodeHexStr(encrypted2));
		logger.info("encrypted == encrypted2: " + Arrays.equals(encrypted, encrypted2));

		byte[] decrypted = decrypt(decrypter, salty, encrypted);
		byte[] decrypted2 = decrypt(decrypter, salty, encrypted2);
//		logger.info("decrypted (" + decrypted.length + " Byte): " +Util.encodeHexStr(decrypted));

		Assert.assertTrue("input != decrypted", Arrays.equals(input, decrypted));
		Assert.assertTrue("input != decrypted2", Arrays.equals(input, decrypted2));

		stopwatch.start("20.encryptManyTimes");
		for (int i = 0; i < ITERATION_COUNT; i++) {
			encrypt(encrypter, salty, input);
		}
		stopwatch.stop("20.encryptManyTimes");

		stopwatch.start("30.decryptManyTimes");
		for (int i = 0; i < ITERATION_COUNT; i++) {
			decrypter.doFinal(encrypted);
		}
		stopwatch.stop("30.decryptManyTimes");

		logger.info(stopwatch.createHumanReport(true));
		logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	private byte[] encrypt(Cipher encrypter, boolean salty, byte[] input) throws GeneralSecurityException
	{
		int saltLength = salty ? getSaltLength(encrypter) : 0;
		int inputWithSaltSize = input.length + saltLength; // encrypted salt length should be the same as plain salt length
		int blockSize = encrypter.getBlockSize();
		int blockQty = inputWithSaltSize / blockSize;
		int resultSize = blockQty * blockSize;
		while (resultSize < inputWithSaltSize)
			resultSize += blockSize;

		ByteBuffer resultBuf = ByteBuffer.allocate(resultSize);

		ByteBuffer inputBuf = ByteBuffer.wrap(input);

		if (salty) {
			byte[] salt = new byte[saltLength];
			if (salt.length > 256)
				throw new IllegalStateException("salt is too long!!!");

			random.nextBytes(salt);

//			// simulating 1 byte at the beginning for indicating how long the actual salt will be (in bytes).
//			ByteBuffer saltWithLength = ByteBuffer.allocate(algoBlockSize + 1);
//			saltWithLength.put((byte)salt.length); // to be understood as UNsigned integer with 8 bit size - not sure if this is correct, but for our test it's fine. Marco.
//			saltWithLength.put(salt);
//			saltWithLength.position(0);
//			encrypter.update(saltWithLength, resultBuf);
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

//	private byte[] encrypt(Cipher encrypter, boolean salty, byte[] input) throws GeneralSecurityException
//	{
//		byte[] saltPrefix = null;
//		if (salty) {
//			byte[] salt = new byte[encrypter.getIV().length];
//			random.nextBytes(salt);
//
//			if (salt.length > 256)
//				throw new IllegalStateException("salt is too long!!!");
//
//			// simulating 1 byte at the beginning for indicating how long the actual salt will be (in bytes).
//			byte[] saltWithLength = new byte[1 + salt.length];
//			saltWithLength[0] = (byte) salt.length; // to be understood as UNsigned integer with 8 bit size - not sure if this is correct, but for our test it's fine. Marco.
//			System.arraycopy(salt, 0, saltWithLength, 1, salt.length);
//			saltPrefix = encrypter.update(saltWithLength);
//		}
//
//		byte[] raw = encrypter.doFinal(input);
//
//		if (!salty)
//			return raw;
//
//		byte[] result = new byte[saltPrefix.length + raw.length];
//		System.arraycopy(saltPrefix, 0, result, 0, saltPrefix.length);
//		System.arraycopy(raw, 0, result, saltPrefix.length, raw.length);
//		return result;
//	}

	private int getSaltLength(Cipher cipher)
	{
		int saltLength = cipher.getBlockSize();
		if (saltLength < 1)
			saltLength = 16;

		return saltLength;
	}

	private byte[] decrypt(Cipher decrypter, boolean salty, byte[] input) throws GeneralSecurityException
	{
		byte[] raw = decrypter.doFinal(input);
		if (!salty)
			return raw;

		int saltLength = getSaltLength(decrypter);

		byte[] result = new byte[raw.length - saltLength];
		System.arraycopy(raw, saltLength, result, 0, result.length);
		return result;
	}

//	private KeyStore load(File f) throws IOException, KeyStoreException, GeneralSecurityException
//	{
//		stopwatch.start("load");
//		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
//
//		FileInputStream fis = null;
//		try {
//			fis = f.length() > 0 ? new FileInputStream(f) : null;
//			ks.load(fis, KEY_STORE_PASSWORD);
//		} finally {
//			if (fis != null) {
//				fis.close();
//			}
//		}
//		stopwatch.stop("load");
//		return ks;
//	}
//
//	private void store(KeyStore ks, File f) throws IOException, KeyStoreException, GeneralSecurityException
//	{
//		stopwatch.start("store");
//		FileOutputStream out = new FileOutputStream(f);
//		ks.store(out, KEY_STORE_PASSWORD);
//		out.close();
//		stopwatch.stop("store");
//	}
}
