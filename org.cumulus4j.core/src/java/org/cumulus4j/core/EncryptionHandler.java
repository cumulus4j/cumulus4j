package org.cumulus4j.core;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.api.crypto.Ciphertext;
import org.cumulus4j.api.crypto.CryptoManager;
import org.cumulus4j.api.crypto.CryptoManagerRegistry;
import org.cumulus4j.api.crypto.CryptoSession;
import org.cumulus4j.api.crypto.Plaintext;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.model.ObjectContainer;
import org.datanucleus.store.ExecutionContext;

/**
 * Singleton (per {@link PersistenceManagerFactory}) handling the encryption and decryption and thus the key management.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class EncryptionHandler
{
	private CryptoSession getCryptoSession(ExecutionContext executionContext)
	{
		Object cryptoManagerID = executionContext.getProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID);
		if (cryptoManagerID == null)
			throw new IllegalStateException("Property \"" + CryptoManager.PROPERTY_CRYPTO_MANAGER_ID + "\" is not set!");

		if (!(cryptoManagerID instanceof String))
			throw new IllegalStateException("Property \"" + CryptoManager.PROPERTY_CRYPTO_MANAGER_ID + "\" is set, but it is an instance of " + cryptoManagerID.getClass().getName() + " instead of java.lang.String!");

		CryptoManager cryptoManager = CryptoManagerRegistry.sharedInstance(executionContext.getNucleusContext()).getCryptoManager((String) cryptoManagerID);

		Object cryptoSessionID = executionContext.getProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID);
		if (cryptoSessionID == null)
			throw new IllegalStateException("Property \"" + CryptoSession.PROPERTY_CRYPTO_SESSION_ID + "\" is not set!");

		if (!(cryptoSessionID instanceof String))
			throw new IllegalStateException("Property \"" + CryptoSession.PROPERTY_CRYPTO_SESSION_ID + "\" is set, but it is an instance of " + cryptoSessionID.getClass().getName() + " instead of java.lang.String!");

		CryptoSession cryptoSession = cryptoManager.getCryptoSession((String) cryptoSessionID);
		return cryptoSession;
	}

//	private byte[] encrypt(ExecutionContext executionContext, long keyID, byte[] plain)
//	{
//		if (plain == null)
//			return null;
//
//		CryptoSession cryptoSession = getKeyManagerSession(executionContext);
//		Cipher encrypter = cryptoSession.getEncrypter(keyID);
//		if (encrypter == null)
//			throw new IllegalStateException("keyManagerSession.getEncrypter(keyID) returned null! keyManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " keyManagerSessionID=" + cryptoSession.getCryptoSessionID() + " keyID=" + keyID);
//
//		synchronized (encrypter) {
//			byte[] result;
//			try {
//				result = encrypter.doFinal(plain);
//			} catch (IllegalBlockSizeException e) {
//				throw new RuntimeException(e);
//			} catch (BadPaddingException e) {
//				throw new RuntimeException(e);
//			}
//			return result;
//		}
//	}
//
//	private byte[] decrypt(ExecutionContext executionContext, long keyID, byte[] encrypted)
//	{
//		if (encrypted == null)
//			return null;
//
//		CryptoSession cryptoSession = getKeyManagerSession(executionContext);
//		Cipher decrypter = cryptoSession.getDecrypter(keyID);
//		if (decrypter == null)
//			throw new IllegalStateException("keyManagerSession.getDecrypter(keyID) returned null! keyManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " keyManagerSessionID=" + cryptoSession.getCryptoSessionID() + " keyID=" + keyID);
//
//		synchronized (decrypter) {
//			byte[] result;
//			try {
//				result = decrypter.doFinal(encrypted);
//			} catch (IllegalBlockSizeException e) {
//				throw new RuntimeException(e);
//			} catch (BadPaddingException e) {
//				throw new RuntimeException(e);
//			}
//			return result;
//		}
//	}

	/**
	 * Get a plain (unencrypted) {@link ObjectContainer} from the encrypted byte-array in
	 * the {@link DataEntry#getValue() DataEntry.value} property.
	 * @param executionContext the context.
	 * @param dataEntry the {@link DataEntry} holding the encrypted data.
	 * @return the plain {@link ObjectContainer}
	 */
	public ObjectContainer decryptDataEntry(ExecutionContext executionContext, DataEntry dataEntry)
	{
		Ciphertext ciphertext = new Ciphertext();
		ciphertext.setKeyID(dataEntry.getKeyID());
		ciphertext.setData(dataEntry.getValue());

		if (ciphertext.getData() == null)
			return null; // TODO or return an empty ObjectContainer instead?

		CryptoSession cryptoSession = getCryptoSession(executionContext);
		Plaintext plaintext = cryptoSession.decrypt(ciphertext);
		if (plaintext == null)
			throw new IllegalStateException("cryptoSession.decrypt(ciphertext) returned null! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());

		ObjectContainer objectContainer;
		ByteArrayInputStream in = new ByteArrayInputStream(plaintext.getData());
		try {
			ObjectInputStream objIn = new DataNucleusObjectInputStream(in, executionContext.getClassLoaderResolver());
			objectContainer = (ObjectContainer) objIn.readObject();
			objIn.close();
		} catch (IOException x) {
			throw new RuntimeException(x);
		} catch (ClassNotFoundException x) {
			throw new RuntimeException(x);
		}
		return objectContainer;
	}

	public void encryptDataEntry(ExecutionContext executionContext, DataEntry dataEntry, ObjectContainer objectContainer)
	{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			ObjectOutputStream objOut = new ObjectOutputStream(out);
			objOut.writeObject(objectContainer);
			objOut.close();
		} catch (IOException x) {
			throw new RuntimeException(x);
		}

		Plaintext plaintext = new Plaintext();
		plaintext.setData(out.toByteArray()); out = null;

		CryptoSession cryptoSession = getCryptoSession(executionContext);
		Ciphertext ciphertext = cryptoSession.encrypt(plaintext);

		if (ciphertext == null)
			throw new IllegalStateException("cryptoSession.encrypt(plaintext) returned null! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());

		if (ciphertext.getKeyID() < 0)
			throw new IllegalStateException("cryptoSession.encrypt(plaintext) returned a ciphertext with keyID < 0! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());

		dataEntry.setKeyID(ciphertext.getKeyID());
		dataEntry.setValue(ciphertext.getData());
	}

	public IndexValue decryptIndexEntry(ExecutionContext executionContext, IndexEntry indexEntry)
	{
		Ciphertext ciphertext = new Ciphertext();
		ciphertext.setKeyID(indexEntry.getKeyID());
		ciphertext.setData(indexEntry.getIndexValue());

		Plaintext plaintext = null;
		if (ciphertext.getData() != null) {
			CryptoSession cryptoSession = getCryptoSession(executionContext);
			plaintext = cryptoSession.decrypt(ciphertext);
			if (plaintext == null)
				throw new IllegalStateException("cryptoSession.decrypt(ciphertext) returned null! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());
		}

		IndexValue indexValue = new IndexValue(plaintext == null ? null : plaintext.getData());
		return indexValue;
	}

	public void encryptIndexEntry(ExecutionContext executionContext, IndexEntry indexEntry, IndexValue indexValue)
	{
		Plaintext plaintext = new Plaintext();
		plaintext.setData(indexValue.toByteArray());

		CryptoSession cryptoSession = getCryptoSession(executionContext);
		Ciphertext ciphertext = cryptoSession.encrypt(plaintext);

		if (ciphertext == null)
			throw new IllegalStateException("cryptoSession.encrypt(plaintext) returned null! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());

		if (ciphertext.getKeyID() < 0)
			throw new IllegalStateException("cryptoSession.encrypt(plaintext) returned a ciphertext with keyID < 0! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());

		indexEntry.setKeyID(ciphertext.getKeyID());
		indexEntry.setIndexValue(ciphertext.getData());
	}
}
