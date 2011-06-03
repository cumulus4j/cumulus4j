package org.cumulus4j.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.StreamCipher;

public class StreamCipherWrapper
implements Cipher
{
	private StreamCipher delegate;

	public StreamCipherWrapper(StreamCipher delegate) {
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

	@Override
	public void reset() {
		delegate.reset();
	}

	@Override
	public int processBlock(byte[] in, int inOff, int inLen, byte[] out, int outOff)
	throws DataLengthException, IllegalStateException
	{
		delegate.processBytes(in, inOff, inLen, out, outOff);
		return inLen;
	}
}
