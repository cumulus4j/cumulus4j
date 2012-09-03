package org.cumulus4j.store.datastoreversion.command;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.store.Cumulus4jPersistenceHandler;
import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.EncryptionHandler;
import org.cumulus4j.store.IndexEntryAction;
import org.cumulus4j.store.ProgressInfo;
import org.cumulus4j.store.WorkInProgressException;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.datastoreversion.AbstractDatastoreVersionCommand;
import org.cumulus4j.store.datastoreversion.CommandApplyParam;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.IndexEntry;
import org.cumulus4j.store.model.ObjectContainer;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Delete all {@link IndexEntry}s from the datastore and then iterate all
 * {@link DataEntry}s and re-index them.
 * <p>
 * TODO This class currently does not yet ensure that different keys are used. Thus,
 * very likely the entire index uses only one single key. This should be improved
 * in a future version.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class RecreateIndex extends AbstractDatastoreVersionCommand
{
	private static final Logger logger = LoggerFactory.getLogger(RecreateIndex.class);

	@Override
	public int getCommandVersion() {
		return 1;
	}

	@Override
	public boolean isFinal() {
		return false;
	}

	@Override
	public boolean isKeyStoreDependent() {
		return true;
	}

	private CommandApplyParam commandApplyParam;
	private Properties workInProgressStateProperties;
	private CryptoContext cryptoContext;
	private PersistenceManager pmIndex;
	private PersistenceManager pmData;
	private long keyStoreRefID;

	private Set<Class<? extends IndexEntry>> indexEntryClasses;

	@Override
	public void apply(CommandApplyParam commandApplyParam) {
		// The index only exists in the index-datastore (not in the index-datastore), hence we return immediately, if the
		// current datastore is not the index-datastore.
		this.commandApplyParam = commandApplyParam;
		PersistenceManager pm = commandApplyParam.getPersistenceManager();
		cryptoContext = commandApplyParam.getCryptoContext();
		if (pm != cryptoContext.getPersistenceManagerForIndex())
			return;

		keyStoreRefID = cryptoContext.getKeyStoreRefID();
		pmIndex = commandApplyParam.getCryptoContext().getPersistenceManagerForIndex();
		pmData = commandApplyParam.getCryptoContext().getPersistenceManagerForData();
		workInProgressStateProperties = commandApplyParam.getWorkInProgressStateProperties();

		deleteIndex();
		createIndex();
	}

	protected static final String PROPERTY_DELETE_COMPLETE = "delete.complete";
	protected static final String PROPERTY_DELETE_FROM_INDEX_ENTRY_ID = "delete.fromIndexEntryID";
	protected static final String PROPERTY_CREATE_FROM_DATA_ENTRY_ID = "create.fromDataEntryID";

	protected void deleteIndex() {
		logger.debug("deleteIndex: Entered.");
		if (Boolean.parseBoolean(workInProgressStateProperties.getProperty(PROPERTY_DELETE_COMPLETE))) {
			logger.debug("deleteIndex: PROPERTY_DELETE_COMPLETE == true => quit.");
			return;
		}

		final long indexEntryBlockSize = 100;
		Long maxIndexEntryIDObj = getMaxIndexEntryID();
		if (maxIndexEntryIDObj == null) {
			logger.debug("deleteIndex: There are no IndexEntry instances in the database => quit.");
		}
		else {
			final long maxIndexEntryID = maxIndexEntryIDObj;
			String fromIndexEntryStr = workInProgressStateProperties.getProperty(PROPERTY_DELETE_FROM_INDEX_ENTRY_ID);
			long fromIndexEntryID;
			if (fromIndexEntryStr != null) {
				logger.info("deleteIndex: previous incomplete run found: fromIndexEntryStr={}", fromIndexEntryStr);
				fromIndexEntryID = Long.parseLong(fromIndexEntryStr);
			}
			else {
				final long minIndexEntryID = getMinIndexEntryID();
				logger.info("deleteIndex: first run: minIndexEntryID={} maxIndexEntryID={}", minIndexEntryID, maxIndexEntryID);
				fromIndexEntryID = minIndexEntryID;
			}
			while (fromIndexEntryID <= maxIndexEntryID - indexEntryBlockSize) {
				long toIndexEntryIDExcl = fromIndexEntryID + indexEntryBlockSize;
				deleteIndexForRange(fromIndexEntryID, toIndexEntryIDExcl);
				fromIndexEntryID = toIndexEntryIDExcl;
				if (commandApplyParam.isDatastoreVersionCommandApplyWorkInProgressTimeoutExceeded()) {
					workInProgressStateProperties.setProperty(PROPERTY_DELETE_FROM_INDEX_ENTRY_ID, Long.toString(fromIndexEntryID));
					throw new WorkInProgressException(new ProgressInfo());
				}
			}
			deleteIndexForRange(fromIndexEntryID, null);
		}

		workInProgressStateProperties.setProperty(PROPERTY_DELETE_COMPLETE, Boolean.TRUE.toString());
		logger.debug("deleteIndex: Leaving.");
	}

	protected void deleteIndexForRange(long fromIndexEntryIDIncl, Long toIndexEntryIDExcl) {
		logger.info("deleteIndexForRange: Entered. fromIndexEntryIDIncl={} toIndexEntryIDExcl={}", fromIndexEntryIDIncl, toIndexEntryIDExcl);
		List<IndexEntry> indexEntries = getIndexEntries(fromIndexEntryIDIncl, toIndexEntryIDExcl);
		pmIndex.deletePersistentAll(indexEntries);
		logger.info("deleteIndexForRange: Leaving. fromIndexEntryIDIncl={} toIndexEntryIDExcl={}", fromIndexEntryIDIncl, toIndexEntryIDExcl);
	}

	protected List<IndexEntry> getIndexEntries(long fromIndexEntryIDIncl, Long toIndexEntryIDExcl) {
		List<IndexEntry> result = new ArrayList<IndexEntry>();
		for (Class<? extends IndexEntry> indexEntryClass : getIndexEntryClasses()) {
			result.addAll(getIndexEntries(indexEntryClass, fromIndexEntryIDIncl, toIndexEntryIDExcl));
		}
		return result;
	}

	protected List<IndexEntry> getIndexEntries(Class<? extends IndexEntry> indexEntryClass, long fromIndexEntryIDIncl, Long toIndexEntryIDExcl) {
		Query q = pmIndex.newQuery(indexEntryClass);
		StringBuilder filter = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>(2);

		filter.append("this.keyStoreRefID == :keyStoreRefID");
		params.put("keyStoreRefID", keyStoreRefID);

		filter.append(" && this.indexEntryID >= :fromIndexEntryIDIncl");
		params.put("fromIndexEntryIDIncl", fromIndexEntryIDIncl);

		if (toIndexEntryIDExcl != null) {
			filter.append(" && this.indexEntryID < :toIndexEntryIDExcl");
			params.put("toIndexEntryIDExcl", toIndexEntryIDExcl);
		}
		q.setFilter(filter.toString());
		q.setOrdering("this.indexEntryID ASC");

		@SuppressWarnings("unchecked")
		List<IndexEntry> result = new ArrayList<IndexEntry>((List<IndexEntry>) q.executeWithMap(params));
		q.closeAll();
		return result;
	}

	protected void createIndex()
	{
		final long dataEntryBlockSize = 100;
		long fromDataEntryID;
		String fromDataEntryIDStr = workInProgressStateProperties.getProperty(PROPERTY_CREATE_FROM_DATA_ENTRY_ID);
		final long maxDataEntryID = getMaxDataEntryID();
		if (fromDataEntryIDStr != null) {
			fromDataEntryID = Long.parseLong(fromDataEntryIDStr);
		}
		else {
			final long minDataEntryID = getMinDataEntryID();
			fromDataEntryID = minDataEntryID;
		}
		while (fromDataEntryID <= maxDataEntryID - dataEntryBlockSize) {
			long toDataEntryIDExcl = fromDataEntryID + dataEntryBlockSize;
			createIndexForRange(fromDataEntryID, toDataEntryIDExcl);
			fromDataEntryID = toDataEntryIDExcl;

			if (commandApplyParam.isDatastoreVersionCommandApplyWorkInProgressTimeoutExceeded()) {
				workInProgressStateProperties.setProperty(PROPERTY_CREATE_FROM_DATA_ENTRY_ID, Long.toString(fromDataEntryID));
				throw new WorkInProgressException(new ProgressInfo());
			}
		}
		createIndexForRange(fromDataEntryID, null);
	}

	protected void createIndexForRange(long fromDataEntryIDIncl, Long toDataEntryIDExcl) {
		ExecutionContext ec = cryptoContext.getExecutionContext();
		Cumulus4jStoreManager storeManager = commandApplyParam.getStoreManager();
		EncryptionHandler encryptionHandler = storeManager.getEncryptionHandler();
		Cumulus4jPersistenceHandler persistenceHandler = storeManager.getPersistenceHandler();
		IndexEntryAction addIndexEntryAction = persistenceHandler.getAddIndexEntryAction();
		List<DataEntryWithClassName> l = getDataEntries(fromDataEntryIDIncl, toDataEntryIDExcl);
		for (DataEntryWithClassName dataEntryWithClassName : l) {
			long dataEntryID = dataEntryWithClassName.getDataEntry().getDataEntryID();
			ObjectContainer objectContainer = encryptionHandler.decryptDataEntry(cryptoContext, dataEntryWithClassName.getDataEntry());
			for (Map.Entry<Long, Object> me : objectContainer.getFieldID2value().entrySet()) {
				long fieldID = me.getKey();
				Object fieldValue = me.getValue();
				Class<?> clazz = ec.getClassLoaderResolver().classForName(dataEntryWithClassName.getClassName());
				ClassMeta classMeta = storeManager.getClassMeta(ec, clazz);
				FieldMeta fieldMeta = classMeta.getFieldMeta(fieldID);
				AbstractMemberMetaData dnMemberMetaData = fieldMeta.getDataNucleusMemberMetaData(ec);
				addIndexEntryAction.perform(cryptoContext, dataEntryID, fieldMeta, dnMemberMetaData, fieldValue);
			}
		}
	}

	protected static final class DataEntryWithClassName {
		private DataEntry dataEntry;
		private String className;

		public DataEntryWithClassName(DataEntry dataEntry, String className) {
			if (dataEntry == null)
				throw new IllegalArgumentException("dataEntry == null");
			if (className == null)
				throw new IllegalArgumentException("className == null");
			this.dataEntry = dataEntry;
			this.className = className;
		}
		/**
		 * Get the {@link DataEntry}.
		 * @return the {@link DataEntry}. Never <code>null</code>.
		 */
		public DataEntry getDataEntry() {
			return dataEntry;
		}
		/**
		 * Get the fully qualified class name of the persistence-capable object represented by
		 * {@link #dataEntry}.
		 * @return the fully qualified class name. Never <code>null</code>.
		 */
		public String getClassName() {
			return className;
		}
	}

	protected List<DataEntryWithClassName> getDataEntries(long fromDataEntryIDIncl, Long toDataEntryIDExcl) {
		Query q = pmData.newQuery(DataEntry.class);
		q.setResult("this, this.classMeta.packageName, this.classMeta.simpleClassName");
		StringBuilder filter = new StringBuilder();
		Map<String, Object> params = new HashMap<String, Object>(2);

		filter.append("this.keyStoreRefID == :keyStoreRefID");
		params.put("keyStoreRefID", keyStoreRefID);

		filter.append(" && this.dataEntryID >= :fromDataEntryIDIncl");
		params.put("fromDataEntryIDIncl", fromDataEntryIDIncl);

		if (toDataEntryIDExcl != null) {
			filter.append(" && this.dataEntryID < :toDataEntryIDExcl");
			params.put("toDataEntryIDExcl", toDataEntryIDExcl);
		}
		q.setFilter(filter.toString());
		q.setOrdering("this.dataEntryID ASC");

		@SuppressWarnings("unchecked")
		List<Object[]> l = (List<Object[]>) q.executeWithMap(params);
		List<DataEntryWithClassName> result = new ArrayList<DataEntryWithClassName>(l.size());
		for (Object[] row : l) {
			if (row.length != 3)
				throw new IllegalStateException(String.format("row.length == %s != 3", row.length));

			result.add(new DataEntryWithClassName(
					(DataEntry)row[0],
					ClassMeta.getClassName((String)row[1], (String)row[2])
			));
		}
		q.closeAll();
		return result;
	}

	protected long getMinDataEntryID() {
		Long result = getMinMaxDataEntryID("min");
		if (result == null)
			return 0;
		return result;
	}

	protected long getMaxDataEntryID() {
		Long result = getMinMaxDataEntryID("max");
		if (result == null)
			return 0;
		return result;
	}

	protected Long getMinMaxDataEntryID(String minMax) {
		Query q = pmData.newQuery(DataEntry.class);
		q.setResult(minMax + "(this.dataEntryID)");
		Long result = (Long) q.execute();
		return result;
	}

	protected Long getMinIndexEntryID() {
		return getMinMaxIndexEntryID("min", new Comparator<Long>() {
			@Override
			public int compare(Long o1, Long o2) {
				return o2.compareTo(o1);
			}
		});
	}

	protected Long getMaxIndexEntryID() {
		return getMinMaxIndexEntryID("max", new Comparator<Long>() {
			@Override
			public int compare(Long o1, Long o2) {
				return o1.compareTo(o2);
			}
		});
	}

	protected Long getMinMaxIndexEntryID(String minMax, Comparator<Long> comparator) {
		Long result = null;
		for (Class<? extends IndexEntry> indexEntryClass : getIndexEntryClasses()) {
			Long minMaxIndexEntryID = getMinMaxIndexEntryID(indexEntryClass, minMax);
			if (minMaxIndexEntryID != null) {
				if (result == null || comparator.compare(result, minMaxIndexEntryID) < 0)
					result = minMaxIndexEntryID;
			}
		}
		return result;
	}

	protected Long getMinMaxIndexEntryID(Class<? extends IndexEntry> indexEntryClass, String minMax) {
		Query q = pmIndex.newQuery(indexEntryClass);
		q.setResult(minMax + "(this.indexEntryID)");
		Long result = (Long) q.execute();
		return result;
	}

	protected Set<Class<? extends IndexEntry>> getIndexEntryClasses() {
		if (indexEntryClasses == null)
			indexEntryClasses = ((Cumulus4jStoreManager)cryptoContext.getExecutionContext().getStoreManager()).getIndexFactoryRegistry().getIndexEntryClasses();

		return indexEntryClasses;
	}
}
