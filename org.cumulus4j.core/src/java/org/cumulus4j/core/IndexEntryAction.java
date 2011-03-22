package org.cumulus4j.core;

import java.lang.reflect.Array;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.FieldMetaRole;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexEntryFactory;
import org.cumulus4j.core.model.IndexEntryFactoryRegistry;
import org.cumulus4j.core.model.IndexEntryObjectRelationHelper;
import org.cumulus4j.core.model.IndexValue;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
import org.datanucleus.store.ExecutionContext;

abstract class IndexEntryAction
{
	protected Cumulus4jPersistenceHandler persistenceHandler;
	protected Cumulus4jStoreManager storeManager;
	protected EncryptionHandler encryptionHandler;
	protected IndexEntryFactoryRegistry indexEntryFactoryRegistry = IndexEntryFactoryRegistry.sharedInstance();

	public IndexEntryAction(Cumulus4jPersistenceHandler persistenceHandler) {
		if (persistenceHandler == null)
			throw new IllegalArgumentException("persistenceHandler == null");

		this.persistenceHandler = persistenceHandler;
		this.storeManager = persistenceHandler.getStoreManager();
		this.encryptionHandler = storeManager.getEncryptionHandler();
	}

	protected abstract IndexEntry getIndexEntry(
			IndexEntryFactory indexEntryFactory, PersistenceManager pm, FieldMeta fieldMeta, Object fieldValue
	);

	protected abstract void _perform(PersistenceManager pm, IndexEntry indexEntry, long dataEntryID);

	public void perform(
			ExecutionContext executionContext, PersistenceManager pm, long dataEntryID,
			FieldMeta fieldMeta, AbstractMemberMetaData dnMemberMetaData,
			Object fieldValue
	)
	{
		int relationType = dnMemberMetaData.getRelationType(executionContext.getClassLoaderResolver());

		if (Relation.NONE == relationType) {
			// The field contains no other persistent entity. It might contain a collection/array/map, though.

			if (dnMemberMetaData.hasCollection() || dnMemberMetaData.hasArray()) {
				FieldMetaRole role;
				if (dnMemberMetaData.hasCollection())
					role = FieldMetaRole.collectionElement;
				else
					role = FieldMetaRole.arrayElement;

				FieldMeta subFieldMeta = fieldMeta.getSubFieldMeta(role);
				IndexEntryFactory indexEntryFactory = indexEntryFactoryRegistry.getIndexEntryFactory(executionContext, subFieldMeta, false);
				for (int idx = 0; idx < Array.getLength(fieldValue); ++idx) {
					Object element = Array.get(fieldValue, idx);
					IndexEntry indexEntry = getIndexEntry(indexEntryFactory, pm, subFieldMeta, element);
					_perform(pm, indexEntry, dataEntryID);
				}
			}
			else if (dnMemberMetaData.hasMap()) {
				Map<?,?> fieldValueMap = (Map<?,?>) fieldValue;

				FieldMeta subFieldMetaKey = fieldMeta.getSubFieldMeta(FieldMetaRole.mapKey);
				FieldMeta subFieldMetaValue = fieldMeta.getSubFieldMeta(FieldMetaRole.mapValue);
				IndexEntryFactory indexEntryFactoryKey = indexEntryFactoryRegistry.getIndexEntryFactory(executionContext, subFieldMetaKey, false);
				IndexEntryFactory indexEntryFactoryValue = indexEntryFactoryRegistry.getIndexEntryFactory(executionContext, subFieldMetaValue, false);

				for (Map.Entry<?, ?> me : fieldValueMap.entrySet()) {
					IndexEntry indexEntryKey = getIndexEntry(indexEntryFactoryKey, pm, subFieldMetaKey, me.getKey());
					_perform(pm, indexEntryKey, dataEntryID);

					IndexEntry indexEntryValue = getIndexEntry(indexEntryFactoryValue, pm, subFieldMetaValue, me.getValue());
					_perform(pm, indexEntryValue, dataEntryID);
				}
			}
			else {
				IndexEntryFactory indexEntryFactory = indexEntryFactoryRegistry.getIndexEntryFactory(executionContext, fieldMeta, false);
				IndexEntry indexEntry = getIndexEntry(indexEntryFactory, pm, fieldMeta, fieldValue);
				_perform(pm, indexEntry, dataEntryID);
			}
		}
		else if (Relation.isRelationSingleValued(relationType)) {
			// 1-1-relationship to another persistence-capable object.
			Long otherDataEntryID = persistenceHandler.getDataEntryIDForObjectID(executionContext, pm, fieldValue);
			IndexEntry indexEntry = IndexEntryObjectRelationHelper.createIndexEntry(pm, fieldMeta, otherDataEntryID);
			_perform(pm, indexEntry, dataEntryID);
		}
		else if (Relation.isRelationMultiValued(relationType)) {
			// map, collection, array
			if (dnMemberMetaData.hasMap()) { // Map.class.isAssignableFrom(dnMemberMetaData.getType())) {
				Map<?,?> fieldValueMap = (Map<?,?>) fieldValue;

				boolean keyIsPersistent = dnMemberMetaData.getMap().keyIsPersistent();
				boolean valueIsPersistent = dnMemberMetaData.getMap().valueIsPersistent();

				FieldMeta subFieldMetaKey = fieldMeta.getSubFieldMeta(FieldMetaRole.mapKey);
				FieldMeta subFieldMetaValue = fieldMeta.getSubFieldMeta(FieldMetaRole.mapValue);
				IndexEntryFactory indexEntryFactoryKey = indexEntryFactoryRegistry.getIndexEntryFactory(executionContext, subFieldMetaKey, false);
				IndexEntryFactory indexEntryFactoryValue = indexEntryFactoryRegistry.getIndexEntryFactory(executionContext, subFieldMetaValue, false);

				for (Map.Entry<?, ?> me : fieldValueMap.entrySet()) {
					if (keyIsPersistent) {
						Long otherDataEntryID = persistenceHandler.getDataEntryIDForObjectID(executionContext, pm, me.getKey());
						IndexEntry indexEntry = IndexEntryObjectRelationHelper.createIndexEntry(pm, subFieldMetaKey, otherDataEntryID);
						_perform(pm, indexEntry, dataEntryID);
					}
					else { // TODO if key is mapped-by, we should better not index it?!
						IndexEntry indexEntry = getIndexEntry(indexEntryFactoryKey, pm, subFieldMetaKey, me.getKey());
						_perform(pm, indexEntry, dataEntryID);
					}

					if (valueIsPersistent) {
						Long otherDataEntryID = persistenceHandler.getDataEntryIDForObjectID(executionContext, pm, me.getValue());
						IndexEntry indexEntry = IndexEntryObjectRelationHelper.createIndexEntry(pm, subFieldMetaValue, otherDataEntryID);
						_perform(pm, indexEntry, dataEntryID);
					}
					else { // TODO if value is mapped-by, we should better not index it?!
						IndexEntry indexEntry = getIndexEntry(indexEntryFactoryValue, pm, subFieldMetaValue, me.getValue());
						_perform(pm, indexEntry, dataEntryID);
					}
				}
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
					Long otherDataEntryID = persistenceHandler.getDataEntryIDForObjectID(executionContext, pm, element);
					IndexEntry indexEntry = IndexEntryObjectRelationHelper.createIndexEntry(pm, subFieldMeta, otherDataEntryID);
					_perform(pm, indexEntry, dataEntryID);
				}
			}
		}
	}

	static class Add extends IndexEntryAction
	{
		public Add(Cumulus4jPersistenceHandler persistenceHandler) {
			super(persistenceHandler);
		}

		@Override
		public IndexEntry getIndexEntry(IndexEntryFactory indexEntryFactory, PersistenceManager pm, FieldMeta fieldMeta, Object fieldValue)
		{
			return indexEntryFactory == null ? null : indexEntryFactory.createIndexEntry(pm, fieldMeta, fieldValue);
		}

		@Override
		protected void _perform(PersistenceManager pm, IndexEntry indexEntry, long dataEntryID)
		{
			if (indexEntry == null)
				return;

			IndexValue indexValue = encryptionHandler.decryptIndexEntry(indexEntry);
			indexValue.addDataEntryID(dataEntryID);
			encryptionHandler.encryptIndexEntry(indexEntry, indexValue);
		}
	}

	static class Remove extends IndexEntryAction
	{
		public Remove(Cumulus4jPersistenceHandler persistenceHandler) {
			super(persistenceHandler);
		}

		@Override
		public IndexEntry getIndexEntry(IndexEntryFactory indexEntryFactory, PersistenceManager pm, FieldMeta fieldMeta, Object fieldValue)
		{
			return indexEntryFactory == null ? null : indexEntryFactory.getIndexEntry(pm, fieldMeta, fieldValue);
		}

		@Override
		protected void _perform(PersistenceManager pm, IndexEntry indexEntry, long dataEntryID)
		{
			if (indexEntry == null)
				return;

			IndexValue indexValue = encryptionHandler.decryptIndexEntry(indexEntry);
			indexValue.removeDataEntryID(dataEntryID);
			if (indexValue.isDataEntryIDsEmpty())
				pm.deletePersistent(indexEntry);
			else
				encryptionHandler.encryptIndexEntry(indexEntry, indexValue);
		}
	}

}
