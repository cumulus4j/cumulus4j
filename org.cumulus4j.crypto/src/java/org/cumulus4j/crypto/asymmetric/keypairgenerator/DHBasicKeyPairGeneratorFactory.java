package org.cumulus4j.crypto.asymmetric.keypairgenerator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.generators.DHBasicKeyPairGenerator;

public class DHBasicKeyPairGeneratorFactory
extends AbstractAsymmetricCipherKeyPairGeneratorFactory
{
	public DHBasicKeyPairGeneratorFactory() {
		setAlgorithmName("DH");
	}

	@Override
	public AsymmetricCipherKeyPairGenerator createAsymmetricCipherKeyPairGenerator() {
		DHBasicKeyPairGenerator generator = new DHBasicKeyPairGenerator();
		return generator;
	}
}
