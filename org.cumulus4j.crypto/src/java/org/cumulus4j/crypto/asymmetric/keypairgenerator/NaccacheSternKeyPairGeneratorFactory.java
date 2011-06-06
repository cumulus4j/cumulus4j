package org.cumulus4j.crypto.asymmetric.keypairgenerator;

import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;
import org.bouncycastle.crypto.generators.NaccacheSternKeyPairGenerator;

public class NaccacheSternKeyPairGeneratorFactory
extends AbstractAsymmetricCipherKeyPairGeneratorFactory
{
	public NaccacheSternKeyPairGeneratorFactory() {
		setAlgorithmName("NaccacheStern");
	}

	@Override
	public AsymmetricCipherKeyPairGenerator createAsymmetricCipherKeyPairGenerator() {
		NaccacheSternKeyPairGenerator generator = new NaccacheSternKeyPairGenerator();
		return generator;
	}
}
