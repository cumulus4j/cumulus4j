package org.cumulus4j.crypto;

import org.bouncycastle.crypto.AsymmetricCipherKeyPairGenerator;

public interface AsymmetricCipherKeyPairGeneratorFactory
{
	/**
	 * Create and initialize a new instance of {@link AsymmetricCipherKeyPairGenerator}.
	 * @return a new ready-to-use instance of {@link AsymmetricCipherKeyPairGenerator}.
	 */
	AsymmetricCipherKeyPairGenerator createAsymmetricCipherKeyPairGenerator();

	String getAlgorithmName();

	void setAlgorithmName(String algorithmName);
}
