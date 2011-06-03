package org.cumulus4j.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface Cipher
{

	void init(boolean forEncryption, CipherParameters params)
	throws IllegalArgumentException;

	String getAlgorithmName();

	/**
	 * Process one block of input from the array <code>in</code> and write it to
	 * the <code>out</code> array.
	 *
	 * @param in the array containing the input data.
	 * @param inOff offset into the <code>in</code> array the data starts at.
	 * @param inLen the number of bytes from <code>in</code> to read (starting at the given offset <code>inOff</code>).
	 * @param out the array the output data will be copied into.
	 * @param outOff the offset into the out array the output will start at.
	 * @exception DataLengthException if there isn't enough data in <code>in</code>, or
	 * space in <code>out</code>.
	 * @exception IllegalStateException if the cipher isn't initialised.
	 * @return the number of bytes processed and produced.
	 */
	int processBlock(byte[] in, int inOff, int inLen, byte[] out, int outOff)
	throws DataLengthException, IllegalStateException;

	void reset();
}
