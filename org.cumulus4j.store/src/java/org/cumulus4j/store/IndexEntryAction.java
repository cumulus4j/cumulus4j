package org.cumulus4j.store;

import java.lang.reflect.Array;
import java.util.Map;

import javax.jdo.PersistenceManager;

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
			IndexEntryFactory indexEntryFactory, PersistenceManager pmIndex, FieldMeta fieldMeta, Object fieldValue
	);

	protected abstract IndexEntry getIndexEntryForObjectRelation(PersistenceManager pmIndex, FieldMeta fieldMeta, Long otherDataEntryID);

	protected abstract void _perform(ExecutionContext executionContext, PersistenceManager pmIndex, IndexEntry indexEntry, long dataEntryID);

	public void perform(ExecutionContext ec, PersistenceManager pmIndex, long dataEntryID,
			FieldMeta fieldMeta, AbstractMemberMetaData dnMemberMetaData, Object fieldValue
	)
	{
		boolean hasQueryable = dnMemberMetaData.hasExtension(Cumulus4jStoreManager.CUMULUS4J_QUERYABLE);
		if (hasQueryable) {
			String val = dnMemberMetaData.getValueForExtension(Cumulus4jStoreManager.CUMULUS4J_QUERYABLE);
			if (val.equalsIgnoreCase("false")) {
				// Field marked as not queryable, so don't index it
				return;
			}
		}

		int relationType = dnMemberMetaData.getRelationType(ec.getClassLoaderResolver());
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
				for (int idx = 0; idx < Array.getLength(fieldValue); ++idx) {
					Object element = Array.get(fieldValue, idx);
					IndexEntry indexEntry = getIndexEntry(indexEntryFactory, pmIndex, subFieldMeta, element);
					_perform(ec, pmIndex, indexEntry, dataEntryID);
				}

				// Add entry for the collection/array size
				int containerSize = Array.getLength(fieldValue);
				IndexEntry sizeIdxEntry = 
					indexEntryFactoryRegistry.getIndexEntryFactoryForContainerSize().createIndexEntry(pmIndex, fieldMeta, new Long(containerSize));
				_perform(ec, pmIndex, sizeIdxEntry, dataEntryID);
			}
			else if (dnMemberMetaData.hasMap()) {
				Map<?,?> fieldValueMap = (Map<?,?>) fieldValue;

				FieldMeta subFieldMetaKey = fieldMeta.getSubFieldMeta(FieldMetaRole.mapKey);
				FieldMeta subFieldMetaValue = fieldMeta.getSubFieldMeta(FieldMetaRole.mapValue);
				IndexEntryFactory indexEntryFactoryKey = indexEntryFactoryRegistry.getIndexEntryFactory(ec, subFieldMetaKey, false);
				IndexEntryFactory indexEntryFactoryValue = indexEntryFactoryRegistry.getIndexEntryFactory(ec, subFieldMetaValue, false);

				for (Map.Entry<?, ?> me : fieldValueMap.entrySet()) {
					IndexEntry indexEntryKey = getIndexEntry(indexEntryFactoryKey, pmIndex, subFieldMetaKey, me.getKey());
					_perform(ec, pmIndex, indexEntryKey, dataEntryID);

					IndexEntry indexEntryValue = getIndexEntry(indexEntryFactoryValue, pmIndex, subFieldMetaValue, me.getValue());
					_perform(ec, pmIndex, indexEntryValue, dataEntryID);
				}

				// Add entry for the map size
				int containerSize = (fieldValueMap != null ? fieldValueMap.size() : 0);
				IndexEntry sizeIdxEntry = 
					indexEntryFactoryRegistry.getIndexEntryFactoryForContainerSize().createIndexEntry(pmIndex, fieldMeta, new Long(containerSize));
				_perform(ec, pmIndex, sizeIdxEntry, dataEntryID);
			}
			else {
				IndexEntryFactory indexEntryFactory = indexEntryFactoryRegistry.getIndexEntryFactory(ec, fieldMeta, false);
				IndexEntry indexEntry = getIndexEntry(indexEntryFactory, pmIndex, fieldMeta, fieldValue);
				_perform(ec, pmIndex, indexEntry, dataEntryID);
			}
		}
		else if (Relation.isRelationSingleValued(relationType)) {
			// 1-1-relationship to another persistence-capable object.
			Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(ec, pmIndex, fieldValue);
			IndexEntry indexEntry = getIndexEntryForObjectRelation(pmIndex, fieldMeta, otherDataEntryID);
			_perform(ec, pmIndex, indexEntry, dataEntryID);
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

				for (Map.Entry<?, ?> me : fieldValueMap.entrySet()) {
					if (keyIsPersistent) {
						Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(ec, pmIndex, me.getKey());
						IndexEntry indexEntry = getIndexEntryForObjectRelation(pmIndex, subFieldMetaKey, otherDataEntryID);
						_perform(ec, pmIndex, indexEntry, dataEntryID);
					}
					else {
						IndexEntry indexEntry = getIndexEntry(indexEntryFactoryKey, pmIndex, subFieldMetaKey, me.getKey());
						_perform(ec, pmIndex, indexEntry, dataEntryID);
					}

					if (valueIsPersistent) {
						Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(ec, pmIndex, me.getValue());
						IndexEntry indexEntry = getIndexEntryForObjectRelation(pmIndex, subFieldMetaValue, otherDataEntryID);
						_perform(ec, pmIndex, indexEntry, dataEntryID);
					}
					else {
						IndexEntry indexEntry = getIndexEntry(indexEntryFactoryValue, pmIndex, subFieldMetaValue, me.getValue());
						_perform(ec, pmIndex, indexEntry, dataEntryID);
					}
				}

				// Add entry for the map size
				int containerSize = (fieldValueMap != null ? fieldValueMap.size() : 0);
				IndexEntry sizeIdxEntry = 
					indexEntryFactoryRegistry.getIndexEntryFactoryForContainerSize().createIndexEntry(pmIndex, fieldMeta, new Long(containerSize));
				_perform(ec, pmIndex, sizeIdxEntry, dataEntryID);
			}
			else if (dnMemberMetaData.hasCollection() || dnMemberMetaData.hasArray()) {
				FieldMetaRole role;
				if (dnMemberMetaData.hasCollection()) // Collection.class.isAssignableFrom(dnMemberMetaData.getType()))
					role = FieldMetaRole.collectionElement;
				else
					role = FieldMetaRole.arrayElement;

				FieldMeta subFieldMeta = fieldMeta.getSubFieldMeta(role);
				Object[] fieldValueArray = (Object[]) fieldValue;
				for (Object element : fieldValueArray) {
					Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(ec, pmIndex, element);
					IndexEntry indexEntry = getIndexEntryForObjectRelation(pmIndex, subFieldMeta, otherDataEntryID);
					_perform(ec, pmIndex, indexEntry, dataEntryID);
				}

				// Add entry for the collection/array size
				int containerSize = (fieldValueArray != null ? fieldValueArray.length : 0);
				IndexEntry sizeIdxEntry = 
					indexEntryFactoryRegistry.getIndexEntryFactoryForContainerSize().createIndexEntry(pmIndex, fieldMeta, new Long(containerSize));
				_perform(ec, pmIndex, sizeIdxEntry, dataEntryID);
			}
		}
	}

	static class Add extends IndexEntryAction
	{
		public Add(Cumulus4jPersistenceHandler persistenceHandler) {
			super(persistenceHandler);
		}

		@Override
		public IndexEntry getIndexEntry(IndexEntryFactory indexEntryFactory, PersistenceManager pmIndex, FieldMeta fieldMeta, Object fieldValue)
		{
			return indexEntryFactory == null ? null : indexEntryFactory.createIndexEntry(pmIndex, fieldMeta, fieldValue);
		}

		@Override
		public IndexEntry getIndexEntryForObjectRelation(PersistenceManager pmIndex, FieldMeta fieldMeta, Long otherDataEntryID) {
			return IndexEntryObjectRelationHelper.createIndexEntry(pmIndex, fieldMeta, otherDataEntryID);
		}

		@Override
		protected void _perform(ExecutionContext ec, PersistenceManager pmIndex, IndexEntry indexEntry, long dataEntryID)
		{
			if (indexEntry == null)
				return;

			IndexValue indexValue = encryptionHandler.decryptIndexEntry(ec, indexEntry);
			indexValue.addDataEntryID(dataEntryID);
			encryptionHandler.encryptIndexEntry(ec, indexEntry, indexValue);
			pmIndex.makePersistent(indexEntry); // We do not persist directly when creating anymore, thus we must persist here. This is a no-op if it's already persistent.
		}
	}

	static class Remove extends IndexEntryAction
	{
		public Remove(Cumulus4jPersistenceHandler persistenceHandler) {
			super(persistenceHandler);
		}

		@Override
		public IndexEntry getIndexEntry(IndexEntryFactory indexEntryFactory, PersistenceManager pmIndex, FieldMeta fieldMeta, Object fieldValue)
		{
			return indexEntryFactory == null ? null : indexEntryFactory.getIndexEntry(pmIndex, fieldMeta, fieldValue);
		}

		@Override
		protected IndexEntry getIndexEntryForObjectRelation(PersistenceManager pmIndex, FieldMeta fieldMeta, Long otherDataEntryID) {
			return IndexEntryObjectRelationHelper.getIndexEntry(pmIndex, fieldMeta, otherDataEntryID);
		}

		@Override
		protected void _perform(ExecutionContext ec, PersistenceManager pmIndex, IndexEntry indexEntry, long dataEntryID)
		{
			if (indexEntry == null)
				return;

			IndexValue indexValue = encryptionHandler.decryptIndexEntry(ec, indexEntry);
			indexValue.removeDataEntryID(dataEntryID);
			if (indexValue.isDataEntryIDsEmpty())
				pmIndex.deletePersistent(indexEntry);
			else
				encryptionHandler.encryptIndexEntry(ec, indexEntry, indexValue);
		}
	}
}