package org.cumulus4j.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class AbstractCipher implements Cipher
{
	public byte[] doFinal(byte[] in)
	throws DataLengthException, IllegalStateException, CryptoException
	{
		byte[] out = new byte[getOutputSize(in.length)];
		int outOff = update(in, 0, in.length, out, 0);
		outOff += doFinal(out, outOff);

		if (outOff == out.length)
			return out;

		byte[] truncOut = new byte[outOff];
		System.arraycopy(out, 0, truncOut, 0, truncOut.length);
		return truncOut;
	}

	private CipherOperationMode mode;

	private CipherParameters parameters;

	@Override
	public final void init(CipherOperationMode mode, CipherParameters parameters)
	throws IllegalArgumentException
	{
		if (mode == null)
			throw new IllegalArgumentException("mode == null");

		if (parameters == null)
			throw new IllegalArgumentException("parameters == null");

		this.mode = mode;
		this.parameters = parameters;

		_init(mode, parameters);
	}

	@Override
	public CipherOperationMode getMode() {
		return mode;
	}

	@Override
	public CipherParameters getParameters() {
		return parameters;
	}

	protected abstract void _init(CipherOperationMode mode, CipherParameters parameters)
	throws IllegalArgumentException;
}
