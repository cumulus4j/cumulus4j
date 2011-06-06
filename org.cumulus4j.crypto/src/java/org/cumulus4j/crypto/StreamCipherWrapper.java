package org.cumulus4j.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.StreamCipher;
import org.bouncycastle.crypto.engines.Grain128Engine;
import org.bouncycastle.crypto.engines.Grainv1Engine;
import org.bouncycastle.crypto.engines.HC128Engine;
import org.bouncycastle.crypto.engines.HC256Engine;
import org.bouncycastle.crypto.engines.ISAACEngine;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.engines.Salsa20Engine;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class StreamCipherWrapper
extends AbstractCipher
{
	private StreamCipher delegate;

	public StreamCipherWrapper(String transformation, StreamCipher delegate) {
		super(transformation);
		this.delegate = delegate;
	}

	@Override
	public void _init(CipherOperationMode mode, CipherParameters parameters)
	throws IllegalArgumentException
	{
		delegate.init(CipherOperationMode.ENCRYPT == mode, parameters);
	}

	@Override
	public void reset() {
		delegate.reset();
	}

	@Override
	public int getInputBlockSize() {
		return 1;
	}

	@Override
	public int getOutputBlockSize() {
		return 1;
	}

	@Override
	public int getUpdateOutputSize(int length) {
		return length;
	}

	@Override
	public int getOutputSize(int length) {
		return length;
	}

	@Override
	public int update(byte in, byte[] out, int outOff)
	throws DataLengthException, IllegalStateException, CryptoException
	{
		out[outOff] = delegate.returnByte(in);
		return 1;
	}

	@Override
	public int update(byte[] in, int inOff, int inLen, byte[] out, int outOff)
	throws DataLengthException, IllegalStateException, CryptoException
	{
		delegate.processBytes(in, inOff, inLen, out, outOff);
		return inLen;
	}

	@Override
	public int doFinal(byte[] out, int outOff)
	throws DataLengthException, IllegalStateException, CryptoException
	{
		return 0;
	}

	private int ivSize = -1;

	@Override
	public int getIVSize()
	{
		if (ivSize < 0) {
			if (delegate instanceof Grainv1Engine)
				ivSize = 8;
			else if (delegate instanceof Grain128Engine)
				ivSize = 12;
			else if (delegate instanceof HC128Engine)
				ivSize = 16;
			else if (delegate instanceof HC256Engine)
				ivSize = 32;
			else if (delegate instanceof ISAACEngine)
				ivSize = 0;
			else if (delegate instanceof RC4Engine)
				ivSize = 0;
			else if (delegate instanceof Salsa20Engine)
				ivSize = 8;
			else
				throw new UnsupportedOperationException("For this delegate cipher type, this operation is not yet supported!");
		}
		return ivSize;
	}
}
