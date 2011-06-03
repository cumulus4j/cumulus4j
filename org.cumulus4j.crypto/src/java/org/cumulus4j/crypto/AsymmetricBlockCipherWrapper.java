package org.cumulus4j.crypto;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.InvalidCipherTextException;

public class AsymmetricBlockCipherWrapper
implements Cipher
{
	private AsymmetricBlockCipher delegate;

	private String algorithmName;

	public AsymmetricBlockCipherWrapper(AsymmetricBlockCipher delegate, String algorithmName) {
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

	public byte[] processBlock(byte[] in, int inOff, int len)
	throws InvalidCipherTextException
	{
		return delegate.processBlock(in, inOff, len);
	}

	@Override
	public void reset() {
		// not necessary?!
	}

	@Override
	public int processBlock(byte[] in, int inOff, int inLen, byte[] out, int outOff)
	throws DataLengthException, IllegalStateException
	{
//		try {
//			delegate.processBlock(in, inOff, inLen);
//		} catch (InvalidCipherTextException e) {
//			throw new IllegalSt
//		}
//		return 0;
		throw new UnsupportedOperationException("NYI");
	}

}
