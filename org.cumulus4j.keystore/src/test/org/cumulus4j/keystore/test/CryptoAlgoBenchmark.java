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

package org.cumulus4j.keystore.test;

import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.CRC32;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.Assert;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
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
	private static final boolean USE_BOUNCY_CASTLE = true;
	private static final BouncyCastleProvider bouncyCastleProvider = USE_BOUNCY_CASTLE ? new BouncyCastleProvider() : null;
	static {
		if (bouncyCastleProvider != null)
			Security.insertProviderAt(bouncyCastleProvider, 2);
	}

//	private static final int ITERATION_COUNT = 1000000;
//	private static final int ITERATION_COUNT = 500000;
//	private static final int ITERATION_COUNT = 40000;
	private static final int ITERATION_COUNT = 3000;

	private static final Logger logger = LoggerFactory.getLogger(CryptoAlgoBenchmark.class);

	private Stopwatch stopwatch = new Stopwatch();

	private static SecureRandom random = new SecureRandom();

	@Test
	public void benchmarkAES128_cbc_noSalt()
	throws Exception
	{
		benchmark("AES", "AES", 128, "AES/CBC/PKCS5Padding", false, 12345);
	}

	@Test
	public void benchmarkAES128_cbc_withSalt()
	throws Exception
	{
		benchmark("AES", "AES", 128, "AES/CBC/PKCS5Padding", true, 12345);
	}

	@Test
	public void benchmarkAES128_cfb_noSalt()
	throws Exception
	{
		benchmark("AES", "AES", 128, "AES/CFB/NoPadding", false, 12345);
	}

	@Test
	public void benchmarkAES128_cfb_withSalt()
	throws Exception
	{
		benchmark("AES", "AES", 128, "AES/CFB/NoPadding", true, 12345);
	}

	@Test
	public void benchmarkAES256_noSalt()
	throws Exception
	{
		benchmark("AES", "AES", 256, "AES/CBC/PKCS5Padding", false, 12345);
	}

	@Test
	public void benchmarkAES256_withSalt()
	throws Exception
	{
		benchmark("AES", "AES", 256, "AES/CBC/PKCS5Padding", true, 12345);
	}

	@Test
	public void benchmarkBlowfish128_noSalt()
	throws Exception
	{
		benchmark("Blowfish", "Blowfish", 128, "Blowfish/CBC/PKCS5Padding", false, 12345);
	}

	@Test
	public void benchmarkBlowfish128_withSalt()
	throws Exception
	{
		benchmark("Blowfish", "Blowfish", 128, "Blowfish/CBC/PKCS5Padding", true, 12345);
	}

	@Test
	public void benchmarkBlowfish256_noSalt()
	throws Exception
	{
		benchmark("Blowfish", "Blowfish", 256, "Blowfish/CBC/PKCS5Padding", false, 12345);
	}

	@Test
	public void benchmarkBlowfish256_withSalt()
	throws Exception
	{
		benchmark("Blowfish", "Blowfish", 256, "Blowfish/CBC/PKCS5Padding", true, 12345);
	}

	@Test
	public void benchmarkBlowfish448_noSalt()
	throws Exception
	{
		benchmark("Blowfish", "Blowfish", 448, "Blowfish/CBC/PKCS5Padding", false, 12345);
	}

	@Test
	public void benchmarkBlowfish448_withSalt()
	throws Exception
	{
		benchmark("Blowfish", "Blowfish", 448, "Blowfish/CBC/PKCS5Padding", true, 12345);
	}

	@Test
	public void benchmarkEncryptRandomLengthData()
	throws Exception
	{
		String[] algos = {
				// CBC and PCBC do require padding!
				"AES/CBC/PKCS5Padding",
				"AES/CBC/ISO10126Padding",

				"AES/PCBC/PKCS5Padding",
				"AES/PCBC/ISO10126Padding",

				// CTR, CTS, CFB and OFB do not require padding.
				// CTR and CTS even *MUST* be used with NoPadding
				"AES/CTR/NoPadding",

				"AES/CTS/NoPadding",

				"AES/CFB/PKCS5Padding",
				"AES/CFB/ISO10126Padding",
				"AES/CFB/NoPadding",

				"AES/OFB/PKCS5Padding",
				"AES/OFB/ISO10126Padding",
				"AES/OFB/NoPadding",

				"AES/CFB8/NoPadding",
				"AES/CFB16/NoPadding",
				"AES/CFB128/NoPadding",

				"AES/OFB8/NoPadding",
				"AES/OFB128/NoPadding",
				"Blowfish/CBC/PKCS5Padding",
				"Blowfish/CFB/PKCS5Padding",
				"Blowfish/CFB/NoPadding",

				"Twofish/CBC/PKCS5Padding",
				"Twofish/CFB/PKCS5Padding",
				"Twofish/CFB/NoPadding"
		};

		Map<String, Throwable> algo2error = new TreeMap<String, Throwable>();
		for (int i = 0; i < 10; ++i) {
			int dataLength = 64 + random.nextInt(10240);
			for (String algo : algos) {
				int idx = algo.indexOf('/');
				String baseAlgo = algo.substring(0, idx);
				try {
					benchmark(baseAlgo, baseAlgo, 128, algo, true, dataLength);
				} catch (Exception x) {
					x = new RuntimeException("benchmark with algo=" + algo + " failed: " + x, x);
					logger.warn("benchmarkEncryptRandomLengthData: " + x.getMessage(), x);
					algo2error.put(algo, x);
				}
			}
		}

		if (!algo2error.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			sb.append("The following algorithms caused exceptions:\n");
			for (Map.Entry<String, Throwable> me : algo2error.entrySet()) {
				sb.append("  * ").append(me.getKey()).append(": ").append(me.getValue()).append('\n');
			}
			throw new RuntimeException(sb.toString());
		}
	}

	private void benchmark(String ivGenAlgo, String keyGenAlgo, int keyLengthBit, String cryptoAlgo, boolean salty, int dataLength)
	throws Exception
	{
		logger.info(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");
		logger.info("ITERATION_COUNT={}", ITERATION_COUNT);

		int ivLengthBit;
		{
			Cipher c = Cipher.getInstance(cryptoAlgo);
			c.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[keyLengthBit / 8], keyGenAlgo));
			ivLengthBit = c.getIV().length * 8;
		}

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
		logger.info(
				"dataLength={}",
				new Object[] { dataLength }
		);

		// initialization vector length: 128 bits
//		KeyGenerator keyGeneratorIV = KeyGenerator.getInstance(ivGenAlgo);
//		keyGeneratorIV.init(ivLengthBit);

		KeyGenerator keyGeneratorKey = KeyGenerator.getInstance(keyGenAlgo);
		keyGeneratorKey.init(keyLengthBit);

//		stopwatch.start("00.keyGeneratorIVManyTimes");
//		for (int i = 0; i < ITERATION_COUNT; i++) {
//			keyGeneratorIV.generateKey();
//		}
//		stopwatch.stop("00.keyGeneratorIVManyTimes");

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

//		byte[] encodedIV = keyGeneratorIV.generateKey().getEncoded();
		byte[] encodedIV = new byte[ivLengthBit / 8];
		random.nextBytes(encodedIV);
		IvParameterSpec iv = new IvParameterSpec(encodedIV);
		logger.info("iv (" + encodedIV.length * 8 + " bit): " +Util.encodeHexStr(encodedIV));

		byte[] encodedKey = keyGeneratorKey.generateKey().getEncoded();
		logger.info("key (" + encodedKey.length * 8 + " bit): " +Util.encodeHexStr(encodedKey));
		SecretKeySpec key = new SecretKeySpec(encodedKey, keyGenAlgo);

		Cipher encrypter;
		Cipher decrypter;
		encrypter = Cipher.getInstance(cryptoAlgo);
		decrypter = Cipher.getInstance(cryptoAlgo);
		logger.info("encrypter.provider={}", encrypter.getProvider());
		logger.info("decrypter.provider={}", decrypter.getProvider());

		stopwatch.start("10.init2CiphersWith1NewIVManyTimes");
		for (int i = 0; i < ITERATION_COUNT; i++) {
			random.nextBytes(altIV);
			IvParameterSpec tmpIV = new IvParameterSpec(altIV);
			encrypter.init(Cipher.ENCRYPT_MODE, key, tmpIV);
			decrypter.init(Cipher.DECRYPT_MODE, key, tmpIV);
		}
		stopwatch.stop("10.init2CiphersWith1NewIVManyTimes");

		stopwatch.start("11.initDecrypterWithSameKeyAndNewIVManyTimes");
		for (int i = 0; i < ITERATION_COUNT; i++) {
			random.nextBytes(altIV);
			IvParameterSpec tmpIV = new IvParameterSpec(altIV);
			decrypter.init(Cipher.DECRYPT_MODE, key, tmpIV);
		}
		stopwatch.stop("11.initDecrypterWithSameKeyAndNewIVManyTimes");

//		if (bouncyCastleProvider != null) { // RepeatedKey is only supported by BC, not trying it with Sun's provider.
//			stopwatch.start("12.initDecrypterWithRepeatedKeyAndNewIVManyTimes");
//			for (int i = 0; i < ITERATION_COUNT; i++) {
//				random.nextBytes(altIV);
//				IvParameterSpec tmpIV = new IvParameterSpec(altIV);
//				decrypter.init(Cipher.DECRYPT_MODE, new RepeatedKey(key.getAlgorithm()), tmpIV);
//			}
//			stopwatch.stop("12.initDecrypterWithRepeatedKeyAndNewIVManyTimes");
//		}

		encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
		decrypter.init(Cipher.DECRYPT_MODE, key, iv);

		byte[] input1 = new byte[dataLength];
		random.nextBytes(input1);
//		logger.info("input (" + input.length + " Byte): " +Util.encodeHexStr(input));

		byte[] encrypted1 = encrypt(encrypter, salty, input1); // encrypter.doFinal(input);
//		logger.info("encrypted (" + encrypted.length + " Byte): " +Util.encodeHexStr(encrypted));

		byte[] encrypted2 = encrypt(encrypter, salty, input1); // encrypter.doFinal(input);
//		logger.info("encrypted2 (" + encrypted2.length + " Byte): " +Util.encodeHexStr(encrypted2));
		logger.info("encrypted == encrypted2: " + Arrays.equals(encrypted1, encrypted2));

		byte[] decrypted1 = decrypt(decrypter, salty, encrypted1);
		byte[] decrypted2 = decrypt(decrypter, salty, encrypted2);
//		logger.info("decrypted (" + decrypted.length + " Byte): " +Util.encodeHexStr(decrypted));

		Assert.assertTrue("input1 != decrypted", Arrays.equals(input1, decrypted1));
		Assert.assertTrue("input1 != decrypted2", Arrays.equals(input1, decrypted2));

		stopwatch.start("20.encryptManyTimes");
		for (int i = 0; i < ITERATION_COUNT; i++) {
			encrypt(encrypter, salty, input1);
		}
		stopwatch.stop("20.encryptManyTimes");

		stopwatch.start("30.decryptManyTimes");
		for (int i = 0; i < ITERATION_COUNT; i++) {
			decrypter.doFinal(encrypted1);
		}
		stopwatch.stop("30.decryptManyTimes");


		// Test the RepeatedKey
		if (bouncyCastleProvider != null) { // RepeatedKey is only supported by BC, not trying it with Sun's provider.
			byte[] input2 = new byte[dataLength];
			random.nextBytes(input2);

			byte[] encodedIV2 = new byte[ivLengthBit / 8];
			random.nextBytes(encodedIV2);
			IvParameterSpec iv2 = new IvParameterSpec(encodedIV2);

			encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
			encrypted1 = encrypter.doFinal(input1);

//			encrypter.init(Cipher.ENCRYPT_MODE, new RepeatedKey(key.getAlgorithm()), iv2); // RepeatedKey disappeared?!
//			encrypted2 = encrypter.doFinal(input2);

			decrypter.init(Cipher.DECRYPT_MODE, key, iv);
			decrypted1 = decrypter.doFinal(encrypted1);

//			decrypter.init(Cipher.DECRYPT_MODE, new RepeatedKey(key.getAlgorithm()), iv2); // RepeatedKey disappeared?!
//			decrypted2 = decrypter.doFinal(encrypted2);

			byte[] decrypted1WithWrongIV;
			try {
				decrypted1WithWrongIV = decrypter.doFinal(encrypted1);
			} catch (GeneralSecurityException x) {
				logger.info("Decrypting with wrong IV threw this exception: " + x);
				// That's likely, because the IV is wrong ;-)
				decrypted1WithWrongIV = new byte[0];
			}

			// After the previous decryption failed, we should still be able to use the decrypter.
			byte[] decrypted2_again = decrypter.doFinal(encrypted2);

//			Assert.assertTrue("input1 != decrypted", Arrays.equals(input1, decrypted1)); // RepeatedKey disappeared?!
//			Assert.assertTrue("input2 != decrypted2", Arrays.equals(input2, decrypted2));
			Assert.assertTrue("input2 != decrypted2_again", Arrays.equals(input2, decrypted2_again));
			Assert.assertFalse("input1 == decrypted2", Arrays.equals(input1, decrypted1WithWrongIV));
		}

		logger.info(stopwatch.createHumanReport(true));
		logger.info("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
	}

	@Test
	public void testByteBufferCopy()
	{
		Dummy dummy = new Dummy();

		ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
		byte[] block = new byte[1024];
		random.nextBytes(block); // to have some data that is likely not 0.
		while (buffer.remaining() >= block.length)
			buffer.put(block);

		for (int i = 0; i < 1000; ++i) {
			byte[] result = new byte[buffer.position()];
			buffer.rewind();
			buffer.get(result);
			dummy.doNothing(result);
		}
	}

	@Test
	public void testByteBufferGetArray()
	{
		Dummy dummy = new Dummy();

		ByteBuffer buffer = ByteBuffer.allocate(1024 * 1024);
		byte[] block = new byte[1024];
		random.nextBytes(block); // to have some data that is likely not 0.
		while (buffer.remaining() >= block.length)
			buffer.put(block);

		for (int i = 0; i < 1000; ++i) {
			byte[] result = buffer.array();
			dummy.doNothing(result);
		}
	}

	@Test
	public void testMessageDigestInstantiation() throws NoSuchAlgorithmException
	{
		final int iterationCount = 10000;
		Dummy dummy = new Dummy();

		stopwatch.start("MessageDigest.getInstance.SHA1");
		for (int i = 0; i < iterationCount; ++i) {
			dummy.doNothing(MessageDigest.getInstance("SHA1"));
		}
		stopwatch.stop("MessageDigest.getInstance.SHA1");

		stopwatch.start("MessageDigest.getInstance.MD5");
		for (int i = 0; i < iterationCount; ++i) {
			dummy.doNothing(MessageDigest.getInstance("MD5"));
		}
		stopwatch.stop("MessageDigest.getInstance.MD5");

		stopwatch.start("newCRC32");
		for (int i = 0; i < iterationCount; ++i) {
			dummy.doNothing(new CRC32());
		}
		stopwatch.stop("newCRC32");

		logger.info(stopwatch.createHumanReport(true));
	}

	@Test
	public void testChecksumCalculation() throws NoSuchAlgorithmException
	{
		byte[] data = new byte[1024 * 1024];
		stopwatch.start("00.getRandomBytes");
		random.nextBytes(data); // to have some data that is likely not 0.
		stopwatch.stop("00.getRandomBytes");

		final int iterationCount = 1000;

		MessageDigest md = MessageDigest.getInstance("SHA1");
		stopwatch.start("calculate.SHA1");
		for (int i = 0; i < iterationCount; ++i) {
			md.update(data);
			md.digest();
		}
		stopwatch.stop("calculate.SHA1");

		md = MessageDigest.getInstance("MD5");
		stopwatch.start("calculate.MD5");
		for (int i = 0; i < iterationCount; ++i) {
			md.update(data);
			md.digest();
		}
		stopwatch.stop("calculate.MD5");

		CRC32 crc32 = new CRC32();
		stopwatch.start("calculate.CRC32");
		for (int i = 0; i < iterationCount; ++i) {
			crc32.update(data);
			crc32.getValue();
			crc32.reset();
		}
		stopwatch.stop("calculate.CRC32");

		logger.info(stopwatch.createHumanReport(true));
	}

	@Test
	public void convertIntToByteAndBack()
	{
		int i = 232;
		byte b = (byte) i;
		int i2 = b; // would be wrong, i.e. negative value
		int i3 = b & 0xff;

		logger.info("i = {}", i);
		logger.info("b = {}", b);
		logger.info("i2 = {}", i2);
		logger.info("i3 = {}", i3);

		Assert.assertEquals(i, i3);
	}

	@Test
	public void compareByteBufferGetPutWithSystemArraycopy()
	{
		final int iterationCount = 100000;
		byte[] data1 = new byte[10 * 1024 * 1024];
		byte[] data2 = data1.clone();
		ByteBuffer data1Buf = ByteBuffer.wrap(data1);
		byte[] block = new byte[10240];
		random.nextBytes(block);

		stopwatch.start("ByteBuffer.put.1");
		for (int i = 0; i < iterationCount; ++i) {
			data1Buf.put(block);
			if (i % (data1.length / block.length) == 0)
				data1Buf.rewind();
		}
		stopwatch.stop("ByteBuffer.put.1");
		data1Buf.rewind();

		stopwatch.start("ByteBuffer.get.1");
		for (int i = 0; i < iterationCount; ++i) {
			data1Buf.get(block);
			if (i % (data1.length / block.length) == 0)
				data1Buf.rewind();
		}
		stopwatch.stop("ByteBuffer.get.1");
		data1Buf.rewind();

		stopwatch.start("System.arraycopy.put.1");
		int data2Pos = 0;
		for (int i = 0; i < iterationCount; ++i) {
			System.arraycopy(block, 0, data2, data2Pos, block.length);
			data2Pos += block.length;
			if (i % (data1.length / block.length) == 0)
				data2Pos = 0;
		}
		stopwatch.stop("System.arraycopy.put.1");

		Assert.assertTrue("data1 != data2", Arrays.equals(data1, data2));

		stopwatch.start("System.arraycopy.get.1");
		data2Pos = 0;
		for (int i = 0; i < iterationCount; ++i) {
			System.arraycopy(data2, data2Pos, block, 0, block.length);
			data2Pos += block.length;
			if (i % (data1.length / block.length) == 0)
				data2Pos = 0;
		}
		stopwatch.stop("System.arraycopy.get.1");

		stopwatch.start("ByteBuffer.put.2");
		for (int i = 0; i < iterationCount; ++i) {
			data1Buf.put(block);
			if (i % (data1.length / block.length) == 0)
				data1Buf.rewind();
		}
		stopwatch.stop("ByteBuffer.put.2");
		data1Buf.rewind();

		stopwatch.start("ByteBuffer.get.2");
		for (int i = 0; i < iterationCount; ++i) {
			data1Buf.get(block);
			if (i % (data1.length / block.length) == 0)
				data1Buf.rewind();
		}
		stopwatch.stop("ByteBuffer.get.2");
		data1Buf.rewind();

		stopwatch.start("System.arraycopy.put.2");
		data2Pos = 0;
		for (int i = 0; i < iterationCount; ++i) {
			System.arraycopy(block, 0, data2, data2Pos, block.length);
			data2Pos += block.length;
			if (i % (data1.length / block.length) == 0)
				data2Pos = 0;
		}
		stopwatch.stop("System.arraycopy.put.2");

		stopwatch.start("System.arraycopy.get.2");
		data2Pos = 0;
		for (int i = 0; i < iterationCount; ++i) {
			System.arraycopy(data2, data2Pos, block, 0, block.length);
			data2Pos += block.length;
			if (i % (data1.length / block.length) == 0)
				data2Pos = 0;
		}
		stopwatch.stop("System.arraycopy.get.2");

		logger.info(stopwatch.createHumanReport(true));
		Assert.assertTrue("data1 != data2", Arrays.equals(data1, data2));
	}

	private byte[] encrypt(Cipher encrypter, boolean salty, byte[] input) throws GeneralSecurityException
	{
		int saltLength = salty ? getSaltLength(encrypter) : 0;
		int resultSize = encrypter.getOutputSize(input.length + saltLength);

		ByteBuffer resultBuf = ByteBuffer.allocate(resultSize);

		ByteBuffer inputBuf = ByteBuffer.wrap(input);

		if (salty) {
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
}
