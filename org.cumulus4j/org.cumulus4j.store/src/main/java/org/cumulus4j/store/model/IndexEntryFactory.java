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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.EncryptionHandler;
import org.cumulus4j.store.crypto.CryptoContext;
import org.datanucleus.store.ExecutionContext;

/**
 * <p>
 * Factory for creating (or looking up) specific {@link IndexEntry} implementations.
 * </p><p>
 * It is optional to implement a specific factory. For most use cases, it is sufficient to
 * use the {@link DefaultIndexEntryFactory} (which is used, if the extension specifies the
 * attribute <code>index-entry-type</code>), but you can alternatively specify a custom
 * factory via the extension-attribute <code>index-entry-factory-type</code>.
 * </p><p>
 * If you specify a custom
 * factory, you must omit (or leave empty) the <code>index-entry-type</code>!
 * </p>
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class IndexEntryFactory
{
	/**
	 * Get the concrete implementation class (sub-class) of {@link IndexEntry} managed by this factory.
	 * @return the concrete implementation class of {@link IndexEntry} managed by this factory.
	 */
	public abstract Class<? extends IndexEntry> getIndexEntryClass();

	public List<IndexEntry> getIndexEntriesIncludingSubClasses(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, ClassMeta classMeta, Object indexKey)
	{
		ExecutionContext ec = cryptoContext.getExecutionContext();
		Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) ec.getStoreManager();
		List<ClassMeta> classMetaWithSubClassMetas = storeManager.getClassMetaWithSubClassMetas(ec, classMeta);
		return getIndexEntries(cryptoContext, pmIndex, fieldMeta, classMetaWithSubClassMetas, indexKey);
	}

	public List<IndexEntry> getIndexEntries(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, List<ClassMeta> classMetas, Object indexKey)
	{
		if (pmIndex == null)
			throw new IllegalArgumentException("pm == null");

		if (fieldMeta == null)
			throw new IllegalArgumentException("fieldMeta == null");

		if (classMetas == null)
			throw new IllegalArgumentException("classMetas == null");

		if (classMetas.isEmpty()) {
			throw new IllegalArgumentException("classMetas is empty"); // hmmm... I think this should never happen.
//			return Collections.emptyList();
		}

		if (classMetas.size() == 1) {
			IndexEntry indexEntry = getIndexEntry(cryptoContext, pmIndex, fieldMeta, classMetas.get(0), indexKey);
			if (indexEntry == null)
				return Collections.emptyList();
			else
				return Collections.singletonList(indexEntry);
		}
//		List<IndexEntry> result = new ArrayList<IndexEntry>(classMetas.size());
//		for (ClassMeta classMeta : classMetas) {
//			IndexEntry indexEntry = getIndexEntry(cryptoContext, pmIndex, fieldMeta, classMeta, indexKey);
//			if (indexEntry != null)
//				result.add(indexEntry);
//		}
//		return result;

		Class<? extends IndexEntry> indexEntryClass = getIndexEntryClass();
		javax.jdo.Query q = pmIndex.newQuery(indexEntryClass);
		Map<String, Object> params = new HashMap<String, Object>();
		q.setFilter(
				"this.keyStoreRefID == :keyStoreRefID && " +
				"this.fieldMeta_fieldID == :fieldMeta_fieldID && " +
//				":classMetas.contains(this.classMeta) && " +
				ClassMetaDAO.getMultiClassMetaOrFilterPart(params, classMetas) + " && " +
				"this.indexKey == :indexKey"
		);
		params.put("keyStoreRefID", cryptoContext.getKeyStoreRefID());
		params.put("fieldMeta_fieldID", fieldMeta.getFieldID());
//		params.put("classMetas", classMetas);
		params.put("indexKey", indexKey);
		@SuppressWarnings("unchecked")
		List<IndexEntry> result = (List<IndexEntry>) q.executeWithMap(params);
		result = Collections.unmodifiableList(new ArrayList<IndexEntry>(result)); // consistent with emptyList + singletonList above (both read-only)
		q.closeAll();
		return result;
	}

	/**
	 * Get an {@link IndexEntry} for the specified unique key fields or <code>null</code>, if no such instance
	 * exists.
	 * @param cryptoContext the crypto-context.
	 * @param pmIndex the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param classMeta TODO
	 * @param indexKey the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntry} or <code>null</code>.
	 */
	public IndexEntry getIndexEntry(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, ClassMeta classMeta, Object indexKey)
	{
		if (pmIndex == null)
			throw new IllegalArgumentException("pm == null");

		if (fieldMeta == null)
			throw new IllegalArgumentException("fieldMeta == null");

		if (classMeta == null)
			throw new IllegalArgumentException("classMeta == null");

		Class<? extends IndexEntry> indexEntryClass = getIndexEntryClass();
		javax.jdo.Query q = pmIndex.newQuery(indexEntryClass);
		q.setUnique(true);
		q.setFilter(
				"this.keyStoreRefID == :keyStoreRefID && " +
				"this.fieldMeta_fieldID == :fieldMeta_fieldID && " +
				"this.classMeta_classID == :classMeta_classID && " +
				"this.indexKey == :indexKey"
		);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("keyStoreRefID", cryptoContext.getKeyStoreRefID());
		params.put("fieldMeta_fieldID", fieldMeta.getFieldID());
		params.put("classMeta_classID", classMeta.getClassID());
		params.put("indexKey", indexKey);
		return indexEntryClass.cast(q.executeWithMap(params));
	}

	/**
	 * Get an existing {@link IndexEntry} just like {@link #getIndexEntry(CryptoContext, PersistenceManager, FieldMeta, ClassMeta, Object)}
	 * or create one, if it does not yet exist.
	 * @param cryptoContext TODO
	 * @param pmIndex the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param classMeta TODO
	 * @param indexKey the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntry} (never <code>null</code>).
	 */
	public IndexEntry createIndexEntry(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, ClassMeta classMeta, Object indexKey)
	{
		IndexEntry result = getIndexEntry(cryptoContext, pmIndex, fieldMeta, classMeta, indexKey);
		if (result == null) {
			try {
				result = getIndexEntryClass().newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			result.setFieldMeta(fieldMeta);
			result.setClassMeta(classMeta);
			result.setKeyStoreRefID(cryptoContext.getKeyStoreRefID());
			result.setIndexKey(indexKey);

			// We persist *after* setting all values, because that improves performance:
			// This way, there is only one INSERT instead of one INSERT AND one UPDATE for each new
			// index entry. The MovieQueryTest.importDataCsv() is around 10% faster when using MySQL
			// (approximately 60 sec vs. 66 sec).
			// However, when dumping the plaintexts for debugging, we need the indexEntryID already *before*
			// encryption. Hence, we persist here, if the DEBUG_DUMP flag is set.
			// Marco :-)
			if (EncryptionHandler.DEBUG_DUMP)
				result = pmIndex.makePersistent(result);
		}

		return result;
	}
}
