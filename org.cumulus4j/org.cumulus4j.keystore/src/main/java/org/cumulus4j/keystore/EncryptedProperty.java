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

import org.cumulus4j.keystore.prop.Property;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class EncryptedProperty
extends AbstractEncryptedData
{
	public EncryptedProperty(
			KeyStoreData keyStoreData, String name,
			Class<? extends Property<?>> type, String encryptionAlgorithm,
			byte[] encryptionIV, String macAlgorithm, short macKeySize, short macIVSize,
			short macSize, byte[] data
	)
	{
		super(
				keyStoreData, encryptionAlgorithm,
				encryptionIV, macAlgorithm, macKeySize, macIVSize,
				macSize, data
		);

		if (name == null)
			throw new IllegalArgumentException("name == null");

		if (type == null)
			throw new IllegalArgumentException("type == null");

		this.name = name;
		this.type = type;
	}

	public EncryptedProperty(KeyStoreData keyStoreData, DataInputStream din, ArrayList<String> stringConstantList) throws IOException
	{
		super(keyStoreData);
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

		super.read(din, stringConstantList);
	}

	@Override
	public void write(DataOutputStream dout, Map<String, Integer> stringConstant2idMap) throws IOException
	{
		dout.writeUTF(name);

		Integer typeNameStringConstantID = stringConstant2idMap.get(type.getName());
		if (typeNameStringConstantID == null)
			throw new IllegalStateException("There is no entry in stringConstant2idMap for the type \"" + type.getName() + "\"!!!");

		dout.writeInt(typeNameStringConstantID);

		super.write(dout, stringConstant2idMap);
	}

	private String name;

	private Class<? extends Property<?>> type;

	public String getName() {
		return name;
	}

	public Class<? extends Property<?>> getType() {
		return type;
	}

	@Override
	protected byte getEncryptedDataLengthHeaderSize() {
		// 2012-02-18 switched to 4 - see https://sourceforge.net/tracker/?func=detail&aid=3453405&group_id=517465&atid=2102911
		// In order to stay compatible with older versions, we check the file version (version 1 used 2 bytes instead of 4).
		if (getKeyStoreData().getVersion() == 1)
			return 2;
		else
			return 4;
	}
}
