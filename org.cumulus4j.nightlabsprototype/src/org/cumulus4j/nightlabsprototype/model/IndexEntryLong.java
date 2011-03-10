package org.cumulus4j.nightlabsprototype.model;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
@Unique(members={"fieldMeta", "indexKeyLong"})
@Queries({
	@Query(
			name="getIndexEntryByUniqueKeyFields",
			value="SELECT UNIQUE WHERE " +
					"this.fieldMeta == :fieldMeta && " +
					"this.indexKeyLong == :indexKeyLong"
	)
})
public class IndexEntryLong
extends IndexEntry<Long>
{
	/**
	 * Get an {@link IndexEntryLong} for the specified unique key fields or <code>null</code>, if no such instance
	 * exists.
	 * @param pm the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param indexKeyLong the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntryLong} or <code>null</code>.
	 */
	public static IndexEntryLong getIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, Long indexKeyLong)
	{
		javax.jdo.Query q = pm.newNamedQuery(IndexEntryLong.class, "getIndexEntryByUniqueKeyFields");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("fieldMeta", fieldMeta);
		params.put("indexKeyLong", indexKeyLong);
		return (IndexEntryLong) q.executeWithMap(params);
	}

	/**
	 * Get an existing {@link IndexEntryLong} just like {@link #getIndexEntry(PersistenceManager, FieldMeta, Long)}
	 * or create one, if it does not yet exist.
	 * @param pm the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param indexKeyLong the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntryLong} (never <code>null</code>).
	 */
	public static IndexEntryLong createIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, Long indexKeyLong)
	{
		IndexEntryLong result = getIndexEntry(pm, fieldMeta, indexKeyLong);
		if (result == null)
			result = pm.makePersistent(new IndexEntryLong(fieldMeta, indexKeyLong));

		return result;
	}

	private Long indexKeyLong;

	protected IndexEntryLong() { }

	protected IndexEntryLong(FieldMeta fieldMeta, Long indexKeyLong) {
		super(fieldMeta);
		this.indexKeyLong = indexKeyLong;
	}

	public Long getIndexKeyLong() {
		return indexKeyLong;
	}

	@Override
	public Long getIndexKey() {
		return indexKeyLong;
	}
}
