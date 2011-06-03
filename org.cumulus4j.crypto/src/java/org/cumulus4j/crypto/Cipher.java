package org.cumulus4j.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public interface Cipher
{
	void init(CipherOperationMode mode, CipherParameters parameters)
	throws IllegalArgumentException;

	/**
	 * Get the mode of this cipher. This is <code>null</code>, before
	 * {@link #init(CipherOperationMode, CipherParameters)} was called the first
	 * time.
	 * @return the mode of this cipher.
	 */
	CipherOperationMode getMode();

	/**
	 * Get the parameters of this cipher. This is <code>null</code>, before
	 * {@link #init(CipherOperationMode, CipherParameters)} was called the first
	 * time.
	 * @return the parameters of this cipher.
	 */
	CipherParameters getParameters();

	String getAlgorithmName();

	void reset();

	/**
   * Get the input block size for this cipher (in bytes).
   * If this is a symmetric cipher, this equals {@link #getOutputBlockSize()}.
   *
   * @return the input block size for this cipher in bytes.
   */
	int getInputBlockSize();

	/**
   * Get the output block size for this cipher (in bytes).
   * If this is a symmetric cipher, this equals {@link #getInputBlockSize()}.
   *
   * @return the output block size for this cipher in bytes.
   */
	int getOutputBlockSize();

	int getUpdateOutputSize(int length);

	int getOutputSize(int length);

	int update(byte in, byte[] out, int outOff) throws DataLengthException,
			IllegalStateException, CryptoException;

	int update(byte[] in, int inOff, int inLen, byte[] out, int outOff)
			throws DataLengthException, IllegalStateException, CryptoException;

	int doFinal(byte[] out, int outOff) throws DataLengthException,
			IllegalStateException, CryptoException;

	/**
	 * Convenience method to encrypt/decrypt the complete input byte array at once.
	 * @param in the input to be encrypted or decrypted.
	 * @return the processed output.
	 * @throws IllegalStateException if the cipher isn't initialised.
	 * @throws CryptoException if padding is expected and not found or sth. else goes wrong while encrypting or decrypting.
	 */
	byte[] doFinal(byte[] in)
	throws IllegalStateException, CryptoException;

	/**
	 * Get the required size of the IV (in bytes). If a cipher supports multiple sizes, this is the optimal (most secure) IV size.
	 * @return the required size of the IV.
	 */
	int getIVSize();
}
