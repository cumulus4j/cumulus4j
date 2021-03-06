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
package org.cumulus4j.crypto.internal.mac;

import org.bouncycastle.crypto.CipherParameters;
import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Mac;
import org.cumulus4j.crypto.MACCalculator;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class MACCalculatorImpl
implements MACCalculator
{
	private Mac macEngine;

	private int keySize;
	private int ivSize;

	public MACCalculatorImpl(Mac macEngine, int keySize, int ivSize)
	{
		if (macEngine == null)
			throw new IllegalArgumentException("macEngine == null");

		this.macEngine = macEngine;
		this.keySize = keySize;
		this.ivSize = ivSize;
	}

	private CipherParameters parameters;

	@Override
	public void init(CipherParameters params) throws IllegalArgumentException {
		macEngine.init(params);
		this.parameters = params;
	}

	@Override
	public CipherParameters getParameters() {
		return parameters;
	}

	@Override
	public int getKeySize() {
		return keySize;
	}

	@Override
	public int getIVSize() {
		return ivSize;
	}

	private String algorithmName;

	@Override
	public void setAlgorithmName(String algorithmName) {
		this.algorithmName = algorithmName;
	}

	@Override
	public String getAlgorithmName() {
		if (algorithmName != null)
			return algorithmName;

		return macEngine.getAlgorithmName();
	}

	@Override
	public int getMacSize() {
		return macEngine.getMacSize();
	}

	@Override
	public void update(byte in)
	throws IllegalStateException
	{
		macEngine.update(in);
	}

	@Override
	public void update(byte[] in, int inOff, int len)
	throws DataLengthException, IllegalStateException
	{
		macEngine.update(in, inOff, len);
	}

	@Override
	public int doFinal(byte[] out, int outOff)
	throws DataLengthException, IllegalStateException
	{
		return macEngine.doFinal(out, outOff);
	}

	@Override
	public byte[] doFinal(byte[] in) throws IllegalStateException
	{
		byte[] mac = new byte[getMacSize()];
		update(in, 0, in.length);
		doFinal(mac, 0);
		return mac;
	}

	@Override
	public void reset() {
		macEngine.reset();
	}
}
