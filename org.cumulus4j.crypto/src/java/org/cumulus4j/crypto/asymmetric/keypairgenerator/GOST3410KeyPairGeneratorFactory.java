package org.cumulus4j.crypto.asymmetric.keypairgenerator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.generators.GOST3410KeyPairGenerator;

public class GOST3410KeyPairGeneratorFactory
extends AbstractAsymmetricCipherKeyPairGeneratorFactory
{
	public GOST3410KeyPairGeneratorFactory() {
		setAlgorithmName("GOST3410");
	}

	@Override
	public AsymmetricCipherKeyPairGenerator createAsymmetricCipherKeyPairGenerator() {
		GOST3410KeyPairGenerator generator = new GOST3410KeyPairGenerator();
		return generator;
	}
}
