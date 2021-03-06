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

class EncryptedMasterKey
extends AbstractEncryptedData
{
	private String userName;

	public String getUserName() {
		return userName;
	}

	private int passwordBasedKeySize; // in bits, normally 128 or 256,

	public int getPasswordBasedKeySize() {
		return passwordBasedKeySize;
	}

	private int passwordBasedIterationCount;

	public int getPasswordBasedIterationCount() {
		return passwordBasedIterationCount;
	}

	private String passwordBasedKeyGeneratorAlgorithm;

	public String getPasswordBasedKeyGeneratorAlgorithm() {
		return passwordBasedKeyGeneratorAlgorithm;
	}

	private byte[] salt;

	public byte[] getSalt() {
		return salt;
	}

	public EncryptedMasterKey(
			KeyStoreData keyStoreData, String userName, int passwordBasedKeySize, int passwordBasedIterationCount,
			String passwordBasedKeyGeneratorAlgorithm,
			byte[] salt, String encryptionAlgorithm,
			byte[] encryptionIV, String macAlgorithm, short macKeySize, short macIVSize,
			short macSize, byte[] data
	) {
		super(
				keyStoreData, encryptionAlgorithm,
				encryptionIV, macAlgorithm, macKeySize, macIVSize,
				macSize, data
		);

		if (userName == null)
			throw new IllegalArgumentException("userName == null");

		if (salt == null)
			throw new IllegalArgumentException("salt == null");

		this.userName = userName;
		this.passwordBasedKeySize = passwordBasedKeySize;
		this.passwordBasedIterationCount = passwordBasedIterationCount;
		this.passwordBasedKeyGeneratorAlgorithm = passwordBasedKeyGeneratorAlgorithm;
		this.salt = salt;
	}

	public EncryptedMasterKey(KeyStoreData keyStoreData, DataInputStream in, ArrayList<String> stringConstantList) throws IOException
	{
		super(keyStoreData);
		userName = in.readUTF();
		passwordBasedKeySize = in.readInt();
		passwordBasedIterationCount = in.readInt();

		int idx = in.readInt();
		passwordBasedKeyGeneratorAlgorithm = stringConstantList.get(idx);
		if (passwordBasedKeyGeneratorAlgorithm == null)
			throw new IllegalStateException("stringConstantList.get(idx) returned null for idx=" + idx);

		salt = KeyStoreUtil.readByteArrayWithShortLengthHeader(in);
		read(in, stringConstantList);
	}

	@Override
	public void write(DataOutputStream out, Map<String, Integer> stringConstant2idMap)
	throws IOException
	{
		out.writeUTF(userName);

		out.writeInt(passwordBasedKeySize);
		out.writeInt(passwordBasedIterationCount);

		Integer idx = stringConstant2idMap.get(passwordBasedKeyGeneratorAlgorithm);
		if (idx == null)
			throw new IllegalStateException("stringConstant2idMap.get(passwordBasedKeyGeneratorAlgorithm) returned null for passwordBasedKeyGeneratorAlgorithm=\"" + passwordBasedKeyGeneratorAlgorithm + "\"!");

		out.writeInt(idx);

		KeyStoreUtil.writeByteArrayWithShortLengthHeader(out, salt);
		super.write(out, stringConstant2idMap);
	}

	@Override
	protected byte getEncryptedDataLengthHeaderSize() {
		return 2;
	}
}
