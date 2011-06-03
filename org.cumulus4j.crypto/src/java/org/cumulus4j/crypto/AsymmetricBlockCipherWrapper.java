package org.cumulus4j.crypto;

import org.bouncycastle.crypto.BufferedAsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

class AsymmetricBlockCipherWrapper
extends AbstractCipher
{
	private BufferedAsymmetricBlockCipher delegate;

	private String algorithmName;

	public AsymmetricBlockCipherWrapper(BufferedAsymmetricBlockCipher delegate, String algorithmName) {
		this.delegate = delegate;
		this.algorithmName = algorithmName;
	}

	@Override
	public String getAlgorithmName() {
		return algorithmName;
	}

	@Override
	public void init(boolean forEncryption, CipherParameters param) {
		delegate.init(forEncryption, param);
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
