package org.cumulus4j.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.model.ObjectContainer;
import org.datanucleus.ClassLoaderResolver;

/**
 * Singleton (per {@link PersistenceManagerFactory}) handling the encryption and decryption and thus the key management.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class EncryptionHandler
{
	private static final byte[] dummyKey = { 43, -113, 119, 2 };

	// TODO implement real encryption/decryption
	private static byte[] dummyEncrypt(byte[] plain)
	{
		if (plain == null)
			return null;

		byte[] result = new byte[plain.length];
		int keyIdx = 0;
		for (int i = 0; i < plain.length; i++) {
			result[i] = (byte) (plain[i] ^ dummyKey[keyIdx]);

			if (++keyIdx >= dummyKey.length)
				keyIdx = 0;
		}
		return result;
	}

	// TODO implement real encryption/decryption
	private static byte[] dummyDecrypt(byte[] encrypted)
	{
		// it is symmetric => use dummyEncrypt.
		return dummyEncrypt(encrypted);
	}

	/**
	 * Get a plain (unencrypted) {@link ObjectContainer} from the encrypted byte-array in
	 * the {@link DataEntry#getValue() DataEntry.value} property.
	 * @param dataEntry the {@link DataEntry} holding the encrypted data.
	 * @param classLoaderResolver the {@link ClassLoaderResolver} to use for deserialising the {@link ObjectContainer}.
	 * @return the plain {@link ObjectContainer}
	 */
	public ObjectContainer decryptDataEntry(DataEntry dataEntry, ClassLoaderResolver classLoaderResolver)
	{
		byte[] encrypted = dataEntry.getValue();

		// TODO *real* decryption here!
		byte[] plain = dummyDecrypt(encrypted);

		ObjectContainer objectContainer;
		ByteArrayInputStream in = new ByteArrayInputStream(plain);
		try {
			ObjectInputStream objIn = new DataNucleusObjectInputStream(in, classLoaderResolver);
			objectContainer = (ObjectContainer) objIn.readObject();
			objIn.close();
		} catch (IOException x) {
			throw new RuntimeException(x);
		} catch (ClassNotFoundException x) {
			throw new RuntimeException(x);
		}
		return objectContainer;
	}

	public void encryptDataEntry(DataEntry dataEntry, ObjectContainer objectContainer)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			objOut.writeObject(objectContainer);
			objOut.close();
		} catch (IOException x) {
			throw new RuntimeException(x);
		}
		byte[] plain = out.toByteArray(); out = null;

		// TODO *real* encryption here!
		byte[] encrypted = dummyEncrypt(plain);

		dataEntry.setValue(encrypted);
	}

	public IndexValue decryptIndexEntry(IndexEntry indexEntry)
	{
		byte[] encrypted = indexEntry.getIndexValue();

		// TODO *real* decryption here!
		byte[] plain = dummyDecrypt(encrypted);

		IndexValue indexValue = new IndexValue(plain);
		return indexValue;
	}

	public void encryptIndexEntry(IndexEntry indexEntry, IndexValue indexValue)
	{
		byte[] plain = indexValue.toByteArray();

		// TODO *real* encryption here!
		byte[] encrypted = dummyEncrypt(plain);

		indexEntry.setIndexValue(encrypted);
	}
}
