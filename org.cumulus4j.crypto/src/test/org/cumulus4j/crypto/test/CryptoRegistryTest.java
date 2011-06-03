package org.cumulus4j.crypto.test;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.Security;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import junit.framework.Assert;

import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.cumulus4j.crypto.CryptoRegistry;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CryptoRegistryTest
{
	private static final Logger logger = LoggerFactory.getLogger(CryptoRegistryTest.class);

	private static final BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();

//	private static final boolean USE_BOUNCY_CASTLE_JCE_PROVIDER = true;
//	static {
//		if (USE_BOUNCY_CASTLE_JCE_PROVIDER) {
//			Security.insertProviderAt(bouncyCastleProvider, 2);
//
//			KeyGenerator kg;
//			try {
//				kg = KeyGenerator.getInstance("AES");
//			} catch (NoSuchAlgorithmException e) {
//				logger.warn("KeyGenerator.getInstance(\"AES\") failed: " + e, e);
//				kg = null;
//			}
//
//			if (kg == null || kg.getProvider() != bouncyCastleProvider)
//				logger.warn("BouncyCastleProvider was NOT registered!!!");
//		}
//	}

	private static final String[] SYMMETRIC_TRANSFORMATIONS = {
		"AES/CBC/NoPadding",
		"AES/CBC/ISO10126Padding",
		"AES/CBC/ISO10126-2",
		"AES/CBC/ISO7816-4",
		"AES/CBC/TBC",
		"AES/CBC/X9.23",
		"AES/CBC/ZeroByte",
		"AES/CBC/PKCS5",
		"AES/CBC/PKCS5Padding",
		"AES/CBC/PKCS7Padding",

		"AES/CFB/NoPadding",
		"AES/CFB/ISO10126Padding",
		"AES/CFB/ISO10126-2",
		"AES/CFB/ISO7816-4",
		"AES/CFB/TBC",
		"AES/CFB/X9.23",
		"AES/CFB/ZeroByte",
		"AES/CFB/PKCS5",
		"AES/CFB/PKCS5Padding",
		"AES/CFB/PKCS7Padding",

		"AES/CFB8/NoPadding",
		"AES/CFB8/ISO10126Padding",
		"AES/CFB8/ISO10126-2",
		"AES/CFB8/ISO7816-4",
		"AES/CFB8/TBC",
		"AES/CFB8/X9.23",
		"AES/CFB8/ZeroByte",
		"AES/CFB8/PKCS5",
		"AES/CFB8/PKCS5Padding",
		"AES/CFB8/PKCS7Padding",

		"AES/CFB16/NoPadding",
		"AES/CFB16/ISO10126Padding",
		"AES/CFB16/ISO10126-2",
		"AES/CFB16/ISO7816-4",
		"AES/CFB16/TBC",
		"AES/CFB16/X9.23",
		"AES/CFB16/ZeroByte",
		"AES/CFB16/PKCS5",
		"AES/CFB16/PKCS5Padding",
		"AES/CFB16/PKCS7Padding",

		"AES/CFB64/NoPadding",
		"AES/CFB64/ISO10126Padding",
		"AES/CFB64/ISO10126-2",
		"AES/CFB64/ISO7816-4",
		"AES/CFB64/TBC",
		"AES/CFB64/X9.23",
		"AES/CFB64/ZeroByte",
		"AES/CFB64/PKCS5",
		"AES/CFB64/PKCS5Padding",
		"AES/CFB64/PKCS7Padding",

		"AES/OFB/NoPadding",
		"AES/OFB/ISO10126Padding",
		"AES/OFB/ISO10126-2",
		"AES/OFB/ISO7816-4",
		"AES/OFB/TBC",
		"AES/OFB/X9.23",
		"AES/OFB/ZeroByte",
		"AES/OFB/PKCS5",
		"AES/OFB/PKCS5Padding",
		"AES/OFB/PKCS7Padding"
	};

	private static final String[] ASYMMETRIC_TRANSFORMATIONS = {
		"RSA",
		"RSA//",
		"RSA//NoPadding",
		"RSA/ECB/NoPadding",
		"RSA/CBC/NoPadding",
		"RSA//ISO9796-1",
		"RSA//OAEP",
		"RSA//PKCS1",
		"RSA//PKCS1Padding"
	};

	@Test
	public void testLookupCompatibilityWithJCE()
	{
		List<String> transformations = new ArrayList<String>();
		transformations.addAll(Arrays.asList(SYMMETRIC_TRANSFORMATIONS));
		transformations.addAll(Arrays.asList(ASYMMETRIC_TRANSFORMATIONS));

		for (String transformation : transformations) {
			Throwable jceError = null;
			Throwable cryptoRegistryError = null;

			try {
				Cipher.getInstance(transformation);
			} catch (Throwable t) {
				jceError = t;
			}

			try {
				CryptoRegistry.sharedInstance().createCipher(transformation);
			} catch (Throwable t) {
				cryptoRegistryError = t;
			}

			if (jceError == null) {
				if (cryptoRegistryError != null) {
					String errorMessage = "JCE successfully provided a Cipher for transformation=\"" + transformation + "\", but our CryptoRegistry failed: " + cryptoRegistryError;
					logger.error(errorMessage, cryptoRegistryError);
					Assert.fail(errorMessage);
				}
			}
			else {
				if (cryptoRegistryError == null)
					logger.warn("JCE fails to provide a Cipher for transformation=\"" + transformation + "\", but our CryptoRegistry succeeded!");
				else if (jceError.getClass() != cryptoRegistryError.getClass())
					Assert.fail("JCE fails to provide a Cipher for transformation=\"" + transformation + "\" with a " + jceError.getClass().getName() + ", but our CryptoRegistry failed with another exception: " + cryptoRegistryError.getClass());
			}
		}
	}

	private SecureRandom random = new SecureRandom();

	private static String getEngineName(String transformation)
	{
		return CryptoRegistry.splitTransformation(transformation)[0];
	}

	private static String getPaddingName(String transformation)
	{
		return CryptoRegistry.splitTransformation(transformation)[2];
	}

	@Test
	public void testSymmetricEncryptionCompatibilityWithJCE_SunProvider()
	throws Exception
	{
		for (String transformation : SYMMETRIC_TRANSFORMATIONS)
		{
			try {
				String paddingName = getPaddingName(transformation);
				if ("".equals(paddingName) || "NOPADDING".equals(paddingName.toUpperCase(Locale.ENGLISH)))
					continue;

				Cipher jceCipher;
				try {
					jceCipher = Cipher.getInstance(transformation);
				} catch (Throwable t) {
					continue;
				}

				org.cumulus4j.crypto.Cipher c4jCipher = CryptoRegistry.sharedInstance().createCipher(transformation);
				byte[] original = new byte[1024 + random.nextInt(10240)];
				random.nextBytes(original);

				byte[] iv = new byte[c4jCipher.getIVSize()];
				random.nextBytes(iv);

				// we generate a random 128 bit key
				byte[] key = new byte[128 / 8];
				random.nextBytes(key);

				c4jCipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));
				jceCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, getEngineName(transformation)), new IvParameterSpec(iv));

				byte[] encrypted = c4jCipher.doFinal(original);
				byte[] decrypted = jceCipher.doFinal(encrypted);
				Assert.assertTrue(
						"Decrypted does not match original for transformation \"" + transformation + "\"!",
						Arrays.equals(original, decrypted)
				);
			} catch (Exception x) {
				throw new Exception("Processing transformation \"" + transformation + "\" failed: " + x, x);
			}
		}
	}

	@Test
	public void testSymmetricEncryptionCompatibilityWithJCE_BouncyCastleProvider()
	throws Exception
	{
		Security.insertProviderAt(bouncyCastleProvider, 2);
		try {
			KeyGenerator kg;
			try {
				kg = KeyGenerator.getInstance("AES");
			} catch (NoSuchAlgorithmException e) {
				logger.warn("KeyGenerator.getInstance(\"AES\") failed: " + e, e);
				kg = null;
			}

			if (kg == null || kg.getProvider() != bouncyCastleProvider)
				Assert.fail("Registering BouncyCastleProvider failed!");

			for (String transformation : SYMMETRIC_TRANSFORMATIONS)
			{
				try {
					String paddingName = getPaddingName(transformation);
					if ("".equals(paddingName) || "NOPADDING".equals(paddingName.toUpperCase(Locale.ENGLISH)))
						continue;

					Cipher jceCipher;
					try {
						jceCipher = Cipher.getInstance(transformation);
					} catch (Throwable t) {
						continue;
					}

					org.cumulus4j.crypto.Cipher c4jCipher = CryptoRegistry.sharedInstance().createCipher(transformation);
					byte[] original = new byte[1024 + random.nextInt(10240)];
					random.nextBytes(original);

					byte[] iv = new byte[c4jCipher.getIVSize()];
					random.nextBytes(iv);

					// we generate a random 128 bit key
					byte[] key = new byte[128 / 8];
					random.nextBytes(key);

					c4jCipher.init(true, new ParametersWithIV(new KeyParameter(key), iv));
					jceCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, getEngineName(transformation)), new IvParameterSpec(iv));

					byte[] encrypted = c4jCipher.doFinal(original);
					byte[] decrypted = jceCipher.doFinal(encrypted);
					Assert.assertTrue(
							"Decrypted does not match original for transformation \"" + transformation + "\"!",
							Arrays.equals(original, decrypted)
					);
				} catch (Exception x) {
					throw new Exception("Processing transformation \"" + transformation + "\" failed: " + x, x);
				}
			}
		} finally {
			Security.removeProvider(bouncyCastleProvider.getName());
		}
	}

	@Test
	public void testSymmetricDecryptionCompatibilityWithJCE()
	{

	}
}
