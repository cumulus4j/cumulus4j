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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.CheckedInputStream;
import java.util.zip.CheckedOutputStream;


/**
 * Container holding all data that is written into the key store file. Instances of this
 * class should never hold unencrypted data - not even in memory!
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
class KeyStoreData
implements Serializable
{
	private static final long serialVersionUID = KeyStoreVersion.VERSION_CURRENT;
	private static final String FILE_HEADER = "Cumulus4jKeyStore";

	private int version = KeyStoreVersion.VERSION_CURRENT;

	Map<String, EncryptedMasterKey> user2keyMap = new HashMap<String, EncryptedMasterKey>();
	Map<Long, EncryptedKey> keyID2keyMap = new HashMap<Long, EncryptedKey>();

	private Map<String, Integer> stringConstant2idMap = new HashMap<String, Integer>();
	private ArrayList<String> stringConstantList = new ArrayList<String>();

	Map<String, EncryptedProperty> name2propertyMap = new HashMap<String, EncryptedProperty>();

	synchronized String stringConstant(String s)
	{
		Integer idx = stringConstant2idMap.get(s);
		if (idx == null) {
			stringConstant2idMap.put(s, stringConstantList.size());
			stringConstantList.add(s);
			return s;
		}
		else {
			String e = stringConstantList.get(idx);
			if (!s.equals(e))
				throw new IllegalStateException("Map and list are not matching!");

			return e;
		}
	}

	public int getVersion() {
		return version;
	}

	private void readObject(java.io.ObjectInputStream in)
	throws IOException, ClassNotFoundException
	{
		readFromStream(in);
	}

	void readFromStream(InputStream in)
	throws IOException
	{
		int headerSizeIncludingVersion = FILE_HEADER.length() + 4; // in bytes

		// We must put the BufferedInputStream here, because we must mark() and reset() - see below. Marco :-)
		in = new BufferedInputStream(in);
		in.mark(headerSizeIncludingVersion);

		DataInputStream din = new DataInputStream(in);

		char[] fileHeaderCharArray = FILE_HEADER.toCharArray();
		char[] buf = new char[fileHeaderCharArray.length];
		for (int i = 0; i < buf.length; i++)
			buf[i] = (char) ( din.readByte() & 0xff );

		if (!Arrays.equals(fileHeaderCharArray, buf))
			throw new IOException("Stream does not start with expected HEADER!");

		int version = din.readInt();
		if (!KeyStoreVersion.VERSION_SUPPORTED_SET.contains(version))
			throw new IOException("Version not supported! Stream contains a keystore of version " + version + " while solely one of version " + KeyStoreVersion.VERSION_SUPPORTED_SET + " is accepted!");

		this.version = version;

		// The checksum is calculated over the complete file, but in order to know which algorithm
		// to use, we first have to read the beginning of the file until including the version.
		// We thus use mark(...) and reset().
		in.reset();

		MessageDigestChecksum checksum = new MessageDigestChecksum.SHA1();
		CheckedInputStream cin = new CheckedInputStream(in, checksum);
		din = new DataInputStream(cin);
		KeyStoreUtil.readByteArrayCompletely(din, new byte[headerSizeIncludingVersion]); // We cannot use skip(...), because that would not update our checksum.

		int stringConstantSize = din.readInt();
		stringConstant2idMap = new HashMap<String, Integer>(stringConstantSize);
		stringConstantList = new ArrayList<String>(stringConstantSize);
		for (int i = 0; i < stringConstantSize; ++i) {
			String stringConstant = din.readUTF();
			stringConstant(stringConstant);
		}

		int user2keyMapSize = din.readInt();
		user2keyMap = new HashMap<String, EncryptedMasterKey>(user2keyMapSize);
		for (int i = 0; i < user2keyMapSize; ++i) {
			EncryptedMasterKey key = new EncryptedMasterKey(this, din, stringConstantList);
			user2keyMap.put(key.getUserName(), key);
		}

		int keyID2keyMapSize = din.readInt();
		keyID2keyMap = new HashMap<Long, EncryptedKey>(keyID2keyMapSize);
		for (int i = 0; i < keyID2keyMapSize; ++i) {
			EncryptedKey key = new EncryptedKey(this, din, stringConstantList);
			keyID2keyMap.put(key.getKeyID(), key);
		}

		int name2propertyMapSize = din.readInt();
		name2propertyMap = new HashMap<String, EncryptedProperty>();
		for (int i = 0; i < name2propertyMapSize; ++i) {
			EncryptedProperty encryptedProperty = new EncryptedProperty(this, din, stringConstantList);
			name2propertyMap.put(encryptedProperty.getName(), encryptedProperty);
		}

		byte[] checksumValueCalculated = checksum.getChecksum();
		byte[] checksumValueExpected = new byte[checksumValueCalculated.length];
		KeyStoreUtil.readByteArrayCompletely(din, checksumValueExpected);
		if (!Arrays.equals(checksumValueCalculated, checksumValueExpected))
			throw new IOException("Checksum error! Expected=" + KeyStoreUtil.encodeHexStr(checksumValueExpected) + " Found=" + KeyStoreUtil.encodeHexStr(checksumValueCalculated));
	}

	private void writeObject(java.io.ObjectOutputStream out)
	throws IOException
	{
		writeToStream(out);
	}

	void writeToStream(OutputStream out)
	throws IOException
	{
		// We always write the newest file version (i.e. convert to a newer version).
		this.version = KeyStoreVersion.VERSION_CURRENT;

		// We calculate the checksum over the complete file.
		MessageDigestChecksum checksum = new MessageDigestChecksum.SHA1();
		CheckedOutputStream cout = new CheckedOutputStream(out, checksum);

		// We put the BufferedOutputStream around the CheckedOutputStream, because this is significantly faster
		// (nearly factor 2; 86 vs 46 sec) and has no disadvantages. Marco.
		DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(cout));

		for (char c : FILE_HEADER.toCharArray()) {
			if (c > 255)
				throw new IllegalStateException("No character in FILE_HEADER should be outside the range 0...255!!! c=" + (int)c);

			dout.writeByte(c);
		}

		dout.writeInt(this.version);

		dout.writeInt(stringConstantList.size());
		for (String stringConstant : stringConstantList) {
			dout.writeUTF(stringConstant);
		}

		dout.writeInt(user2keyMap.size());
		for (EncryptedMasterKey key : user2keyMap.values()) {
			key.write(dout, stringConstant2idMap);
		}

		dout.writeInt(keyID2keyMap.size());
		for (EncryptedKey key : keyID2keyMap.values()) {
			key.write(dout, stringConstant2idMap);
		}

		dout.writeInt(name2propertyMap.size());
		for (EncryptedProperty encryptedProperty : name2propertyMap.values()) {
			encryptedProperty.write(dout, stringConstant2idMap);
		}

		// Flush before fetching checksum, because the checksum is only completely calculated when really all data
		// is written through. Marco :-)
		dout.flush();

		byte[] checksumValue = checksum.getChecksum();
//		logger.debug("store: checksum={}", checksumValue);
		dout.write(checksumValue);
		dout.flush();
	}

//	private void writeKey(DataOutputStream dout, EncryptedKey key) throws IOException
//	{
//		dout.writeInt(key.getData().length);
//		dout.write(key.getData());
//
//		dout.writeInt(key.getSalt().length);
//		dout.write(key.getSalt());
//
//		Integer idx = stringConstant2idMap.get(key.getAlgorithm());
//		dout.writeInt(idx);
//
//		dout.writeInt(key.getEncryptionIV().length);
//		dout.write(key.getEncryptionIV());
//
//		idx = stringConstant2idMap.get(key.getEncryptionAlgorithm());
//		dout.writeInt(idx);
//
//		dout.writeShort(key.getChecksumSize());
//
//		idx = stringConstant2idMap.get(key.getChecksumAlgorithm());
//		dout.writeInt(idx);
//	}
}
