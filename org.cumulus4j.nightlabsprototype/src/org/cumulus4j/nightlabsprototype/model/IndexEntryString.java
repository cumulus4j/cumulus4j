package org.cumulus4j.nightlabsprototype.model;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.Inheritance;
import javax.jdo.annotations.InheritanceStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Inheritance(strategy=InheritanceStrategy.NEW_TABLE)
// TODO The following "@Unique" does not work at all in Derby and only with special syntax in MySQL
// (note the length "(255)" behind the "indexkeystring"):
//    ALTER TABLE indexentrystring ADD CONSTRAINT UNIQUE KEY (indexkeystring(255), fieldmeta_fieldid_oid)
// => file a bug in DataNucleus' issue tracker (should log a warning, if the underlying DB doesn't support
// it and should use RDBMS-specific syntax, if necessary).
//@Unique(members={"fieldMeta", "indexKeyString"})
@Queries({
	@Query(
			name="getIndexEntryByUniqueKeyFields",
			value="SELECT UNIQUE WHERE " +
					"this.fieldMeta == :fieldMeta && " +
					"this.indexKeyString == :indexKeyString"
	)
})
public class IndexEntryString
extends IndexEntry<String>
{
	/**
	 * Get an {@link IndexEntryString} for the specified unique key fields or <code>null</code>, if no such instance
	 * exists.
	 * @param pm the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param indexKeyString the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntryString} or <code>null</code>.
	 */
	public static IndexEntryString getIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, String indexKeyString)
	{
		javax.jdo.Query q = pm.newNamedQuery(IndexEntryString.class, "getIndexEntryByUniqueKeyFields");
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("fieldMeta", fieldMeta);
		params.put("indexKeyString", indexKeyString);
		return (IndexEntryString) q.executeWithMap(params);
	}

	/**
	 * Get an existing {@link IndexEntryString} just like {@link #getIndexEntry(PersistenceManager, FieldMeta, String)}
	 * or create one, if it does not yet exist.
	 * @param pm the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 * @param fieldMeta the meta-data of the field to query. Must not be <code>null</code>.
	 * @param indexKeyString the indexed value to search for. Might be <code>null</code> (<code>null</code> can be indexed).
	 * @return the matching {@link IndexEntryString} (never <code>null</code>).
	 */
	public static IndexEntryString createIndexEntry(PersistenceManager pm, FieldMeta fieldMeta, String indexKeyString)
	{
		IndexEntryString result = getIndexEntry(pm, fieldMeta, indexKeyString);
		if (result == null)
			result = pm.makePersistent(new IndexEntryString(fieldMeta, indexKeyString));

		return result;
	}

	@Column(jdbcType="CLOB")
	private String indexKeyString;

	protected IndexEntryString() { }

	protected IndexEntryString(FieldMeta fieldMeta, String indexKeyString) {
		super(fieldMeta);
		this.indexKeyString = indexKeyString;
	}

	public String getIndexKeyString() {
		return indexKeyString;
	}

	@Override
	public String getIndexKey() {
		return indexKeyString;
	}
}
