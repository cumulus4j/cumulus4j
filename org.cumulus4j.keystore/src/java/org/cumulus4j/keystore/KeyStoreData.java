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
public class KeyStoreData
implements Serializable
{
	private static final int FILE_VERSION = 1;
	private static final long serialVersionUID = FILE_VERSION;
	private static final String FILE_HEADER = "Cumulus4jKeyStore";

//	long nextKeyID = 1;
	Map<String, EncryptedKey> user2keyMap = new HashMap<String, EncryptedKey>();
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

	private EncryptedKey readKey(DataInputStream din) throws IOException
	{
		int keySize = din.readInt();
		byte[] key = new byte[keySize];
		KeyStoreUtil.readByteArrayCompletely(din, key);

		int saltSize = din.readInt();
		byte[] salt = saltSize == 0 ? null : new byte[saltSize];
		if (salt != null)
			KeyStoreUtil.readByteArrayCompletely(din, salt);

		int algorithmIdx = din.readInt();
		String algorithm = stringConstantList.get(algorithmIdx);

		int keyCryptIVSize = din.readInt();
		byte[] keyCryptIV = keyCryptIVSize == 0 ? null : new byte[keyCryptIVSize];
		if (keyCryptIV != null)
			KeyStoreUtil.readByteArrayCompletely(din, keyCryptIV);

		int keyCryptAlgoIdx = din.readInt();
		String keyCryptAlgo = stringConstantList.get(keyCryptAlgoIdx);

		short checksumSize = din.readShort();

		int checksumAlgoIdx = din.readInt();
		String checksumAlgo = stringConstantList.get(checksumAlgoIdx);

		return new EncryptedKey(key, salt, algorithm, keyCryptIV, keyCryptAlgo, checksumSize, checksumAlgo);
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

		int fileVersion = din.readInt();
		if (FILE_VERSION != fileVersion)
			throw new IOException("Version not supported! Stream contains a keystore of version \"" + fileVersion + "\" while version \"" + FILE_VERSION + "\" (or lower) is expected!");

		// The checksum is calculated over the complete file, but in order to know which algorithm
		// to use, we first have to read the beginning of the file until including the version.
		// We thus use mark(...) and reset().
		in.reset();

		MessageDigestChecksum checksum = new MessageDigestChecksum.SHA1();
		CheckedInputStream cin = new CheckedInputStream(in, checksum);
		din = new DataInputStream(cin);
		KeyStoreUtil.readByteArrayCompletely(din, new byte[headerSizeIncludingVersion]); // We cannot use skip(...), because that would not update our checksum.

//		nextKeyID = din.readLong();

		int stringConstantSize = din.readInt();
		stringConstant2idMap = new HashMap<String, Integer>(stringConstantSize);
		stringConstantList = new ArrayList<String>(stringConstantSize);
		for (int i = 0; i < stringConstantSize; ++i) {
			String stringConstant = din.readUTF();
			stringConstant(stringConstant);
		}

		int user2keyMapSize = din.readInt();
		user2keyMap = new HashMap<String, EncryptedKey>(user2keyMapSize);
		for (int i = 0; i < user2keyMapSize; ++i) {
			String user = din.readUTF();
			EncryptedKey key = readKey(din);
			user2keyMap.put(user, key);
		}

		int keyID2keyMapSize = din.readInt();
		keyID2keyMap = new HashMap<Long, EncryptedKey>(keyID2keyMapSize);
		for (int i = 0; i < keyID2keyMapSize; ++i) {
			long keyID = din.readLong();
			EncryptedKey key = readKey(din);
			keyID2keyMap.put(keyID, key);
		}

		int name2propertyMapSize = din.readInt();
		name2propertyMap = new HashMap<String, EncryptedProperty>();
		for (int i = 0; i < name2propertyMapSize; ++i) {
			EncryptedProperty encryptedProperty = new EncryptedProperty(din, stringConstantList);
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

		dout.writeInt(FILE_VERSION);
//		dout.writeLong(nextKeyID);

		dout.writeInt(stringConstantList.size());
		for (String stringConstant : stringConstantList) {
			dout.writeUTF(stringConstant);
		}

		dout.writeInt(user2keyMap.size());
		for (Map.Entry<String, EncryptedKey> me : user2keyMap.entrySet()) {
			dout.writeUTF(me.getKey());
			writeKey(dout, me.getValue());
		}

		dout.writeInt(keyID2keyMap.size());
		for (Map.Entry<Long, EncryptedKey> me : keyID2keyMap.entrySet()) {
			dout.writeLong(me.getKey());
			writeKey(dout, me.getValue());
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

	private void writeKey(DataOutputStream dout, EncryptedKey key) throws IOException
	{
		dout.writeInt(key.getData().length);
		dout.write(key.getData());

		dout.writeInt(key.getSalt().length);
		dout.write(key.getSalt());

		Integer idx = stringConstant2idMap.get(key.getAlgorithm());
		dout.writeInt(idx);

		dout.writeInt(key.getEncryptionIV().length);
		dout.write(key.getEncryptionIV());

		idx = stringConstant2idMap.get(key.getEncryptionAlgorithm());
		dout.writeInt(idx);

		dout.writeShort(key.getChecksumSize());

		idx = stringConstant2idMap.get(key.getChecksumAlgorithm());
		dout.writeInt(idx);
	}
}
