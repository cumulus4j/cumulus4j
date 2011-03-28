package org.cumulus4j.core.model;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public abstract class IndexEntryFactory
{
	public abstract Class<? extends IndexEntry> getIndexEntryClass();

	/**
	 * Get an {@link IndexEntry} for the specified unique key fields or <code>null</code>, if no such instance
	 * exists.
	 * @param pm the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param indexKey the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntry} or <code>null</code>.
	 */
	public IndexEntry getIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, Object indexKey)
	{
		if (pm == null)
			throw new IllegalArgumentException("pm == null");

		if (fieldMeta == null)
			throw new IllegalArgumentException("fieldMeta == null");

		Class<? extends IndexEntry> indexEntryClass = getIndexEntryClass();
		javax.jdo.Query q = pm.newQuery(indexEntryClass);
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
	 * @param pm the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param indexKey the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntry} (never <code>null</code>).
	 */
	public IndexEntry createIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, Object indexKey)
	{
		IndexEntry result = getIndexEntry(pm, fieldMeta, indexKey);
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

			// We persist *after* setting all values, because that improves performance a lot.
			// This way, there is only one INSERT instead of an INSERT AND an UPDATE for each new
			// index entry.
			// MovieQueryTest.importDataCsv takes around 145 sec with persisting here, but only
			// xxx sec when persisting *after* all data is known.
			// Marco :-)
//			result = pm.makePersistent(result);
		}

		return result;
	}
}
