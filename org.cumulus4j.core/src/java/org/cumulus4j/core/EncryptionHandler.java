package org.cumulus4j.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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
	// key length: 128 bits
	private static final byte[] dummyKey = { 'D', 'e', 'r', ' ', 'F', 'e', 'r', 'd', ' ', 'h', 'a', 't', ' ', 'v', 'i', 'e' };
	// initialization vector length: 128 bits
	private static final IvParameterSpec iv = new IvParameterSpec(new byte[] {'b', 'l', 'a', 't', 'r', 'u', 'l', 'l', 'a', 'l', 'a', 't', 'r', 'a', 'r', 'a'});

	private static final String ALGORITHM = "AES";
	private static final String ALGORITHM_WITH_PARAMS = ALGORITHM + "/CBC/PKCS5Padding";

	private Cipher encrypter;
	private Cipher decrypter;
	{
		try {
			SecretKeySpec key = new SecretKeySpec(dummyKey, ALGORITHM);
			encrypter = Cipher.getInstance(ALGORITHM_WITH_PARAMS);
			encrypter.init(Cipher.ENCRYPT_MODE, key, iv);
			decrypter = Cipher.getInstance(ALGORITHM_WITH_PARAMS);
			decrypter.init(Cipher.DECRYPT_MODE, key, iv);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	// TODO implement real encryption/decryption with proper key management
	private byte[] dummyEncrypt(byte[] plain)
	{
		if (plain == null)
			return null;

		byte[] result;
		try {
			result = encrypter.doFinal(plain);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		}
		return result;
	}

	// TODO implement real encryption/decryption with proper key management
	private byte[] dummyDecrypt(byte[] encrypted)
	{
		if (encrypted == null)
			return null;

		byte[] result;
		try {
			result = decrypter.doFinal(encrypted);
		} catch (IllegalBlockSizeException e) {
			throw new RuntimeException(e);
		} catch (BadPaddingException e) {
			throw new RuntimeException(e);
		}
		return result;
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
