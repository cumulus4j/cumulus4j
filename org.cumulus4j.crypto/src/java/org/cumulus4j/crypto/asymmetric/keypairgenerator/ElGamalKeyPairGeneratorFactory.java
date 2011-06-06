package org.cumulus4j.crypto.asymmetric.keypairgenerator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.generators.ElGamalKeyPairGenerator;

public class ElGamalKeyPairGeneratorFactory
extends AbstractAsymmetricCipherKeyPairGeneratorFactory
{
	public ElGamalKeyPairGeneratorFactory() {
		setAlgorithmName("ElGamal");
	}

	@Override
	public AsymmetricCipherKeyPairGenerator createAsymmetricCipherKeyPairGenerator() {
		ElGamalKeyPairGenerator generator = new ElGamalKeyPairGenerator();
		return generator;
	}
}
