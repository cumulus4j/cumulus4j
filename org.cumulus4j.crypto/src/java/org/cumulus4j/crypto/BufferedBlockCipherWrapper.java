package org.cumulus4j.crypto;

import org.bouncycastle.crypto.BlockCipher;
import org.bouncycastle.crypto.BufferedBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.modes.CFBBlockCipher;
import org.bouncycastle.crypto.modes.OFBBlockCipher;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class BufferedBlockCipherWrapper
extends AbstractCipher
{
	private BufferedBlockCipher delegate;

	public BufferedBlockCipherWrapper(BufferedBlockCipher delegate) {
		this.delegate = delegate;
	}

	@Override
	public void _init(CipherOperationMode mode, CipherParameters parameters)
	throws IllegalArgumentException
	{
		delegate.init(CipherOperationMode.ENCRYPT == mode, parameters);
	}

	@Override
	public String getAlgorithmName() {
		return delegate.getUnderlyingCipher().getAlgorithmName();
	}

	@Override
	public int getInputBlockSize() {
		return delegate.getUnderlyingCipher().getBlockSize();
	}

	@Override
	public int getOutputBlockSize() {
		return delegate.getUnderlyingCipher().getBlockSize();
	}

	@Override
	public void reset() {
		delegate.reset();
	}

	@Override
	public int getUpdateOutputSize(int len) {
		return delegate.getUpdateOutputSize(len);
	}

	@Override
	public int getOutputSize(int length) {
		return delegate.getOutputSize(length);
	}

	@Override
	public int update(byte in, byte[] out, int outOff)
	throws DataLengthException, IllegalStateException, CryptoException
	{
		return delegate.processByte(in, out, outOff);
	}

	@Override
	public int update(byte[] in, int inOff, int len, byte[] out, int outOff)
	throws DataLengthException, IllegalStateException, CryptoException
	{
		return delegate.processBytes(in, inOff, len, out, outOff);
	}

	@Override
	public int doFinal(byte[] out, int outOff)
	throws DataLengthException, IllegalStateException, CryptoException
	{
		return delegate.doFinal(out, outOff);
	}

	@Override
	public int getIVSize()
	{
		BlockCipher underlyingCipher = delegate.getUnderlyingCipher();

		if (underlyingCipher instanceof CFBBlockCipher)
			return ((CFBBlockCipher)underlyingCipher).getUnderlyingCipher().getBlockSize();

		if (underlyingCipher instanceof OFBBlockCipher)
			return ((OFBBlockCipher)underlyingCipher).getUnderlyingCipher().getBlockSize();

		return underlyingCipher.getBlockSize();
	}
}
