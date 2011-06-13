package org.cumulus4j.crypto;

public interface MacCalculatorFactory
{
	MacCalculator createMacCalculator(boolean initWithDefaults);

	String getAlgorithmName();

	void setAlgorithmName(String algorithmName);
}
