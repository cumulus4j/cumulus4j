package org.cumulus4j.crypto;

import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

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
}
