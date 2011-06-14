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
package org.cumulus4j.keystore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.cumulus4j.crypto.util.ChecksumAlgorithm;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
abstract class AbstractEncryptedKey
{
	public AbstractEncryptedKey() { }

	public AbstractEncryptedKey(
			byte[] data, byte[] encryptionIV, String encryptionAlgorithm, short checksumSize, ChecksumAlgorithm checksumAlgorithm
	)
	{
		if (data == null)
			throw new IllegalArgumentException("data must not be null!");

		if (encryptionAlgorithm == null)
			throw new IllegalArgumentException("encryptionAlgorithm must not be null!");

		if (checksumSize < 1)
			throw new IllegalArgumentException("checksumSize < 1");

		if (checksumAlgorithm == null)
			throw new IllegalArgumentException("checksumAlgorithm must not be null!");

		this.data = data;
		this.encryptionIV = encryptionIV;
		this.encryptionAlgorithm = encryptionAlgorithm;
		this.checksumSize = checksumSize;
		this.checksumAlgorithm = checksumAlgorithm;
	}

	private byte[] data;

	public byte[] getData() {
		return data;
	}

	byte[] encryptionIV;

	public byte[] getEncryptionIV() {
		return encryptionIV;
	}

	private String encryptionAlgorithm;

	public String getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	private short checksumSize;

	public short getChecksumSize() {
		return checksumSize;
	}

	private ChecksumAlgorithm checksumAlgorithm;

	public ChecksumAlgorithm getChecksumAlgorithm() {
		return checksumAlgorithm;
	}

	public void read(DataInputStream din, ArrayList<String> stringConstantList) throws IOException
	{
		data = KeyStoreUtil.readByteArrayWithLengthHeader(din);

//		int algorithmIdx = din.readInt();
//		algorithm = stringConstantList.get(algorithmIdx);

		encryptionIV = KeyStoreUtil.readByteArrayWithLengthHeader(din);

		int idx = din.readInt();
		encryptionAlgorithm = stringConstantList.get(idx);

		checksumSize = din.readShort();

		idx = din.readInt();
		String checksumAlgorithmName = stringConstantList.get(idx);
		checksumAlgorithm = ChecksumAlgorithm.valueOf(checksumAlgorithmName);
	}

	public void write(DataOutputStream out, Map<String, Integer> stringConstant2idMap) throws IOException
	{
		KeyStoreUtil.writeByteArrayWithLengthHeader(out, data);

		Integer idx;
//		Integer idx = stringConstant2idMap.get(algorithm);
//		out.writeInt(idx);

		KeyStoreUtil.writeByteArrayWithLengthHeader(out, encryptionIV);

		idx = stringConstant2idMap.get(encryptionAlgorithm);
		out.writeInt(idx);

		out.writeShort(checksumSize);

		idx = stringConstant2idMap.get(checksumAlgorithm.name());
		if (idx == null)
			throw new IllegalStateException("Entry missing in stringConstant2idMap for key=\"" + checksumAlgorithm.name() + "\"!");

		out.writeInt(idx);
	}
}
