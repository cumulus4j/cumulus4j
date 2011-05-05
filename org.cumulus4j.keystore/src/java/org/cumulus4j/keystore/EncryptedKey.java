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
extends AbstractEncryptedKey
{
	private long keyID;

	public EncryptedKey(
			long keyID,
			byte[] data, String algorithm, byte[] encryptionIV, String encryptionAlgorithm, short checksumSize, String checksumAlgorithm
	)
	{
		super(data, algorithm, encryptionIV, encryptionAlgorithm, checksumSize, checksumAlgorithm);
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
}
