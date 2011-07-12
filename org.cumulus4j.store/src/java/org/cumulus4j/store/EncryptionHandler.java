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
package org.cumulus4j.store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.jdo.PersistenceManagerFactory;

import org.cumulus4j.store.crypto.Ciphertext;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoManagerRegistry;
import org.cumulus4j.store.crypto.CryptoSession;
import org.cumulus4j.store.crypto.Plaintext;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.IndexEntry;
import org.cumulus4j.store.model.IndexValue;
import org.cumulus4j.store.model.ObjectContainer;
import org.datanucleus.store.ExecutionContext;

/**
 * Singleton (per {@link PersistenceManagerFactory}) handling the encryption and decryption and thus the key management.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class EncryptionHandler
{
	public EncryptionHandler() { }

	private CryptoSession getCryptoSession(ExecutionContext ec)
	{
		Object cryptoManagerID = ec.getProperty(CryptoManager.PROPERTY_CRYPTO_MANAGER_ID);
		if (cryptoManagerID == null)
			throw new IllegalStateException("Property \"" + CryptoManager.PROPERTY_CRYPTO_MANAGER_ID + "\" is not set!");

		if (!(cryptoManagerID instanceof String))
			throw new IllegalStateException("Property \"" + CryptoManager.PROPERTY_CRYPTO_MANAGER_ID + "\" is set, but it is an instance of " + cryptoManagerID.getClass().getName() + " instead of java.lang.String!");

		CryptoManager cryptoManager = CryptoManagerRegistry.sharedInstance(ec.getNucleusContext()).getCryptoManager((String) cryptoManagerID);

		Object cryptoSessionID = ec.getProperty(CryptoSession.PROPERTY_CRYPTO_SESSION_ID);
		if (cryptoSessionID == null)
			throw new IllegalStateException("Property \"" + CryptoSession.PROPERTY_CRYPTO_SESSION_ID + "\" is not set!");

		if (!(cryptoSessionID instanceof String))
			throw new IllegalStateException("Property \"" + CryptoSession.PROPERTY_CRYPTO_SESSION_ID + "\" is set, but it is an instance of " + cryptoSessionID.getClass().getName() + " instead of java.lang.String!");

		CryptoSession cryptoSession = cryptoManager.getCryptoSession((String) cryptoSessionID);
		return cryptoSession;
	}

	/**
	 * Get a plain (unencrypted) {@link ObjectContainer} from the encrypted byte-array in
	 * the {@link DataEntry#getValue() DataEntry.value} property.
	 * @param cryptoContext the context.
	 * @param dataEntry the {@link DataEntry} holding the encrypted data (read from).
	 * @return the plain {@link ObjectContainer}.
	 * @see #encryptDataEntry(CryptoContext, DataEntry, ObjectContainer)
	 */
	public ObjectContainer decryptDataEntry(CryptoContext cryptoContext, DataEntry dataEntry)
	{
		Ciphertext ciphertext = new Ciphertext();
		ciphertext.setKeyID(dataEntry.getKeyID());
		ciphertext.setData(dataEntry.getValue());

		if (ciphertext.getData() == null)
			return null; // TODO or return an empty ObjectContainer instead?

		CryptoSession cryptoSession = getCryptoSession(cryptoContext.getExecutionContext());
		Plaintext plaintext = cryptoSession.decrypt(cryptoContext, ciphertext);
		if (plaintext == null)
			throw new IllegalStateException("cryptoSession.decrypt(ciphertext) returned null! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());

		ObjectContainer objectContainer;
		ByteArrayInputStream in = new ByteArrayInputStream(plaintext.getData());
		try {
			ObjectInputStream objIn = new DataNucleusObjectInputStream(in, cryptoContext.getExecutionContext().getClassLoaderResolver());
			objectContainer = (ObjectContainer) objIn.readObject();
			objIn.close();
		} catch (IOException x) {
			throw new RuntimeException(x);
		} catch (ClassNotFoundException x) {
			throw new RuntimeException(x);
		}
		return objectContainer;
	}

	/**
	 * Encrypt the given plain <code>objectContainer</code> and store the cipher-text into the given
	 * <code>dataEntry</code>.
	 * @param cryptoContext the context.
	 * @param dataEntry the {@link DataEntry} that should be holding the encrypted data (written into).
	 * @param objectContainer the plain {@link ObjectContainer} (read from).
	 * @see #decryptDataEntry(CryptoContext, DataEntry)
	 */
	public void encryptDataEntry(CryptoContext cryptoContext, DataEntry dataEntry, ObjectContainer objectContainer)
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

		CryptoSession cryptoSession = getCryptoSession(cryptoContext.getExecutionContext());
		Ciphertext ciphertext = cryptoSession.encrypt(cryptoContext, plaintext);

		if (ciphertext == null)
			throw new IllegalStateException("cryptoSession.encrypt(plaintext) returned null! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());

		if (ciphertext.getKeyID() < 0)
			throw new IllegalStateException("cryptoSession.encrypt(plaintext) returned a ciphertext with keyID < 0! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());

		dataEntry.setKeyID(ciphertext.getKeyID());
		dataEntry.setValue(ciphertext.getData());
	}

	/**
	 * Get a plain (unencrypted) {@link IndexValue} from the encrypted byte-array in
	 * the {@link IndexEntry#getIndexValue() IndexEntry.indexValue} property.
	 * @param cryptoContext the context.
	 * @param indexEntry the {@link IndexEntry} holding the encrypted data (read from).
	 * @return the plain {@link IndexValue}.
	 */
	public IndexValue decryptIndexEntry(CryptoContext cryptoContext, IndexEntry indexEntry)
	{
		Ciphertext ciphertext = new Ciphertext();
		ciphertext.setKeyID(indexEntry.getKeyID());
		ciphertext.setData(indexEntry.getIndexValue());

		Plaintext plaintext = null;
		if (ciphertext.getData() != null) {
			CryptoSession cryptoSession = getCryptoSession(cryptoContext.getExecutionContext());
			plaintext = cryptoSession.decrypt(cryptoContext, ciphertext);
			if (plaintext == null)
				throw new IllegalStateException("cryptoSession.decrypt(ciphertext) returned null! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());
		}

		IndexValue indexValue = new IndexValue(plaintext == null ? null : plaintext.getData());
		return indexValue;
	}

	/**
	 * Encrypt the given plain <code>indexValue</code> and store the cipher-text into the given
	 * <code>indexEntry</code>.
	 * @param cryptoContext the context.
	 * @param indexEntry the {@link IndexEntry} that should be holding the encrypted data (written into).
	 * @param indexValue the plain {@link IndexValue} (read from).
	 */
	public void encryptIndexEntry(CryptoContext cryptoContext, IndexEntry indexEntry, IndexValue indexValue)
	{
		Plaintext plaintext = new Plaintext();
		plaintext.setData(indexValue.toByteArray());

		CryptoSession cryptoSession = getCryptoSession(cryptoContext.getExecutionContext());
		Ciphertext ciphertext = cryptoSession.encrypt(cryptoContext, plaintext);

		if (ciphertext == null)
			throw new IllegalStateException("cryptoSession.encrypt(plaintext) returned null! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());

		if (ciphertext.getKeyID() < 0)
			throw new IllegalStateException("cryptoSession.encrypt(plaintext) returned a ciphertext with keyID < 0! cryptoManagerID=" + cryptoSession.getCryptoManager().getCryptoManagerID() + " cryptoSessionID=" + cryptoSession.getCryptoSessionID());

		indexEntry.setKeyID(ciphertext.getKeyID());
		indexEntry.setIndexValue(ciphertext.getData());
	}
}
