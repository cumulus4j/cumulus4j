package org.cumulus4j.keystore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
abstract class AbstractEncryptedKey
{
	public AbstractEncryptedKey() { }

	public AbstractEncryptedKey(
			byte[] data, String algorithm, byte[] encryptionIV, String encryptionAlgorithm, short checksumSize, String checksumAlgorithm
	)
	{
		if (data == null)
			throw new IllegalArgumentException("data must not be null!");

		if (algorithm == null)
			throw new IllegalArgumentException("algorithm must not be null!");

		if (encryptionAlgorithm == null)
			throw new IllegalArgumentException("encryptionAlgorithm must not be null!");

		if (checksumSize < 1)
			throw new IllegalArgumentException("checksumSize < 1");

		if (checksumAlgorithm == null || checksumAlgorithm.isEmpty())
			throw new IllegalArgumentException("checksumAlgorithm must not be null and not be empty!");

		this.data = data;
		this.algorithm = algorithm;
		this.encryptionIV = encryptionIV;
		this.encryptionAlgorithm = encryptionAlgorithm;
		this.checksumSize = checksumSize;
		this.checksumAlgorithm = checksumAlgorithm;
	}

	private byte[] data;

	public byte[] getData() {
		return data;
	}

	private String algorithm;

	public String getAlgorithm() {
		return algorithm;
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

	private String checksumAlgorithm;

	public String getChecksumAlgorithm() {
		return checksumAlgorithm;
	}

	public void read(DataInputStream din, ArrayList<String> stringConstantList) throws IOException
	{
		data = KeyStoreUtil.readByteArrayWithLengthHeader(din);

		int algorithmIdx = din.readInt();
		algorithm = stringConstantList.get(algorithmIdx);

		encryptionIV = KeyStoreUtil.readByteArrayWithLengthHeader(din);

		int idx = din.readInt();
		encryptionAlgorithm = stringConstantList.get(idx);

		checksumSize = din.readShort();

		idx = din.readInt();
		checksumAlgorithm = stringConstantList.get(idx);
	}

	public void write(DataOutputStream out, Map<String, Integer> stringConstant2idMap) throws IOException
	{
		KeyStoreUtil.writeByteArrayWithLengthHeader(out, data);

		Integer idx = stringConstant2idMap.get(algorithm);
		out.writeInt(idx);

		KeyStoreUtil.writeByteArrayWithLengthHeader(out, encryptionIV);

		idx = stringConstant2idMap.get(encryptionAlgorithm);
		out.writeInt(idx);

		out.writeShort(checksumSize);

		idx = stringConstant2idMap.get(checksumAlgorithm);
		out.writeInt(idx);
	}
}
