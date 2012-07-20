package org.cumulus4j.store.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;
import javax.jdo.identity.StringIdentity;

public class DatastoreVersionDAO extends AbstractDAO {

	public DatastoreVersionDAO() { }

	public DatastoreVersionDAO(PersistenceManager pm) {
		super(pm);
	}

	public Map<String, DatastoreVersion> getDatastoreVersionID2DatastoreVersionMap() {
		List<DatastoreVersion> datastoreVersions = getDatastoreVersions();
		Map<String, DatastoreVersion> result = new HashMap<String, DatastoreVersion>(datastoreVersions.size());
		for (DatastoreVersion datastoreVersion : datastoreVersions) {
			result.put(datastoreVersion.getDatastoreVersionID(), datastoreVersion);
		}
		return result;
	}

	public List<DatastoreVersion> getDatastoreVersions() {
		Query query = pm.newQuery(DatastoreVersion.class);
		@SuppressWarnings("unchecked")
		Collection<DatastoreVersion> c = (Collection<DatastoreVersion>) query.execute();
		try {
			return new ArrayList<DatastoreVersion>(c);
		} finally {
			query.closeAll();
		}
	}

	public DatastoreVersion getDatastoreVersion(String datastoreVersionID)
	{
		StringIdentity id = new StringIdentity(DatastoreVersion.class, datastoreVersionID);
		DatastoreVersion datastoreVersion;
		try {
			datastoreVersion = (DatastoreVersion) pm.getObjectById(id);
		} catch (JDOObjectNotFoundException x) {
			datastoreVersion = null;
		}
		return datastoreVersion;
	}

//	public DatastoreVersion createDatastoreVersion(String datastoreVersionID)
//	{
//		DatastoreVersion sequence = getDatastoreVersion(datastoreVersionID);
//		if (sequence == null)
//			sequence = pm.makePersistent(new DatastoreVersion(datastoreVersionID));
//
//		return sequence;
//	}
}
