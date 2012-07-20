package org.cumulus4j.store.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.identity.LongIdentity;

public class DataEntryDAO extends AbstractDAO {

	public DataEntryDAO() { }

	/**
	 * Create a new instance with the backend-<code>PersistenceManager</code> used for data.
	 * @param pmData the backend-<code>PersistenceManager</code>. Must not be <code>null</code>.
	 */
	public DataEntryDAO(PersistenceManager pmData) {
		super(pmData);
	}

	/**
	 * Get the <code>DataEntry</code> identified by the specified {@link #getDataEntryID() dataEntryID} or
	 * <code>null</code> if no such instance exists.
	 * @param dataEntryID the <code>DataEntry</code>'s {@link #getDataEntryID() identifier}.
	 * @return the <code>DataEntry</code> matching the given <code>dataEntryID</code> or <code>null</code>, if no such instance exists.
	 */
	public DataEntry getDataEntry(long dataEntryID)
	{
		DataEntry dataEntry;
		try {
			dataEntry = (DataEntry) pm.getObjectById(new LongIdentity(DataEntry.class, dataEntryID));
		} catch (JDOObjectNotFoundException x) {
			dataEntry = null;
		}
		return dataEntry;
	}

	/**
	 * Get the <code>DataEntry</code> identified by the given type and JDO/JPA-object-ID.
	 *
	 * @param classMeta reference to the searched <code>DataEntry</code>'s {@link #getClassMeta() classMeta} (which must match
	 * the searched instance's concrete type - <b>not</b> the root-type of the inheritance tree!).
	 * @param objectID the <code>String</code>-representation of the JDO/JPA-object-ID.
	 * @return the <code>DataEntry</code> matching the given combination of <code>classMeta</code> and <code>objectID</code>;
	 * or <code>null</code>, if no such instance exists.
	 */
	public DataEntry getDataEntry(ClassMeta classMeta, String objectID)
	{
		javax.jdo.Query q = pm.newNamedQuery(DataEntry.class, "getDataEntryByClassMetaAndObjectID");
		return (DataEntry) q.execute(classMeta, objectID);
		// UNIQUE query does not need to be closed, because there is no result list lingering.
	}

	/**
	 * <p>
	 * Get the {@link #getDataEntryID() dataEntryID} of the <code>DataEntry</code> identified by the
	 * given type and JDO/JPA-object-ID.
	 * </p>
	 * <p>
	 * This method is equivalent to first calling
	 * </p>
	 * <pre>DataEntry e = {@link #getDataEntry(PersistenceManager, ClassMeta, String)}</pre>
	 * <p>
	 * and then
	 * </p>
	 * <pre>e == null ? null : Long.valueOf({@link #getDataEntryID() e.getDataEntryID()})</pre>
	 * <p>
	 * but faster, because it does not query unnecessary data from the underlying database.
	 * </p>
	 *
	 * @param classMeta reference to the searched <code>DataEntry</code>'s {@link #getClassMeta() classMeta} (which must match
	 * the searched instance's concrete type - <b>not</b> the root-type of the inheritance tree!).
	 * @param objectID the <code>String</code>-representation of the JDO/JPA-object-ID.
	 * @return the {@link #getDataEntryID() dataEntryID} of the <code>DataEntry</code> matching the
	 * given combination of <code>classMeta</code> and <code>objectID</code>;
	 * or <code>null</code>, if no such instance exists.
	 */
	public Long getDataEntryID(ClassMeta classMeta, String objectID)
	{
		javax.jdo.Query q = pm.newNamedQuery(DataEntry.class, "getDataEntryIDByClassMetaAndObjectID");
		return (Long) q.execute(classMeta, objectID);
		// UNIQUE query does not need to be closed, because there is no result list lingering.
	}

	/**
	 * <p>
	 * Get the {@link #getDataEntryID() dataEntryID}s of all those <code>DataEntry</code> instances
	 * which do <b>not</b> match the given type and JDO/JPA-object-ID.
	 * </p>
	 * <p>
	 * This method is thus the negation of {@link #getDataEntryID(PersistenceManager, ClassMeta, String)}.
	 * </p>
	 *
	 * @param classMeta reference to the searched <code>DataEntry</code>'s {@link #getClassMeta() classMeta} (which must match
	 * the searched instance's concrete type - <b>not</b> the root-type of the inheritance tree!).
	 * @param notThisObjectID the <code>String</code>-representation of the JDO/JPA-object-ID, which should be
	 * excluded.
	 * @return the {@link #getDataEntryID() dataEntryID}s of those <code>DataEntry</code>s which match the given
	 * <code>classMeta</code> but have an object-ID different from the one specified as <code>notThisObjectID</code>.
	 */
	public Set<Long> getDataEntryIDsNegated(ClassMeta classMeta, String notThisObjectID)
	{
		javax.jdo.Query q = pm.newNamedQuery(DataEntry.class, "getDataEntryIDsByClassMetaAndObjectIDNegated");
		@SuppressWarnings("unchecked")
		Collection<Long> dataEntryIDsColl = (Collection<Long>) q.execute(classMeta, notThisObjectID);
		Set<Long> dataEntryIDsSet = new HashSet<Long>(dataEntryIDsColl);
		q.closeAll();
		return dataEntryIDsSet;
	}
}
