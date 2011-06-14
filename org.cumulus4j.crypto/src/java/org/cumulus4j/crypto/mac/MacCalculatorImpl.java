package org.cumulus4j.crypto.mac;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Mac;
import org.cumulus4j.crypto.MacCalculator;

public class MacCalculatorImpl
implements MacCalculator
{
	private Mac macEngine;

	private int keySize;
	private int ivSize;

	public MacCalculatorImpl(Mac macEngine, int keyAndIVSize)
	{
		this(macEngine, keyAndIVSize, keyAndIVSize);
	}

	public MacCalculatorImpl(Mac macEngine, int keySize, int ivSize)
	{
		if (macEngine == null)
			throw new IllegalArgumentException("macEngine == null");

		this.macEngine = macEngine;
		this.keySize = keySize;
		this.ivSize = ivSize;
	}

	private CipherParameters parameters;

	@Override
	public void init(CipherParameters params) throws IllegalArgumentException {
		macEngine.init(params);
		this.parameters = params;
	}

	@Override
	public CipherParameters getParameters() {
		return parameters;
	}

	@Override
	public int getKeySize() {
		return keySize;
	}

	@Override
	public int getIVSize() {
		return ivSize;
	}

	private String algorithmName;

	@Override
	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	@Override
	public String getAlgorithmName() {
		if (algorithmName != null)
			return algorithmName;

		return macEngine.getAlgorithmName();
	}

	@Override
	public int getMacSize() {
		return macEngine.getMacSize();
	}

	@Override
	public void update(byte in) throws IllegalStateException {
		macEngine.update(in);
	}

	@Override
	public void update(byte[] in, int inOff, int len) throws DataLengthException,
			IllegalStateException {
		macEngine.update(in, inOff, len);
	}

	@Override
	public int doFinal(byte[] out, int outOff) throws DataLengthException,
			IllegalStateException {
		return macEngine.doFinal(out, outOff);
	}

	@Override
	public void reset() {
		macEngine.reset();
	}
}
