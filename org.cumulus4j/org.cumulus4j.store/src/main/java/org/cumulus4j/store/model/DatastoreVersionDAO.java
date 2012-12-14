package org.cumulus4j.store.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

public class DatastoreVersionDAO extends AbstractDAO {

	public DatastoreVersionDAO() { }

	public DatastoreVersionDAO(PersistenceManager pm) {
		super(pm);
	}

	public Map<String, DatastoreVersion> getCommandID2DatastoreVersionMap(int keyStoreRefID) {
		List<DatastoreVersion> datastoreVersions = getDatastoreVersions(keyStoreRefID);
		Map<String, DatastoreVersion> result = new HashMap<String, DatastoreVersion>(datastoreVersions.size());
		for (DatastoreVersion datastoreVersion : datastoreVersions) {
			result.put(datastoreVersion.getCommandID(), datastoreVersion);
		}
		return result;
	}

	public List<DatastoreVersion> getDatastoreVersions(int keyStoreRefID) {
		Query query = pm.newQuery(DatastoreVersion.class);
		@SuppressWarnings("unchecked")
		Collection<DatastoreVersion> c = (Collection<DatastoreVersion>) query.execute();
		try {
			return new ArrayList<DatastoreVersion>(c);
		} finally {
			query.closeAll();
		}
	}
}
