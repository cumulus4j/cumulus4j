package org.cumulus4j.crypto.mac;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Mac;
import org.cumulus4j.crypto.MacCalculator;

public class MacCalculatorImpl
implements MacCalculator
{
	private Mac macEngine;

	public MacCalculatorImpl(Mac macEngine)
	{
		if (macEngine == null)
			throw new IllegalArgumentException("macEngine == null");
	}

	@Override
	public void init(CipherParameters params) throws IllegalArgumentException {
		macEngine.init(params);
	}

	@Override
	public String getAlgorithmName() {
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
