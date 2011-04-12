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

	protected abstract IndexEntry getIndexEntryForObjectRelation(PersistenceManager pm, FieldMeta fieldMeta, Long otherDataEntryID);

	protected abstract void _perform(ExecutionContext executionContext, PersistenceManager pm, IndexEntry indexEntry, long dataEntryID);

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
					_perform(executionContext, pm, indexEntry, dataEntryID);
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
					_perform(executionContext, pm, indexEntryKey, dataEntryID);

					IndexEntry indexEntryValue = getIndexEntry(indexEntryFactoryValue, pm, subFieldMetaValue, me.getValue());
					_perform(executionContext, pm, indexEntryValue, dataEntryID);
				}
			}
			else {
				IndexEntryFactory indexEntryFactory = indexEntryFactoryRegistry.getIndexEntryFactory(executionContext, fieldMeta, false);
				IndexEntry indexEntry = getIndexEntry(indexEntryFactory, pm, fieldMeta, fieldValue);
				_perform(executionContext, pm, indexEntry, dataEntryID);
			}
		}
		else if (Relation.isRelationSingleValued(relationType)) {
			// 1-1-relationship to another persistence-capable object.
			Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(executionContext, pm, fieldValue);
			IndexEntry indexEntry = getIndexEntryForObjectRelation(pm, fieldMeta, otherDataEntryID);
			_perform(executionContext, pm, indexEntry, dataEntryID);
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
						Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(executionContext, pm, me.getKey());
						IndexEntry indexEntry = getIndexEntryForObjectRelation(pm, subFieldMetaKey, otherDataEntryID);
						_perform(executionContext, pm, indexEntry, dataEntryID);
					}
					else {
						IndexEntry indexEntry = getIndexEntry(indexEntryFactoryKey, pm, subFieldMetaKey, me.getKey());
						_perform(executionContext, pm, indexEntry, dataEntryID);
					}

					if (valueIsPersistent) {
						Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(executionContext, pm, me.getValue());
						IndexEntry indexEntry = getIndexEntryForObjectRelation(pm, subFieldMetaValue, otherDataEntryID);
						_perform(executionContext, pm, indexEntry, dataEntryID);
					}
					else {
						IndexEntry indexEntry = getIndexEntry(indexEntryFactoryValue, pm, subFieldMetaValue, me.getValue());
						_perform(executionContext, pm, indexEntry, dataEntryID);
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
					Long otherDataEntryID = ObjectContainerHelper.referenceToDataEntryID(executionContext, pm, element);
					IndexEntry indexEntry = getIndexEntryForObjectRelation(pm, subFieldMeta, otherDataEntryID);
					_perform(executionContext, pm, indexEntry, dataEntryID);
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
		public IndexEntry getIndexEntryForObjectRelation(PersistenceManager pm, FieldMeta fieldMeta, Long otherDataEntryID) {
			return IndexEntryObjectRelationHelper.createIndexEntry(pm, fieldMeta, otherDataEntryID);
		}

		@Override
		protected void _perform(ExecutionContext executionContext, PersistenceManager pm, IndexEntry indexEntry, long dataEntryID)
		{
			if (indexEntry == null)
				return;

			IndexValue indexValue = encryptionHandler.decryptIndexEntry(executionContext, indexEntry);
			indexValue.addDataEntryID(dataEntryID);
			encryptionHandler.encryptIndexEntry(executionContext, indexEntry, indexValue);
			pm.makePersistent(indexEntry); // We do not persist directly when creating anymore, thus we must persist here. This is a no-op, if it's already persistent.
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
		protected IndexEntry getIndexEntryForObjectRelation(PersistenceManager pm, FieldMeta fieldMeta, Long otherDataEntryID) {
			return IndexEntryObjectRelationHelper.getIndexEntry(pm, fieldMeta, otherDataEntryID);
		}

		@Override
		protected void _perform(ExecutionContext executionContext, PersistenceManager pm, IndexEntry indexEntry, long dataEntryID)
		{
			if (indexEntry == null)
				return;

			IndexValue indexValue = encryptionHandler.decryptIndexEntry(executionContext, indexEntry);
			indexValue.removeDataEntryID(dataEntryID);
			if (indexValue.isDataEntryIDsEmpty())
				pm.deletePersistent(indexEntry);
			else
				encryptionHandler.encryptIndexEntry(executionContext, indexEntry, indexValue);
		}
	}

}
