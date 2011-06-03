package org.cumulus4j.crypto;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;

public class BlockCipherWrapper
implements Cipher
{
	private BlockCipher delegate;

	public BlockCipherWrapper(BlockCipher delegate) {
		this.delegate = delegate;
	}

	@Override
	public void init(boolean forEncryption, CipherParameters params)
	throws IllegalArgumentException
	{
		delegate.init(forEncryption, params);
	}

	@Override
	public String getAlgorithmName() {
		return delegate.getAlgorithmName();
	}

	public int getBlockSize() {
		return delegate.getBlockSize();
	}

	@Override
	public int processBlock(byte[] in, int inOff, int inLen, byte[] out, int outOff)
	throws DataLengthException, IllegalStateException
	{
		if (inLen == in.length - inOff)
			return delegate.processBlock(in, inOff, out, outOff);

		byte[] delegateIn = new byte[inLen];
		try {
			System.arraycopy(in, inOff, delegateIn, 0, inLen);
		} catch (IndexOutOfBoundsException x) {
			DataLengthException y = new DataLengthException(x.getMessage());
			y.initCause(x);
			throw y;
		}

		return delegate.processBlock(delegateIn, 0, out, outOff);
	}

	public void reset() {
		delegate.reset();
	}
}
