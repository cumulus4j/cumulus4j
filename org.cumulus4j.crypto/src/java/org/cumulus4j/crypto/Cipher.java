/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cumulus4j.crypto;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.CryptoException;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 * <p>
 * A cipher encrypts or decrypts data.
 * </p>
 * <p>
 * This interface defines the algorithm-independent API contract to allow
 * for encrypting and decrypting data. It has been introduced in analogy
 * to {@link javax.crypto.Cipher} and with easy migration from JCE
 * to this API in mind.
 * </p>
 * <p>
 * <b>Important: <code>Cipher</code>s are not thread-safe!</b>
 * </p>
 * <p>
 * Use {@link CryptoRegistry#createCipher(String)} to obtain a <code>Cipher</code> instance.
 * </p>
 * <p>
 * This own API is used instead of the JCE, because of the following reasons:
 * </p>
 * <ul>
 *  <li>The JCE has a key length constraint (maximum 128 bit) that requires manual modifications of
 * the Java runtime environment (installing some files that are not included in the operating system's
 * package management).</li>
 * 	<li>The {@link BouncyCastleProvider} was not correctly registered in the JCE when using One-JAR to
 * package e.g. the <code>org.cumulus4j.keystore.cli</code>. Probably because the signatures where not
 * found when looking for the MANIFEST.MF (probably the wrong MANIFEST.MF was picked by the class loader).
 * 	</li>
 * </ul>
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

	/**
	 * Get the transformation that was passed to {@link CryptoRegistry#createCipher(String)}
	 * for obtaining this <code>Cipher</code>.
	 * @return the transformation (encryption algorithm, mode and padding) of this cipher.
	 */
	String getTransformation();

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

//	/**
//	 * Get a ready-to-use <code>SecretKeyGenerator</code>.
//	 * @param initWithDefaults TODO
//	 * @return a <code>SecretKeyGenerator</code> appropriate for this Cipher and ready to be used.
//	 * @throws UnsupportedOperationException
//	 */
//	SecretKeyGenerator createSecretKeyGenerator(boolean initWithDefaults)
//	throws UnsupportedOperationException;
//
//	/**
//	 * Get a ready-to-use <code>AsymmetricCipherKeyPairGenerator</code>.
//	 * @param initWithDefaults TODO
//	 * @return an <code>AsymmetricCipherKeyPairGenerator</code> appropriate for this <code>Cipher</code> and ready to be used.
//	 * @throws UnsupportedOperationException if this <code>Cipher</code> does not implement asymmetric encryption.
//	 * @see #createSecretKeyGenerator(boolean)
//	 */
//	AsymmetricCipherKeyPairGenerator createKeyPairGenerator(boolean initWithDefaults)
//	throws UnsupportedOperationException;
}
