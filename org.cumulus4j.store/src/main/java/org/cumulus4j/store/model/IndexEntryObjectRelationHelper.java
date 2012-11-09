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
package org.cumulus4j.store.model;

import java.util.List;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.crypto.CryptoContext;
import org.datanucleus.store.ExecutionContext;

/**
 * Helper to find an {@link IndexEntry} for an object relation (1-1, 1-n or m-n).
 * Even though {@link DefaultIndexEntryFactory} and {@link IndexEntryLong} are used for such relations, these
 * classes should <b>not</b> be directly accessed in order to make refactorings easier (if this class is used for all
 * object relations, it is possible to search for references of this class).
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class IndexEntryObjectRelationHelper
{
	private static final IndexEntryFactory indexEntryFactoryLong = new DefaultIndexEntryFactory(IndexEntryLong.class);

	public static IndexEntryFactory getIndexEntryFactory() {
		return indexEntryFactoryLong;
	}

	public static List<IndexEntry> getIndexEntriesIncludingSubClasses(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, ClassMeta classMeta, Long indexedDataEntryID)
	{
		ExecutionContext ec = cryptoContext.getExecutionContext();
		Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) ec.getStoreManager();
		List<ClassMeta> classMetaWithSubClassMetas = storeManager.getClassMetaWithSubClassMetas(ec, classMeta);
		return getIndexEntries(cryptoContext, pmIndex, fieldMeta, classMetaWithSubClassMetas, indexedDataEntryID);
	}

	public static List<IndexEntry> getIndexEntries(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, List<ClassMeta> classMetas, Long indexedDataEntryID)
	{
//		List<IndexEntry> result = new ArrayList<IndexEntry>(classMetas.size());
//		for (ClassMeta classMeta : classMetas) {
//			IndexEntry indexEntry = getIndexEntry(cryptoContext, pmIndex, fieldMeta, classMeta, indexedDataEntryID);
//			if (indexEntry != null)
//				result.add(indexEntry);
//		}
//		return result;
		return indexEntryFactoryLong.getIndexEntries(cryptoContext, pmIndex, fieldMeta, classMetas, indexedDataEntryID);
	}

	/**
	 * Get an existing {@link IndexEntry} or <code>null</code>, if it does not exist.
	 * This method looks up an <code>IndexEntry</code> for a relation to the object referenced
	 * by the given <code>indexedDataEntryID</code> and the relation-type specified by the given <code>fieldMeta</code>.
	 * @param cryptoContext the crypto-context.
	 * @param pmIndex the backend-<code>PersistenceManager</code> used to access the index-datastore.
	 * @param fieldMeta the field pointing to the referenced object.
	 * @param classMeta the concrete owner type holding the field (might be a sub-class of {@link FieldMeta#getClassMeta() fieldMeta.classMeta}.
	 * @param indexedDataEntryID the {@link DataEntry#getDataEntryID() DataEntry.dataEntryID} of the referenced object.
	 * @return the appropriate {@link IndexEntry} or <code>null</code>.
	 */
	public static IndexEntry getIndexEntry(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, ClassMeta classMeta, Long indexedDataEntryID)
	{
		return indexEntryFactoryLong.getIndexEntry(cryptoContext, pmIndex, fieldMeta, classMeta, indexedDataEntryID);
	}

	/**
	 * Get an existing {@link IndexEntry} or create it, if it does not yet exist. This method behaves
	 * just like {@link #getIndexEntry(CryptoContext, PersistenceManager, FieldMeta, ClassMeta, Long)}, but instead of returning <code>null</code>,
	 * it creates an <code>IndexEntry</code>, if it does not yet exist.
	 * @param cryptoContext the crypto-context.
	 * @param pmIndex the backend-<code>PersistenceManager</code> used to access the index-datastore.
	 * @param fieldMeta the field pointing to the referenced object.
	 * @param classMeta the concrete owner type holding the field (might be a sub-class of {@link FieldMeta#getClassMeta() fieldMeta.classMeta}.
	 * @param indexedDataEntryID the {@link DataEntry#getDataEntryID() DataEntry.dataEntryID} of the referenced object.
	 * @return the appropriate {@link IndexEntry}; never <code>null</code>.
	 */
	public static IndexEntry createIndexEntry(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, ClassMeta classMeta, Long indexedDataEntryID)
	{
		return indexEntryFactoryLong.createIndexEntry(cryptoContext, pmIndex, fieldMeta, classMeta, indexedDataEntryID);
	}
}
