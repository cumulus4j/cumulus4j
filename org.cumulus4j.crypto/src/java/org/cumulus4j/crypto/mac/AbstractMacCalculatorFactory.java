package org.cumulus4j.crypto.mac;

import java.security.SecureRandom;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD2Digest;
import org.bouncycastle.crypto.digests.MD4Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.RIPEMD128Digest;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.digests.SHA224Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.digests.SHA384Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.bouncycastle.crypto.digests.TigerDigest;
import org.bouncycastle.crypto.engines.DESEngine;
import org.bouncycastle.crypto.engines.RC2Engine;
import org.bouncycastle.crypto.macs.CBCBlockCipherMac;
import org.bouncycastle.crypto.macs.CFBBlockCipherMac;
import org.bouncycastle.crypto.macs.GOST28147Mac;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.macs.ISO9797Alg3Mac;
import org.bouncycastle.crypto.macs.OldHMac;
import org.bouncycastle.crypto.paddings.ISO7816d4Padding;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.cumulus4j.crypto.MacCalculator;
import org.cumulus4j.crypto.MacCalculatorFactory;

public abstract class AbstractMacCalculatorFactory
implements MacCalculatorFactory
{
	private String algorithmName;

	@Override
	public String getAlgorithmName() {
		return algorithmName;
	}

	@Override
	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	public boolean isIVSupported() { return true; }

	@Override
	public MacCalculator createMacCalculator(boolean initWithDefaults)
	{
		MacCalculator macCalculator = _createMacCalculator();

		if (initWithDefaults) {
			SecureRandom random = new SecureRandom();
			byte[] key = new byte[macCalculator.getKeySize()];
			random.nextBytes(key);
			if (isIVSupported()) {
				byte[] iv = new byte[macCalculator.getIVSize()];
				random.nextBytes(iv);
				macCalculator.init(new ParametersWithIV(new KeyParameter(key), iv));
			}
			else
				macCalculator.init(new KeyParameter(key));
		}

		macCalculator.setAlgorithmName(getAlgorithmName());

		return macCalculator;
	}

	protected abstract MacCalculator _createMacCalculator();

	public static class DES
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			BlockCipher cipher = new DESEngine();
			return new MacCalculatorImpl(new CBCBlockCipherMac(cipher), cipher.getBlockSize());
		}
	}

	public static class RC2
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			BlockCipher cipher = new RC2Engine();
			return new MacCalculatorImpl(new CBCBlockCipherMac(cipher), cipher.getBlockSize());
		}
	}

	public static class GOST28147
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			return new MacCalculatorImpl(new GOST28147Mac(), 4);
		}
	}

	public static class DESCFB8
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			BlockCipher cipher = new DESEngine();
			return new MacCalculatorImpl(new CFBBlockCipherMac(cipher), cipher.getBlockSize());
		}
	}

	public static class RC2CFB8
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			BlockCipher cipher = new RC2Engine();
			return new MacCalculatorImpl(new CFBBlockCipherMac(cipher), cipher.getBlockSize());
		}
	}

	public static class DES9797Alg3with7816d4
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			BlockCipher cipher = new DESEngine();
			return new MacCalculatorImpl(new ISO9797Alg3Mac(cipher, new ISO7816d4Padding()), cipher.getBlockSize());
		}
	}

	public static class DES9797Alg3
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			BlockCipher cipher = new DESEngine();
			return new MacCalculatorImpl(new ISO9797Alg3Mac(cipher), cipher.getBlockSize());
		}
	}

	public static class MD2
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			Digest digest = new MD2Digest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

	public static class MD4
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			Digest digest = new MD4Digest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

	public static class MD5
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			MD5Digest digest = new MD5Digest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

	public static class SHA1
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			SHA1Digest digest = new SHA1Digest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

	public static class SHA224
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			SHA224Digest digest = new SHA224Digest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

	public static class SHA256
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			SHA256Digest digest = new SHA256Digest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

	public static class SHA384
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			SHA384Digest digest = new SHA384Digest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

	/**
	 * @deprecated See {@link OldHMac}.
	 */
	@Deprecated
	public static class OldSHA384
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			SHA384Digest digest = new SHA384Digest();
			return new MacCalculatorImpl(new OldHMac(digest), digest.getDigestSize());
		}
	}

	public static class SHA512
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			SHA512Digest digest = new SHA512Digest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

	/**
	 * @deprecated See {@link OldHMac}.
	 */
	@Deprecated
	public static class OldSHA512
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			SHA512Digest digest = new SHA512Digest();
			return new MacCalculatorImpl(new OldHMac(digest), digest.getDigestSize());
		}
	}

	public static class RIPEMD128
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			RIPEMD128Digest digest = new RIPEMD128Digest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

	public static class RIPEMD160
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			RIPEMD160Digest digest = new RIPEMD160Digest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

	public static class Tiger
	extends AbstractMacCalculatorFactory
	{
		@Override
		public MacCalculator _createMacCalculator() {
			TigerDigest digest = new TigerDigest();
			return new MacCalculatorImpl(new HMac(digest), digest.getDigestSize());
		}
	}

// TODO implement the following 3 later.
//	//
//	// PKCS12 states that the same algorithm should be used
//	// for the key generation as is used in the HMAC, so that
//	// is what we do here.
//	//
//
//	public static class PBEWithRIPEMD160
//	extends AbstractMacCalculatorFactory
//	{
//		public PBEWithRIPEMD160()
//		{
//			super(new HMac(new RIPEMD160Digest()), PKCS12, RIPEMD160, 160);
//		}
//	}
//
//	public static class PBEWithSHA
//	extends AbstractMacCalculatorFactory
//	{
//		public PBEWithSHA()
//		{
//			super(new HMac(new SHA1Digest()), PKCS12, SHA1, 160);
//		}
//	}
//
//	public static class PBEWithTiger
//	extends AbstractMacCalculatorFactory
//	{
//		public PBEWithTiger()
//		{
//			super(new HMac(new TigerDigest()), PKCS12, TIGER, 192);
//		}
//	}
}
