package org.cumulus4j.crypto.asymmetric.keypairgenerator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.generators.DSAKeyPairGenerator;

public class DSAKeyPairGeneratorFactory
extends AbstractAsymmetricCipherKeyPairGeneratorFactory
{
	public DSAKeyPairGeneratorFactory() {
		setAlgorithmName("DSA");
	}

	@Override
	public AsymmetricCipherKeyPairGenerator createAsymmetricCipherKeyPairGenerator() {
		DSAKeyPairGenerator generator = new DSAKeyPairGenerator();
		return generator;
	}
}
