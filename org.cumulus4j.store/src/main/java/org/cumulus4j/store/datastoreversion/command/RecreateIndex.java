package org.cumulus4j.store.datastoreversion.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.store.Cumulus4jPersistenceHandler;
import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.EncryptionHandler;
import org.cumulus4j.store.IndexEntryAction;
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

	private CommandApplyParam commandApplyParam;
	private CryptoContext cryptoContext;
	private PersistenceManager pmIndex;
	private PersistenceManager pmData;
	private long keyStoreRefID;

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

		deleteIndex();
		createIndex();
	}

	protected void deleteIndex() {
		logger.debug("deleteIndex: Entered.");
		final long indexEntryBlockSize = 1000;
		final long minIndexEntryID = getMinIndexEntryID();
		final long maxIndexEntryID = getMaxIndexEntryID();
		logger.info("deleteIndex: minIndexEntryID={} maxIndexEntryID={}", minIndexEntryID, maxIndexEntryID);
		long fromIndexEntryID = minIndexEntryID;
		while (fromIndexEntryID <= maxIndexEntryID - indexEntryBlockSize) {
			long toIndexEntryIDExcl = fromIndexEntryID + indexEntryBlockSize;
			deleteIndexForRange(fromIndexEntryID, toIndexEntryIDExcl);
			fromIndexEntryID = toIndexEntryIDExcl;
		}
		deleteIndexForRange(fromIndexEntryID, null);

		logger.debug("deleteIndex: Leaving.");
	}

	protected void deleteIndexForRange(long fromIndexEntryIDIncl, Long toIndexEntryIDExcl) {
		logger.info("deleteIndexForRange: Entered. fromIndexEntryIDIncl={} toIndexEntryIDExcl={}", fromIndexEntryIDIncl, toIndexEntryIDExcl);
		List<IndexEntry> indexEntries = getIndexEntries(fromIndexEntryIDIncl, toIndexEntryIDExcl);
		pmIndex.deletePersistentAll(indexEntries);
		logger.info("deleteIndexForRange: Leaving. fromIndexEntryIDIncl={} toIndexEntryIDExcl={}", fromIndexEntryIDIncl, toIndexEntryIDExcl);
	}

	protected List<IndexEntry> getIndexEntries(long fromIndexEntryIDIncl, Long toIndexEntryIDExcl) {
		Query q = pmIndex.newQuery(DataEntry.class);
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

	protected void createIndex() {
		final long dataEntryBlockSize = 1000;
		final long minDataEntryID = getMinDataEntryID();
		final long maxDataEntryID = getMaxDataEntryID();
		long fromDataEntryID = minDataEntryID;
		while (fromDataEntryID <= maxDataEntryID - dataEntryBlockSize) {
			long toDataEntryIDExcl = fromDataEntryID + dataEntryBlockSize;
			createIndexForRange(fromDataEntryID, toDataEntryIDExcl);
			fromDataEntryID = toDataEntryIDExcl;
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
		return getMinMaxDataEntryID("min");
	}

	protected long getMaxDataEntryID() {
		return getMinMaxDataEntryID("max");
	}

	protected long getMinMaxDataEntryID(String minMax) {
		Query q = pmData.newQuery(DataEntry.class);
		q.setResult(minMax + "(this.dataEntryID)");
		Long result = (Long) q.execute();
		// BEGIN WORKAROUND
		if (result == null)
			return 0;
		// END WORKAROUND
		return result;
	}

	protected long getMinIndexEntryID() {
		return getMinMaxIndexEntryID("min");
	}

	protected long getMaxIndexEntryID() {
		return getMinMaxIndexEntryID("max");
	}

	protected long getMinMaxIndexEntryID(String minMax) {
		Query q = pmIndex.newQuery(IndexEntry.class);
		q.setResult(minMax + "(this.indexEntryID)");
		Long result = (Long) q.execute();
		// BEGIN WORKAROUND
		if (result == null)
			return 0;
		// END WORKAROUND
		return result;
	}
}
