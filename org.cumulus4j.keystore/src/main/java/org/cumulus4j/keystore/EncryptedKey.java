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
class EncryptedKey
extends AbstractEncryptedData
{
	private long keyID;

	public EncryptedKey(
			long keyID,
			String encryptionAlgorithm, byte[] encryptionIV,
			String macAlgorithm, short macKeySize, short macIVSize, short macSize,
			byte[] data
	)
	{
		super(
				encryptionAlgorithm, encryptionIV,
				macAlgorithm, macKeySize, macIVSize, macSize,
				data
		);
		this.keyID = keyID;
	}

	public long getKeyID() {
		return keyID;
	}

	public EncryptedKey(DataInputStream in, ArrayList<String> stringConstantList) throws IOException
	{
		keyID = in.readLong();
		read(in, stringConstantList);
	}

	@Override
	public void write(DataOutputStream out, Map<String, Integer> stringConstant2idMap) throws IOException
	{
		out.writeLong(keyID);
		super.write(out, stringConstant2idMap);
	}

	@Override
	protected byte getEncryptedDataLengthHeaderSize() {
		return 2;
	}
}
