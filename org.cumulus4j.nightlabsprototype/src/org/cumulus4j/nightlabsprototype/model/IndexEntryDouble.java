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
@Unique(members={"fieldMeta", "indexKeyDouble"})
@Queries({
	@Query(
			name="getIndexEntryByUniqueKeyFields",
			value="SELECT UNIQUE WHERE " +
					"this.fieldMeta == :fieldMeta && " +
					"this.indexKeyDouble == :indexKeyDouble"
	)
})
public class IndexEntryDouble
extends IndexEntry
{
	/**
	 * Get an {@link IndexEntryDouble} for the specified unique key fields or <code>null</code>, if no such instance
	 * exists.
	 * @param pm the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param indexKeyDouble the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntryDouble} or <code>null</code>.
	 */
	public static IndexEntryDouble getIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, Double indexKeyDouble)
	{
		javax.jdo.Query q = pm.newNamedQuery(IndexEntryDouble.class, "getIndexEntryByUniqueKeyFields");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("fieldMeta", fieldMeta);
		params.put("indexKeyDouble", indexKeyDouble);
		return (IndexEntryDouble) q.executeWithMap(params);
	}

	/**
	 * Get an existing {@link IndexEntryDouble} just like {@link #getIndexEntry(PersistenceManager, FieldMeta, Double)}
	 * or create one, if it does not yet exist.
	 * @param pm the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param indexKeyDouble the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntryDouble} (never <code>null</code>).
	 */
	public static IndexEntryDouble createIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, Double indexKeyDouble)
	{
		IndexEntryDouble result = getIndexEntry(pm, fieldMeta, indexKeyDouble);
		if (result == null)
			result = pm.makePersistent(new IndexEntryDouble(fieldMeta, indexKeyDouble));

		return result;
	}

	private Double indexKeyDouble;

	protected IndexEntryDouble() { }

	protected IndexEntryDouble(FieldMeta fieldMeta, Double indexKeyDouble) {
		super(fieldMeta);
		this.indexKeyDouble = indexKeyDouble;
	}

	public Double getIndexKeyDouble() {
		return indexKeyDouble;
	}

	@Override
	public Double getIndexKey() {
		return indexKeyDouble;
	}
}
