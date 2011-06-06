package org.cumulus4j.crypto.asymmetric.keypairgenerator;

import org.cumulus4j.crypto.AsymmetricCipherKeyPairGeneratorFactory;

public abstract class AbstractAsymmetricCipherKeyPairGeneratorFactory
implements AsymmetricCipherKeyPairGeneratorFactory
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
}
