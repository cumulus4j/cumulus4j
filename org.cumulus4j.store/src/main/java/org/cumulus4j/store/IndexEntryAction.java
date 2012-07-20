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

import java.lang.reflect.Array;
import java.util.Map;

import javax.jdo.JDOHelper;
import javax.jdo.JDOUserException;
import javax.jdo.PersistenceManager;
import javax.jdo.Query;

import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.FieldMetaRole;
import org.cumulus4j.store.model.IndexEntry;
import org.cumulus4j.store.model.IndexEntryFactory;
import org.cumulus4j.store.model.IndexEntryFactoryRegistry;
import org.cumulus4j.store.model.IndexEntryObjectRelationHelper;
import org.cumulus4j.store.model.IndexValue;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
import org.datanucleus.store.ExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class IndexEntryAction
{
	protected Cumulus4jPersistenceHandler persistenceHandler;
	protected Cumulus4jStoreManager storeManager;
	protected EncryptionHandler encryptionHandler;
	protected IndexEntryFactoryRegistry indexEntryFactoryRegistry;

	public IndexEntryAction(Cumulus4jPersistenceHandler persistenceHandler) {
		if (persistenceHandler == null)
			throw new IllegalArgumentException("persistenceHandler == null");

		this.persistenceHandler = persistenceHandler;
		this.storeManager = persistenceHandler.getStoreManager();
		this.encryptionHandler = storeManager.getEncryptionHandler();
		indexEntryFactoryRegistry = storeManager.getIndexFactoryRegistry();
	}

	protected abstract IndexEntry getIndexEntry(
			IndexEntryFactory indexEntryFactory, CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, Object fieldValue
	);

	protected abstract IndexEntry getIndexEntryForObjectRelation(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, Long otherDataEntryID);

	protected abstract void _perform(CryptoContext cryptoContext, IndexEntry indexEntry, long dataEntryID);

	public void perform(CryptoContext cryptoContext, long dataEntryID, FieldMeta fieldMeta,
			AbstractMemberMetaData dnMemberMetaData, Object fieldValue
	)
	{
		ExecutionContext ec = cryptoContext.getExecutionContext();
		PersistenceManager pmData = cryptoContext.getPersistenceManagerForData();
		PersistenceManager pmIndex = cryptoContext.getPersistenceManagerForIndex();
		boolean hasQueryable = dnMemberMetaData.hasExtension(Cumulus4jStoreManager.CUMULUS4J_QUERYABLE);
		if (hasQueryable) {
			String val = dnMemberMetaData.getValueForExtension(Cumulus4jStoreManager.CUMULUS4J_QUERYABLE);
			if (val.equalsIgnoreCase("false")) {
				// Field marked as not queryable, so don't index it
				return;
			}
		}

		int relationType = dnMemberMetaData.getRelationType(cryptoContext.getExecutionContext().getClassLoaderResolver());
		if (Relation.NONE == relationType) {
			// The field contains no other persistent entity. It might contain a collection/array/map, though.

			if (dnMemberMetaData.hasCollection() || dnMemberMetaData.hasArray()) {
				FieldMetaRole role;
				if (dnMemberMetaData.hasCollection())
					role = FieldMetaRole.collectionElement;
				else
					role = FieldMetaRole.arrayElement;

				FieldMeta subFieldMeta = fieldMeta.getSubFieldMeta(role);
				IndexEntryFactory indexEntryFactory = indexEntryFactoryRegistry.getIndexEntryFactory(ec, subFieldMeta, false);
				if (fieldValue != null) {
					for (int idx = 0; idx < Array.getLength(fieldValue); ++idx) {
						Object element = Array.get(fieldValue, idx);
						IndexEntry indexEntry = getIndexEntry(indexEntryFactory, cryptoContext, pmIndex, subFieldMeta, element);
						_perform(cryptoContext, indexEntry, dataEntryID);
					}
				}

				// Add entry for the collection/array size
				int containerSize = fieldValue == null ? 0 : Array.getLength(fieldValue);
				IndexEntry sizeIdxEntry =
					indexEntryFactoryRegistry.getIndexEntryFactoryForContainerSize().createIndexEntry(cryptoContext, pmIndex, fieldMeta, new Long(containerSize));
				_perform(cryptoContext, sizeIdxEntry, dataEntryID);
			}
			else if (dnMemberMetaData.hasMap()) {
				Map<?,?> fieldValueMap = (Map<?,?>) fieldValue;

				FieldMeta subFieldMetaKey = fieldMeta.getSubFieldMeta(FieldMetaRole.mapKey);
				FieldMeta subFieldMetaValue = fieldMeta.getSubFieldMeta(FieldMetaRole.mapValue);
				IndexEntryFactory indexEntryFactoryKey = indexEntryFactoryRegistry.getIndexEntryFactory(ec, subFieldMetaKey, false);
				IndexEntryFactory indexEntryFactoryValue = indexEntryFactoryRegistry.getIndexEntryFactory(ec, subFieldMetaValue, false);

				if (fieldValueMap != null) {
					for (Map.Entry<?, ?> me : fieldValueMap.entrySet()) {
						IndexEntry indexEntryKey = getIndexEntry(indexEntryFactoryKey, cryptoContext, pmIndex, subFieldMetaKey, me.getKey());
						_perform(cryptoContext, indexEntryKey, dataEntryID);

						IndexEntry indexEntryValue = getIndexEntry(indexEntryFactoryValue, cryptoContext, pmIndex, subFieldMetaValue, me.getValue());
						_perform(cryptoContext, indexEntryValue, dataEntryID);
					}
				}

				// Add entry for the map size
				int containerSize = (fieldValueMap != null ? fieldValueMap.size() : 0);
				IndexEntry sizeIdxEntry =
					indexEntryFactoryRegistry.getIndexEntryFactoryForContainerSize().createIndexEntry(cryptoContext, pmIndex, fieldMeta, new Long(containerSize));
				_perform(cryptoContext, sizeIdxEntry, dataEntryID);
			}
			else {
				IndexEntryFactory indexEntryFactory = indexEntryFactoryRegistry.getIndexEntryFactory(ec, fieldMeta, false);
				IndexEntry indexEntry = getIndexEntry(indexEntryFactory, cryptoContext, pmIndex, fieldMeta, fieldValue);
				_perform(cryptoContext, indexEntry, dataEntryID);
			}
		}
		else if (Relation.isRelationSingleValued(relationType)) {
			// 1-1-relationship to another persistence-capable object.
			Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(cryptoContext, pmData, fieldValue);
			IndexEntry indexEntry = getIndexEntryForObjectRelation(cryptoContext, pmIndex, fieldMeta, otherDataEntryID);
			_perform(cryptoContext, indexEntry, dataEntryID);
		}
		else if (Relation.isRelationMultiValued(relationType)) {
			// map, collection, array

			if (dnMemberMetaData.hasMap()) { // Map.class.isAssignableFrom(dnMemberMetaData.getType())) {
				Map<?,?> fieldValueMap = (Map<?,?>) fieldValue;

				boolean keyIsPersistent = dnMemberMetaData.getMap().keyIsPersistent();
				boolean valueIsPersistent = dnMemberMetaData.getMap().valueIsPersistent();

				FieldMeta subFieldMetaKey = fieldMeta.getSubFieldMeta(FieldMetaRole.mapKey);
				FieldMeta subFieldMetaValue = fieldMeta.getSubFieldMeta(FieldMetaRole.mapValue);
				IndexEntryFactory indexEntryFactoryKey = indexEntryFactoryRegistry.getIndexEntryFactory(ec, subFieldMetaKey, false);
				IndexEntryFactory indexEntryFactoryValue = indexEntryFactoryRegistry.getIndexEntryFactory(ec, subFieldMetaValue, false);

				if (fieldValueMap != null) {
					for (Map.Entry<?, ?> me : fieldValueMap.entrySet()) {
						if (keyIsPersistent) {
							Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(cryptoContext, pmData, me.getKey());
							IndexEntry indexEntry = getIndexEntryForObjectRelation(cryptoContext, pmIndex, subFieldMetaKey, otherDataEntryID);
							_perform(cryptoContext, indexEntry, dataEntryID);
						}
						else {
							IndexEntry indexEntry = getIndexEntry(indexEntryFactoryKey, cryptoContext, pmIndex, subFieldMetaKey, me.getKey());
							_perform(cryptoContext, indexEntry, dataEntryID);
						}

						if (valueIsPersistent) {
							Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(cryptoContext, pmData, me.getValue());
							IndexEntry indexEntry = getIndexEntryForObjectRelation(cryptoContext, pmIndex, subFieldMetaValue, otherDataEntryID);
							_perform(cryptoContext, indexEntry, dataEntryID);
						}
						else {
							IndexEntry indexEntry = getIndexEntry(indexEntryFactoryValue, cryptoContext, pmIndex, subFieldMetaValue, me.getValue());
							_perform(cryptoContext, indexEntry, dataEntryID);
						}
					}
				}

				// Add entry for the map size
				int containerSize = (fieldValueMap != null ? fieldValueMap.size() : 0);
				IndexEntry sizeIdxEntry =
					indexEntryFactoryRegistry.getIndexEntryFactoryForContainerSize().createIndexEntry(cryptoContext, pmIndex, fieldMeta, new Long(containerSize));
				_perform(cryptoContext, sizeIdxEntry, dataEntryID);
			}
			else if (dnMemberMetaData.hasCollection() || dnMemberMetaData.hasArray()) {
				FieldMetaRole role;
				if (dnMemberMetaData.hasCollection()) // Collection.class.isAssignableFrom(dnMemberMetaData.getType()))
					role = FieldMetaRole.collectionElement;
				else
					role = FieldMetaRole.arrayElement;

				FieldMeta subFieldMeta = fieldMeta.getSubFieldMeta(role);
				Object[] fieldValueArray = (Object[]) fieldValue;
				if (fieldValueArray != null) {
					for (Object element : fieldValueArray) {
						Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(cryptoContext, pmData, element);
						IndexEntry indexEntry = getIndexEntryForObjectRelation(cryptoContext, pmIndex, subFieldMeta, otherDataEntryID);
						_perform(cryptoContext, indexEntry, dataEntryID);
					}
				}

				// Add entry for the collection/array size
				int containerSize = (fieldValueArray != null ? fieldValueArray.length : 0);
				IndexEntry sizeIdxEntry =
					indexEntryFactoryRegistry.getIndexEntryFactoryForContainerSize().createIndexEntry(cryptoContext, pmIndex, fieldMeta, new Long(containerSize));
				_perform(cryptoContext, sizeIdxEntry, dataEntryID);
			}
		}
	}

	static class Add extends IndexEntryAction
	{
		public Add(Cumulus4jPersistenceHandler persistenceHandler) {
			super(persistenceHandler);
		}

		@Override
		public IndexEntry getIndexEntry(IndexEntryFactory indexEntryFactory, CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, Object fieldValue)
		{
			return indexEntryFactory == null ? null : indexEntryFactory.createIndexEntry(cryptoContext, pmIndex, fieldMeta, fieldValue);
		}

		@Override
		public IndexEntry getIndexEntryForObjectRelation(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, Long otherDataEntryID) {
			return IndexEntryObjectRelationHelper.createIndexEntry(cryptoContext, pmIndex, fieldMeta, otherDataEntryID);
		}

		@Override
		protected void _perform(CryptoContext cryptoContext, IndexEntry indexEntry, long dataEntryID)
		{
			if (indexEntry == null)
				return;

			IndexValue indexValue = encryptionHandler.decryptIndexEntry(cryptoContext, indexEntry);
			indexValue.addDataEntryID(dataEntryID);
			encryptionHandler.encryptIndexEntry(cryptoContext, indexEntry, indexValue);
			cryptoContext.getPersistenceManagerForIndex().makePersistent(indexEntry); // We do not persist directly when creating anymore, thus we must persist here. This is a no-op if it's already persistent.
		}
	}

	static class Remove extends IndexEntryAction
	{
		private static final Logger logger = LoggerFactory.getLogger(Remove.class);

		public Remove(Cumulus4jPersistenceHandler persistenceHandler) {
			super(persistenceHandler);
		}

		@Override
		public IndexEntry getIndexEntry(IndexEntryFactory indexEntryFactory, CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, Object fieldValue)
		{
			return indexEntryFactory == null ? null : indexEntryFactory.getIndexEntry(cryptoContext, pmIndex, fieldMeta, fieldValue);
		}

		@Override
		protected IndexEntry getIndexEntryForObjectRelation(CryptoContext cryptoContext, PersistenceManager pmIndex, FieldMeta fieldMeta, Long otherDataEntryID) {
			return IndexEntryObjectRelationHelper.getIndexEntry(cryptoContext, pmIndex, fieldMeta, otherDataEntryID);
		}

		@Override
		protected void _perform(CryptoContext cryptoContext, IndexEntry indexEntry, long dataEntryID)
		{
			if (indexEntry == null)
				return;

			IndexValue indexValue = encryptionHandler.decryptIndexEntry(cryptoContext, indexEntry);
			indexValue.removeDataEntryID(dataEntryID);
			if (indexValue.isDataEntryIDsEmpty()) {
				PersistenceManager pmIndex = cryptoContext.getPersistenceManagerForIndex();
				try {
					pmIndex.deletePersistent(indexEntry);
					// The above line sometimes (but not always) causes the following error:
//					org.datanucleus.exceptions.NucleusUserException: Transient instances cant be deleted.
//			        at org.datanucleus.ObjectManagerImpl.deleteObjectInternal(ObjectManagerImpl.java:2068)
//			        at org.datanucleus.ObjectManagerImpl.deleteObjectWork(ObjectManagerImpl.java:2019)
//			        at org.datanucleus.ObjectManagerImpl.deleteObject(ObjectManagerImpl.java:1973)
//			        at org.datanucleus.api.jdo.JDOPersistenceManager.jdoDeletePersistent(JDOPersistenceManager.java:815)
//			        at org.datanucleus.api.jdo.JDOPersistenceManager.deletePersistent(JDOPersistenceManager.java:833)
//			        at org.cumulus4j.store.IndexEntryAction$Remove._perform(IndexEntryAction.java:268)
//			        at org.cumulus4j.store.IndexEntryAction.perform(IndexEntryAction.java:206)
//			        at org.cumulus4j.store.Cumulus4jPersistenceHandler.updateObject(Cumulus4jPersistenceHandler.java:343)
				} catch (JDOUserException x) {
					logger.warn("_perform: " + x, x);
					// If we cannot delete it, we try to store an empty value. That's not nice, but at least a consistent DB state.
					encryptionHandler.encryptIndexEntry(cryptoContext, indexEntry, indexValue);
					// Make sure it is persisted or we get a new exception (because we just tried to delete it).
					pmIndex.makePersistent(indexEntry);
					pmIndex.flush();
					Query query = pmIndex.newQuery(IndexEntry.class);
					query.setFilter("JDOHelper.getObjectId(this) == :indexEntryID");
					query.setUnique(true);
					IndexEntry persistentIndexEntry = (IndexEntry) query.execute(JDOHelper.getObjectId(indexEntry));
					if (persistentIndexEntry == null)
						throw new IllegalStateException("Newly persisted IndexEntry did not end up in our database! indexEntryID=" + indexEntry.getIndexEntryID());
				}

				// BEGIN workaround - this works, but is not nice.
//				Object indexEntryID = JDOHelper.getObjectId(indexEntry);
//				if (indexEntryID != null) {
//					PersistenceManager pmIdx = cryptoContext.getPersistenceManagerForIndex();
//					pmIdx.flush();
//					Query query = pmIdx.newQuery(IndexEntry.class);
//					query.setFilter("JDOHelper.getObjectId(this) == :indexEntryID");
//					query.setUnique(true);
//					IndexEntry persistentIndexEntry = (IndexEntry) query.execute(indexEntryID);
//					if (persistentIndexEntry != null)
//						pmIdx.deletePersistent(persistentIndexEntry);
//				}
				// END workaround
			}
			else
				encryptionHandler.encryptIndexEntry(cryptoContext, indexEntry, indexValue);
		}
	}
}