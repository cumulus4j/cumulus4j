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
{
	public EncryptedProperty(
			String name, Class<? extends Property<?>> type,
					byte[] data, byte[] encryptionIV, String encryptionAlgorithm,
					short checksumSize, String checksumAlgorithm
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
		encryptionAlgorithm = din.readUTF();
		checksumSize = din.readShort();
		checksumAlgorithm = din.readUTF();
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
		dout.writeUTF(encryptionAlgorithm);
		dout.writeShort(checksumSize);
		dout.writeUTF(checksumAlgorithm);
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

	private String checksumAlgorithm;

	public String getChecksumAlgorithm() {
		return checksumAlgorithm;
	}

}
