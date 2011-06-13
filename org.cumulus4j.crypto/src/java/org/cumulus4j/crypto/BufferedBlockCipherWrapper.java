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

	public BufferedBlockCipherWrapper(String transformation, BufferedBlockCipher delegate) {
		super(transformation);
		this.delegate = delegate;
	}

	@Override
	public void _init(CipherOperationMode mode, CipherParameters parameters)
	throws IllegalArgumentException
	{
		delegate.init(CipherOperationMode.ENCRYPT == mode, parameters);
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
