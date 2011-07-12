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

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

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

	/**
	 * Get an {@link IndexEntry} for the specified unique key fields or <code>null</code>, if no such instance
	 * exists.
	 * @param pmIndex the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param indexKey the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntry} or <code>null</code>.
	 */
	public IndexEntry getIndexEntry(PersistenceManager pmIndex, FieldMeta fieldMeta, Object indexKey)
	{
		if (pmIndex == null)
			throw new IllegalArgumentException("pm == null");

		if (fieldMeta == null)
			throw new IllegalArgumentException("fieldMeta == null");

		Class<? extends IndexEntry> indexEntryClass = getIndexEntryClass();
		javax.jdo.Query q = pmIndex.newQuery(indexEntryClass);
		q.setUnique(true);
		q.setFilter(
				"this.fieldMeta == :fieldMeta && " +
				"this.indexKey == :indexKey"
		);
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("fieldMeta", fieldMeta);
		params.put("indexKey", indexKey);
		return indexEntryClass.cast(q.executeWithMap(params));
	}

	/**
	 * Get an existing {@link IndexEntry} just like {@link #getIndexEntry(PersistenceManager, FieldMeta, Object)}
	 * or create one, if it does not yet exist.
	 * @param pmIndex the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param indexKey the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntry} (never <code>null</code>).
	 */
	public IndexEntry createIndexEntry(PersistenceManager pmIndex, FieldMeta fieldMeta, Object indexKey)
	{
		IndexEntry result = getIndexEntry(pmIndex, fieldMeta, indexKey);
		if (result == null) {
			try {
				result = getIndexEntryClass().newInstance();
			} catch (InstantiationException e) {
				throw new RuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
			result.setFieldMeta(fieldMeta);
			result.setIndexKey(indexKey);

			// We persist *after* setting all values, because that improves performance:
			// This way, there is only one INSERT instead of one INSERT AND one UPDATE for each new
			// index entry. The MovieQueryTest.importDataCsv() is around 10% faster when using MySQL
			// (approximately 60 sec vs. 66 sec).
			// Marco :-)
//			result = pm.makePersistent(result);
		}

		return result;
	}
}
