package org.cumulus4j.crypto;

import org.bouncycastle.crypto.BufferedAsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class AsymmetricBlockCipherWrapper
extends AbstractCipher
{
	private BufferedAsymmetricBlockCipher delegate;

	public AsymmetricBlockCipherWrapper(String transformation, BufferedAsymmetricBlockCipher delegate) {
		super(transformation);
		this.delegate = delegate;
	}

	@Override
	public void _init(CipherOperationMode mode, CipherParameters parameters) {
		delegate.init(CipherOperationMode.ENCRYPT == mode, parameters);
	}

	public int getInputBlockSize() {
		return delegate.getInputBlockSize();
	}

	public int getOutputBlockSize() {
		return delegate.getOutputBlockSize();
	}

	@Override
	public void reset() {
		// does not exist in delegate => not necessary?!
	}

	@Override
	public int getUpdateOutputSize(int length) {
		return getOutputSize(length); // this is not correct and very pessimistic, but for now, it is at least sth. that shouldn't produce any errors (the result should be >= the real value).
	}

	@Override
	public int getOutputSize(int length) {
		return getOutputBlockSize(); // Copied this from org.bouncycastle.jce.provider.JCERSACipher.
	}

	@Override
	public int update(byte in, byte[] out, int outOff)
	throws DataLengthException, IllegalStateException, CryptoException
	{
		delegate.processByte(in);
		return 0;
	}

	@Override
	public int update(byte[] in, int inOff, int inLen, byte[] out, int outOff)
	throws DataLengthException, IllegalStateException, CryptoException
	{
		delegate.processBytes(in, inOff, inLen);
		return 0;
	}

	@Override
	public int doFinal(byte[] out, int outOff)
	throws DataLengthException, IllegalStateException, CryptoException
	{
		byte[] encrypted = delegate.doFinal();
		System.arraycopy(encrypted, 0, out, 0, encrypted.length);
		return encrypted.length;
	}

	@Override
	public int getIVSize() {
		return 0;
	}
}
