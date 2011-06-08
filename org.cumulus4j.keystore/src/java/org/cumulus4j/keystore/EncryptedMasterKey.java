package org.cumulus4j.keystore;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import org.cumulus4j.crypto.util.ChecksumAlgorithm;

class EncryptedMasterKey
extends AbstractEncryptedKey
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
			String userName, int passwordBasedKeySize, int passwordBasedInterationCount, String passwordBasedKeyGeneratorAlgorithm,
			byte[] data, byte[] salt, byte[] encryptionIV, String encryptionAlgorithm, short checksumSize, ChecksumAlgorithm checksumAlgorithm
	) {
		super(data, encryptionIV, encryptionAlgorithm, checksumSize, checksumAlgorithm);

		if (userName == null)
			throw new IllegalArgumentException("userName == null");

		if (salt == null)
			throw new IllegalArgumentException("salt == null");

		this.userName = userName;
		this.passwordBasedKeySize = passwordBasedKeySize;
		this.passwordBasedIterationCount = passwordBasedInterationCount;
		this.passwordBasedKeyGeneratorAlgorithm = passwordBasedKeyGeneratorAlgorithm;
		this.salt = salt;
	}

	public EncryptedMasterKey(DataInputStream in, ArrayList<String> stringConstantList) throws IOException
	{
		userName = in.readUTF();
		passwordBasedKeySize = in.readInt();
		passwordBasedIterationCount = in.readInt();

		int idx = in.readInt();
		passwordBasedKeyGeneratorAlgorithm = stringConstantList.get(idx);
		if (passwordBasedKeyGeneratorAlgorithm == null)
			throw new IllegalStateException("stringConstantList.get(idx) returned null for idx=" + idx);

		salt = KeyStoreUtil.readByteArrayWithLengthHeader(in);
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

		KeyStoreUtil.writeByteArrayWithLengthHeader(out, salt);
		super.write(out, stringConstant2idMap);
	}
}
