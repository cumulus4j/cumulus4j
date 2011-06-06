package org.cumulus4j.crypto.asymmetric.keypairgenerator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.generators.RSAKeyPairGenerator;

public class RSAKeyPairGeneratorFactory
extends AbstractAsymmetricCipherKeyPairGeneratorFactory
{
	public RSAKeyPairGeneratorFactory() {
		setAlgorithmName("RSA");
	}

	@Override
	public AsymmetricCipherKeyPairGenerator createAsymmetricCipherKeyPairGenerator() {
		RSAKeyPairGenerator generator = new RSAKeyPairGenerator();
		return generator;
	}
}
