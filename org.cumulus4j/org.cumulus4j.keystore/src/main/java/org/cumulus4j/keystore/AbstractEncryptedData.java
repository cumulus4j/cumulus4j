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

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
abstract class AbstractEncryptedData
{
	private KeyStoreData keyStoreData;

	public AbstractEncryptedData(KeyStoreData keyStoreData) {
		if (keyStoreData == null)
			throw new IllegalArgumentException("keyStoreData must not be null!");

		this.keyStoreData = keyStoreData;
	}

	public AbstractEncryptedData(
			KeyStoreData keyStoreData, String encryptionAlgorithm,
			byte[] encryptionIV, String macAlgorithm, short macKeySize, short macIVSize,
			short macSize, byte[] encryptedData
	)
	{
		if (keyStoreData == null)
			throw new IllegalArgumentException("keyStoreData must not be null!");

		if (encryptionAlgorithm == null)
			throw new IllegalArgumentException("encryptionAlgorithm must not be null!");

		if (macAlgorithm == null)
			throw new IllegalArgumentException("macAlgorithm must not be null!");

		if (macKeySize < 0)
			throw new IllegalArgumentException("macKeySize < 0");

		if (macIVSize < 0)
			throw new IllegalArgumentException("macIVSize < 0");

		if (macSize < 0)
			throw new IllegalArgumentException("macSize < 0");

		if (encryptedData == null)
			throw new IllegalArgumentException("encryptedData must not be null!");

		this.keyStoreData = keyStoreData;
		this.encryptionAlgorithm = encryptionAlgorithm;
		this.encryptionIV = encryptionIV;
		this.macAlgorithm = macAlgorithm;
		this.macKeySize = macKeySize;
		this.macIVSize = macIVSize;
		this.macSize = macSize;
		this.encryptedData = encryptedData;
	}

	protected KeyStoreData getKeyStoreData() {
		return keyStoreData;
	}

	private String encryptionAlgorithm;

	public String getEncryptionAlgorithm() {
		return encryptionAlgorithm;
	}

	byte[] encryptionIV;

	public byte[] getEncryptionIV() {
		return encryptionIV;
	}

	private String macAlgorithm;

	public String getMACAlgorithm() {
		return macAlgorithm;
	}

	private short macKeySize;

	public short getMACKeySize() {
		return macKeySize;
	}

	private short macIVSize;

	public short getMACIVSize() {
		return macIVSize;
	}

	private short macSize;

	public short getMACSize() {
		return macSize;
	}

	private byte[] encryptedData;

	public byte[] getEncryptedData() {
		return encryptedData;
	}

	/**
	 * Get the size in bytes of the header field 'length' for the encrypted data. This can be either 2 (short) or 4 (integer).
	 * @return the size (in bytes) of the 'length' of the encrypted data. Only 2 or 4 are allowed.
	 */
	protected abstract byte getEncryptedDataLengthHeaderSize();

	public void read(DataInputStream din, ArrayList<String> stringConstantList) throws IOException
	{
		int idx = din.readInt();
		encryptionAlgorithm = stringConstantList.get(idx);
		if (encryptionAlgorithm == null)
			throw new IllegalStateException("encryptionAlgorithm == null, because stringConstantList.get(idx) returned null for idx=" + idx);

		encryptionIV = KeyStoreUtil.readByteArrayWithShortLengthHeader(din);

		idx = din.readInt();
		macAlgorithm = stringConstantList.get(idx);
		if (macAlgorithm == null)
			throw new IllegalStateException("macAlgorithm == null, because stringConstantList.get(idx) returned null for idx=" + idx);

		macKeySize = din.readShort();
		macIVSize = din.readShort();
		macSize = din.readShort();

		byte encryptedDataLengthHeaderSize = getEncryptedDataLengthHeaderSize();
		switch (encryptedDataLengthHeaderSize) {
			case 2:
				encryptedData = KeyStoreUtil.readByteArrayWithShortLengthHeader(din);
				break;
			case 4:
				encryptedData = KeyStoreUtil.readByteArrayWithIntegerLengthHeader(din);
				break;
			default:
				throw new IllegalStateException("Implementation error in class " + this.getClass().getName() + ": Method 'getEncryptedDataLengthHeaderSize()' returned an illegal value: " + encryptedDataLengthHeaderSize);
		}
	}

	public void write(DataOutputStream out, Map<String, Integer> stringConstant2idMap) throws IOException
	{
		Integer idx;

		idx = stringConstant2idMap.get(encryptionAlgorithm);
		if (idx == null)
			throw new IllegalStateException("stringConstant2idMap.get(...) returned null for encryptionAlgorithm=\"" + encryptionAlgorithm + "\"!");
		out.writeInt(idx);

		KeyStoreUtil.writeByteArrayWithShortLengthHeader(out, encryptionIV);


		idx = stringConstant2idMap.get(macAlgorithm);
		if (idx == null)
			throw new IllegalStateException("stringConstant2idMap.get(...) returned null for macAlgorithm=\"" + macAlgorithm + "\"!");
		out.writeInt(idx);

		out.writeShort(macKeySize);
		out.writeShort(macIVSize);
		out.writeShort(macSize);

		byte encryptedDataLengthHeaderSize = getEncryptedDataLengthHeaderSize();
		switch (encryptedDataLengthHeaderSize) {
			case 2:
				KeyStoreUtil.writeByteArrayWithShortLengthHeader(out, encryptedData);
				break;
			case 4:
				KeyStoreUtil.writeByteArrayWithIntegerLengthHeader(out, encryptedData);
				break;
			default:
				throw new IllegalStateException("Implementation error in class '" + this.getClass().getName() + "': Method 'getEncryptedDataLengthHeaderSize()' returned an illegal value: " + encryptedDataLengthHeaderSize);
		}
	}
}
