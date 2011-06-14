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
import org.cumulus4j.keystore.prop.Property;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class EncryptedProperty
{
	public EncryptedProperty(
			String name, Class<? extends Property<?>> type,
					byte[] data, byte[] encryptionIV, String encryptionAlgorithm,
					short checksumSize, ChecksumAlgorithm checksumAlgorithm
	)
	{
		if (name == null)
			throw new IllegalArgumentException("name == null");

		if (type == null)
			throw new IllegalArgumentException("type == null");

		if (data == null)
			throw new IllegalArgumentException("data == null");

		if (encryptionIV == null)
			throw new IllegalArgumentException("encryptionIV == null");

		if (encryptionAlgorithm == null)
			throw new IllegalArgumentException("encryptionAlgorithm == null");

		if (checksumSize < 1)
			throw new IllegalArgumentException("checksumSize < 1");

		if (checksumAlgorithm == null)
			throw new IllegalArgumentException("checksumAlgorithm == null");

		this.name = name;
		this.type = type;
		this.data = data;
		this.encryptionIV = encryptionIV;
		this.encryptionAlgorithm = encryptionAlgorithm;
		this.checksumSize = checksumSize;
		this.checksumAlgorithm = checksumAlgorithm;
	}


	public EncryptedProperty(DataInputStream din, ArrayList<String> stringConstantList) throws IOException
	{
		name = din.readUTF();

		int typeNameStringConstantID = din.readInt();
		String typeName = stringConstantList.get(typeNameStringConstantID);
		try {
			@SuppressWarnings("unchecked")
			Class<? extends Property<?>> c = (Class<? extends Property<?>>) Class.forName(typeName);
			type = c;
		} catch (ClassNotFoundException e) {
			throw new IOException("String constant index " + typeNameStringConstantID + " points to \"" + typeName + "\", but the class with this name cannot be found!", e);
		}

		data = KeyStoreUtil.readByteArrayWithLengthHeader(din);
		encryptionIV = KeyStoreUtil.readByteArrayWithLengthHeader(din);
//		encryptionAlgorithm = din.readUTF();
//		checksumSize = din.readShort();
//		checksumAlgorithm = din.readUTF();

		int idx = din.readInt();
		encryptionAlgorithm = stringConstantList.get(idx);

		checksumSize = din.readShort();

		idx = din.readInt();
		String checksumAlgorithmName = stringConstantList.get(idx);
		checksumAlgorithm = ChecksumAlgorithm.valueOf(checksumAlgorithmName);
	}

	public void write(DataOutputStream dout, Map<String, Integer> stringConstant2idMap) throws IOException
	{
		dout.writeUTF(name);

		Integer typeNameStringConstantID = stringConstant2idMap.get(type.getName());
		if (typeNameStringConstantID == null)
			throw new IllegalStateException("There is no entry in stringConstant2idMap for the type \"" + type.getName() + "\"!!!");

		dout.writeInt(typeNameStringConstantID);

		KeyStoreUtil.writeByteArrayWithLengthHeader(dout, data);
		KeyStoreUtil.writeByteArrayWithLengthHeader(dout, encryptionIV);
//		dout.writeUTF(encryptionAlgorithm);
//		dout.writeShort(checksumSize);
//		dout.writeUTF(checksumAlgorithm);

		Integer idx = stringConstant2idMap.get(encryptionAlgorithm);
		dout.writeInt(idx);

		dout.writeShort(checksumSize);

		idx = stringConstant2idMap.get(checksumAlgorithm.name());
		if (idx == null)
			throw new IllegalStateException("Entry missing in stringConstant2idMap for key=\"" + checksumAlgorithm.name() + "\"!");

		dout.writeInt(idx);
	}

	private String name;

	private Class<? extends Property<?>> type;

	public String getName() {
		return name;
	}

	public Class<? extends Property<?>> getType() {
		return type;
	}

	private byte[] data;

	public byte[] getData() {
		return data;
	}

	private byte[] encryptionIV;

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

}
