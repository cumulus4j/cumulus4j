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
package org.cumulus4j.store;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;

import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.DataEntryDAO;
import org.cumulus4j.store.model.EmbeddedObjectContainer;
import org.cumulus4j.store.model.ObjectContainer;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.store.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for replacing object-references when storing a 1-1- or 1-n- or m-n-relationship
 * inside an {@link ObjectContainer}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class ObjectContainerHelper
{
	private static final Logger logger = LoggerFactory.getLogger(ObjectContainerHelper.class);

	/**
	 * If <code>false</code>, store object-ID in {@link ObjectContainer}.
	 * If <code>true</code>, store {@link DataEntry#getDataEntryID() dataEntryID} in {@link ObjectContainer}.
	 */
	private static final boolean USE_DATA_ENTRY_ID = true;

	private ObjectContainerHelper() { }

	private static final class TemporaryReferenceDataEntry {
		public CryptoContext cryptoContext;
		public String objectID;
		public long dataEntryID = -1;
		public ClassMeta classMeta;
	}

	private static void registerTemporaryReferenceDataEntry(CryptoContext cryptoContext, PersistenceManager pmData, DataEntry dataEntry)
	{
		assertTemporaryReferenceScopeEntered(cryptoContext, pmData);

		Map<String, TemporaryReferenceDataEntry> objectID2tempRefMap = temporaryReferenceDataEntryMapThreadLocal.get();
		if (objectID2tempRefMap == null) {
			objectID2tempRefMap = new HashMap<String, TemporaryReferenceDataEntry>();
			temporaryReferenceDataEntryMapThreadLocal.set(objectID2tempRefMap);
		}

		TemporaryReferenceDataEntry trde = new TemporaryReferenceDataEntry();
		trde.cryptoContext = cryptoContext;
		trde.objectID = dataEntry.getObjectID();
		trde.dataEntryID = dataEntry.getDataEntryID();
		trde.classMeta = dataEntry.getClassMeta();

		if (trde.dataEntryID < 0) {
			throw new IllegalStateException("dataEntry.dataEntryID < 0 :: trde.objectID = " + trde.objectID);
		}

		objectID2tempRefMap.put(trde.objectID, trde);
	}

	private static ThreadLocal<Map<String, TemporaryReferenceDataEntry>> temporaryReferenceDataEntryMapThreadLocal = new ThreadLocal<Map<String,TemporaryReferenceDataEntry>>();

	private static ThreadLocal<Integer> temporaryReferenceScopeCounterThreadLocal = new ThreadLocal<Integer>();

	public static void enterTemporaryReferenceScope() {
		Integer temporaryReferenceScopeCounter = temporaryReferenceScopeCounterThreadLocal.get();

		if (temporaryReferenceScopeCounter == null) {
			assertNoEmptyTemporaryReferenceDataEntry();
			temporaryReferenceScopeCounter = 1;
		}
		else
			temporaryReferenceScopeCounter = temporaryReferenceScopeCounter + 1;

		temporaryReferenceScopeCounterThreadLocal.set(temporaryReferenceScopeCounter);
	}

	public static void exitTemporaryReferenceScope(boolean error) {
		Integer temporaryReferenceScopeCounter = temporaryReferenceScopeCounterThreadLocal.get();

		if (temporaryReferenceScopeCounter == null)
			throw new IllegalStateException("temporaryReferenceScopeCounter == null");

		temporaryReferenceScopeCounter = temporaryReferenceScopeCounter - 1;
		if (temporaryReferenceScopeCounter.intValue() == 0) {
			temporaryReferenceScopeCounterThreadLocal.remove();
			if (error)
				deleteTemporaryReferenceEmptyDataEntries();
			else {
				assertNoEmptyTemporaryReferenceDataEntry();
				temporaryReferenceDataEntryMapThreadLocal.remove();
			}
		}
		else {
			if (temporaryReferenceScopeCounter.intValue() < 0)
				throw new IllegalStateException("temporaryReferenceScopeCounter < 0");

			temporaryReferenceScopeCounterThreadLocal.set(temporaryReferenceScopeCounter);
		}
	}

	public static DataEntry getTemporaryReferenceDataEntry(CryptoContext cryptoContext, PersistenceManager pmData, String objectIDString)
	{
		assertTemporaryReferenceScopeEntered(cryptoContext, pmData);

		Map<String, TemporaryReferenceDataEntry> objectID2tempRefMap = temporaryReferenceDataEntryMapThreadLocal.get();
		if (objectID2tempRefMap == null)
			return null;

		TemporaryReferenceDataEntry trde = objectID2tempRefMap.get(objectIDString);
		if (trde == null)
			return null;

		DataEntry dataEntry = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntry(trde.dataEntryID); // .getDataEntry(trde.classMeta, objectIDString);
		return dataEntry;
	}

	private static void deleteTemporaryReferenceEmptyDataEntries() {
		Map<String, TemporaryReferenceDataEntry> objectID2tempRefMap = temporaryReferenceDataEntryMapThreadLocal.get();
		if (objectID2tempRefMap == null || objectID2tempRefMap.isEmpty())
			return;

		for (TemporaryReferenceDataEntry trde : objectID2tempRefMap.values()) {
			PersistenceManager pmData = trde.cryptoContext.getPersistenceManagerForData();
			DataEntryDAO dataEntryDAO = new DataEntryDAO(pmData, trde.cryptoContext.getKeyStoreRefID());
			DataEntry dataEntry = dataEntryDAO.getDataEntry(trde.dataEntryID);
			if (dataEntry != null && (dataEntry.getValue() == null || dataEntry.getValue().length == 0))
				pmData.deletePersistent(dataEntry);
		}
		temporaryReferenceDataEntryMapThreadLocal.remove();
	}

	private static void assertNoEmptyTemporaryReferenceDataEntry() {
		Map<String, TemporaryReferenceDataEntry> objectID2tempRefMap = temporaryReferenceDataEntryMapThreadLocal.get();
		if (objectID2tempRefMap == null || objectID2tempRefMap.isEmpty())
			return;

		for (TemporaryReferenceDataEntry trde : objectID2tempRefMap.values()) {
			PersistenceManager pmData = trde.cryptoContext.getPersistenceManagerForData();
			DataEntryDAO dataEntryDAO = new DataEntryDAO(pmData, trde.cryptoContext.getKeyStoreRefID());
			DataEntry dataEntry = dataEntryDAO.getDataEntry(trde.dataEntryID);
			if (dataEntry != null && (dataEntry.getValue() == null || dataEntry.getValue().length == 0))
				throw new IllegalStateException("Found empty TemporaryReferenceDataEntry! dataEntryID=" + trde.dataEntryID
						+ " classMeta.classID=" + (trde.classMeta == null ? null : trde.classMeta.getClassID())
						+ " objectID=" + trde.objectID);
		}
	}

	private static void assertTemporaryReferenceScopeEntered(CryptoContext cryptoContext, PersistenceManager pmData) {
		Integer temporaryReferenceScopeCounter = temporaryReferenceScopeCounterThreadLocal.get();

		if (temporaryReferenceScopeCounter == null)
			throw new IllegalStateException("temporaryReferenceScopeCounter == null");

		if (temporaryReferenceScopeCounter.intValue() < 1)
			throw new IllegalStateException("temporaryReferenceScopeCounter < 1");
	}

	public static Object entityToReference(CryptoContext cryptoContext, PersistenceManager pmData, Object entity)
	{
		if (entity == null)
			return null;

		ExecutionContext ec = cryptoContext.getExecutionContext();
		Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) ec.getStoreManager();
		Object objectID = ec.getApiAdapter().getIdForObject(entity);
		if (objectID == null)
			throw new IllegalStateException("executionContext.getApiAdapter().getIdForObject(entity) returned null for " + entity);

		storeManager.setClassNameForObjectID(objectID, entity.getClass().getName());

		if (USE_DATA_ENTRY_ID) {
			ClassMeta classMeta = storeManager.getClassMeta(ec, entity.getClass());
			String objectIDString = objectID.toString();
			Long dataEntryID = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntryID(classMeta, objectIDString);
			if (dataEntryID == null) {
				// Referenced entity not yet persisted => Create a temporarily empty DataEntry. It should be
				// filled later when Cumulus4jPersistenceHandler.insertObject(...) is called for this entity.
				//
				// TODO If we ever stumble over empty DataEntry objects in the database, we should add a sanity check,
				// which checks at the end of a flush(...) or commit(...) whether all of the DataEntry objects created here
				// were actually post-processed by a call to Cumulus4jPersistenceHandler.insertObject(...). Marco :-)
				DataEntry dataEntry = pmData.makePersistent(new DataEntry(classMeta, cryptoContext.getKeyStoreRefID(), objectIDString));
				dataEntryID = dataEntry.getDataEntryID();
				registerTemporaryReferenceDataEntry(cryptoContext, pmData, dataEntry);
				logger.trace("entityToReference: Created temporary-reference-DataEntry for: {}", objectIDString);
//				throw new IllegalStateException("DataEntry.getDataEntryID(...) returned null for entity=\"" + entity + "\" with objectID=\"" + objectID +  "\"");
			}

			return dataEntryID;
		}

		return objectID;
	}

	public static Object referenceToEntity(CryptoContext cryptoContext, PersistenceManager pmData, Object reference)
	{
		if (reference == null)
			return null;

		ExecutionContext ec = cryptoContext.getExecutionContext();

		if (USE_DATA_ENTRY_ID) {
			DataEntry dataEntry = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntry(((Long)reference).longValue());
			if (dataEntry != null && JDOHelper.isDeleted(dataEntry)) {
				// Added check for deleted state because of https://sourceforge.net/tracker/?func=detail&aid=3515534&group_id=517465&atid=2102911
				// Marco :-)
				logger.warn("referenceToEntity: DataEntry.getDataEntry(...) returned deleted instance for dataEntryID=\"{}\"! Setting it to null.", reference);
				dataEntry = null;
			}

			if (dataEntry == null) {
				String message = String.format("DataEntry.getDataEntry(...) returned null for reference=\"%s\"!", reference);
				if (ec.getNucleusContext().getStoreManager().getPersistenceHandler().useReferentialIntegrity())
					throw new IllegalStateException(message);
				else {
					// https://sourceforge.net/tracker/?func=detail&aid=3515529&group_id=517465&atid=2102914
					logger.warn("referenceToEntity: {} Returning null, because reference is orphaned.", message);
					return null;
				}
			}

			AbstractClassMetaData cmd = dataEntry.getClassMeta().getDataNucleusClassMetaData(ec);
			return IdentityUtils.getObjectFromIdString(dataEntry.getObjectID(), cmd, ec, true);
		}

		return ec.findObject(reference, true, true, null);
	}

	public static Long referenceToDataEntryID(CryptoContext cryptoContext, PersistenceManager pmData, Object reference)
	{
		if (reference == null)
			return null;

		if (USE_DATA_ENTRY_ID) {
			if (reference instanceof EmbeddedObjectContainer)
				return null;

			return (Long)reference;
		}

		ExecutionContext ec = cryptoContext.getExecutionContext();
		Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) ec.getStoreManager();
		String clazzName = storeManager.getClassNameForObjectID(reference, ec.getClassLoaderResolver(), ec);
		Class<?> clazz = ec.getClassLoaderResolver().classForName(clazzName);
		ClassMeta classMeta = storeManager.getClassMeta(ec, clazz);
		return new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntryID(classMeta, reference.toString());
	}
}
