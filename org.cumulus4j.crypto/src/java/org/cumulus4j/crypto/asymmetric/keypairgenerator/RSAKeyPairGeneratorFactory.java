package org.cumulus4j.crypto.asymmetric.keypairgenerator;

import java.math.BigInteger;
import java.security.SecureRandom;

import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;
import org.bouncycastle.crypto.params.RSAKeyGenerationParameters;

public class RSAKeyPairGeneratorFactory
extends AbstractAsymmetricCipherKeyPairGeneratorFactory
{
	public RSAKeyPairGeneratorFactory() {
		setAlgorithmName("RSA");
	}

	private static final BigInteger defaultPublicExponent = BigInteger.valueOf(0x10001);
	private static final int defaultTests = 12;

	private SecureRandom random = new SecureRandom();

	@Override
	public AsymmetricCipherKeyPairGenerator createAsymmetricCipherKeyPairGenerator() {
		RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
		generator.init(new RSAKeyGenerationParameters(defaultPublicExponent, random, 1024, defaultTests));
		return generator;
	}
}
