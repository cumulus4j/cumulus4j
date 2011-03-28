package org.cumulus4j.core;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.IndexEntry;
import org.cumulus4j.core.model.IndexEntryObjectRelationHelper;
import org.cumulus4j.core.model.IndexValue;
import org.cumulus4j.core.model.ObjectContainer;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.fieldmanager.AbstractFieldManager;
import org.datanucleus.store.types.sco.SCOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class FetchFieldManager extends AbstractFieldManager
{
	private static final Logger logger = LoggerFactory.getLogger(FetchFieldManager.class);

	private ObjectProvider op;
	private PersistenceManager pm;
	private ExecutionContext executionContext;
	private ClassMeta classMeta;
	private AbstractClassMetaData dnClassMetaData;
	private ObjectContainer objectContainer;

	public FetchFieldManager(
			ObjectProvider op,
			PersistenceManager pm,
			ClassMeta classMeta,
			AbstractClassMetaData dnClassMetaData,
			ObjectContainer objectContainer
	)
	{
		this.op = op;
		this.pm = pm;
		this.executionContext = op.getExecutionContext();
		this.classMeta = classMeta;
		this.dnClassMetaData = dnClassMetaData;
		this.objectContainer = objectContainer;
	}

	protected EncryptionHandler getEncryptionHandler()
	{
		return ((Cumulus4jStoreManager) executionContext.getStoreManager()).getEncryptionHandler();
	}

	private long getFieldID(int fieldNumber)
	{
		AbstractMemberMetaData mmd = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);

		FieldMeta fieldMeta = classMeta.getFieldMeta(mmd.getClassName(), mmd.getName());
		if (fieldMeta == null)
			throw new IllegalStateException("Unknown field! class=" + dnClassMetaData.getFullClassName() + " fieldNumber=" + fieldNumber + " fieldName=" + mmd.getName());

		return fieldMeta.getFieldID();
	}

	@Override
	public boolean fetchBooleanField(int fieldNumber) {
		Object value = objectContainer.getValue(getFieldID(fieldNumber));
		return value == null ? false : (Boolean)value;
	}

	@Override
	public byte fetchByteField(int fieldNumber) {
		Object value = objectContainer.getValue(getFieldID(fieldNumber));
		return value == null ? 0 : (Byte)value;
	}

	@Override
	public char fetchCharField(int fieldNumber) {
		Object value = objectContainer.getValue(getFieldID(fieldNumber));
		return value == null ? 0 : (Character)value;
	}

	@Override
	public double fetchDoubleField(int fieldNumber) {
		Object value = objectContainer.getValue(getFieldID(fieldNumber));
		return value == null ? 0 : (Double)value;
	}

	@Override
	public float fetchFloatField(int fieldNumber) {
		Object value = objectContainer.getValue(getFieldID(fieldNumber));
		return value == null ? 0 : (Float)value;
	}

	@Override
	public int fetchIntField(int fieldNumber) {
		Object value = objectContainer.getValue(getFieldID(fieldNumber));
		return value == null ? 0 : (Integer)value;
	}

	@Override
	public long fetchLongField(int fieldNumber) {
		Object value = objectContainer.getValue(getFieldID(fieldNumber));
		return value == null ? 0 : (Long)value;
	}

	@Override
	public short fetchShortField(int fieldNumber) {
		Object value = objectContainer.getValue(getFieldID(fieldNumber));
		return value == null ? 0 : (Short)value;
	}

	@Override
	public String fetchStringField(int fieldNumber) {
		Object value = objectContainer.getValue(getFieldID(fieldNumber));
		return (String)value;
	}

	private long thisDataEntryID = -1;

	protected long getThisDataEntryID()
	{
		if (thisDataEntryID < 0)
			thisDataEntryID = DataEntry.getDataEntryID(pm, classMeta, op.getObjectId().toString());

		return thisDataEntryID;
	}

	@Override
	public Object fetchObjectField(int fieldNumber)
	{
		AbstractMemberMetaData mmd = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
		FieldMeta fieldMeta = classMeta.getFieldMeta(mmd.getClassName(), mmd.getName());
		if (fieldMeta == null)
			throw new IllegalStateException("Unknown field! class=" + dnClassMetaData.getFullClassName() + " fieldNumber=" + fieldNumber + " fieldName=" + mmd.getName());

		if (mmd.getMappedBy() != null) { // TODO remove this when all mapped-by-situations are supported!
			logger.warn("fetchObjectField: fieldNumber=" + fieldNumber + ": NYI", new UnsupportedOperationException("NYI"));
		}

		int relationType = mmd.getRelationType(executionContext.getClassLoaderResolver());

		if (relationType == Relation.NONE)
		{
			if (mmd.hasCollection())
			{
				Collection<Object> collection;
				@SuppressWarnings("unchecked")
				Class<? extends Collection<Object>> instanceType = SCOUtils.getContainerInstanceType(mmd.getType(), mmd.getOrderMetaData() != null);
				try {
					collection = instanceType.newInstance();
				} catch (InstantiationException e) {
					throw new NucleusDataStoreException(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					throw new NucleusDataStoreException(e.getMessage(), e);
				}

				Object array = objectContainer.getValue(fieldMeta.getFieldID());
				for (int idx = 0; idx < Array.getLength(array); ++idx) {
					Object element = Array.get(array, idx);
					collection.add(element);
				}
				return op.wrapSCOField(fieldNumber, collection, false, false, true);
			}

			if (mmd.hasMap())
			{
				Map<?,?> map = (Map<?,?>) objectContainer.getValue(fieldMeta.getFieldID());
				return op.wrapSCOField(fieldNumber, map, false, false, true);
			}

			// Arrays are stored 'as is', thus no conversion necessary.
			return objectContainer.getValue(getFieldID(fieldNumber));
		}
		else if (Relation.isRelationSingleValued(relationType))
		{
			if (mmd.getMappedBy() != null) {
				IndexEntry indexEntry = IndexEntryObjectRelationHelper.getIndexEntry(pm, fieldMeta.getMappedByFieldMeta(executionContext), getThisDataEntryID());
				if (indexEntry == null)
					return null;

				IndexValue indexValue = getEncryptionHandler().decryptIndexEntry(indexEntry);
				if (indexValue.getDataEntryIDs().isEmpty())
					return null;

				if (indexValue.getDataEntryIDs().size() != 1)
					throw new IllegalStateException("There are multiple objects referencing a 1-1-mapped-by-relationship! Expected 0 or 1! fieldMeta=" + fieldMeta + " dataEntryIDsForMappedBy=" + indexValue.getDataEntryIDs());

				long dataEntryID = indexValue.getDataEntryIDs().iterator().next();
				String idStr = DataEntry.getDataEntry(pm, dataEntryID).getObjectID();
				return IdentityUtils.getObjectFromIdString(idStr, classMeta.getDataNucleusClassMetaData(executionContext), executionContext, true);
			}

			Object valueID = objectContainer.getValue(fieldMeta.getFieldID());
			return ObjectContainerHelper.referenceToEntity(executionContext, pm, valueID);
		}
		else if (Relation.isRelationMultiValued(relationType))
		{
			// Collection/Map/Array
			if (mmd.hasCollection())
			{
				Collection<Object> collection;
				@SuppressWarnings("unchecked")
				Class<? extends Collection<Object>> instanceType = SCOUtils.getContainerInstanceType(mmd.getType(), mmd.getOrderMetaData() != null);
				try {
					collection = instanceType.newInstance();
				} catch (InstantiationException e) {
					throw new NucleusDataStoreException(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					throw new NucleusDataStoreException(e.getMessage(), e);
				}

				Object ids = objectContainer.getValue(fieldMeta.getFieldID());
				for (int idx = 0; idx < Array.getLength(ids); ++idx) {
					Object id = Array.get(ids, idx);
					Object element = executionContext.findObject(id, true, true, null); // TODO
					collection.add(element);
				}
				return op.wrapSCOField(fieldNumber, collection, false, false, true);
			}
			else if (mmd.hasMap())
			{
				Map<Object, Object> map;
				@SuppressWarnings("unchecked")
				Class<? extends Map<Object, Object>> instanceType = SCOUtils.getContainerInstanceType(mmd.getType(), mmd.getOrderMetaData() != null);
				try {
					map = instanceType.newInstance();
				} catch (InstantiationException e) {
					throw new NucleusDataStoreException(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					throw new NucleusDataStoreException(e.getMessage(), e);
				}

				boolean keyIsPersistent = mmd.getMap().keyIsPersistent();
				boolean valueIsPersistent = mmd.getMap().valueIsPersistent();

				Map<?,?> idMap = (Map<?,?>) objectContainer.getValue(fieldMeta.getFieldID());
				for (Map.Entry<?, ?> me : idMap.entrySet()) {
					Object k = me.getKey();
					Object v = me.getValue();

					if (keyIsPersistent) {
						k = executionContext.findObject(k, true, true, null); // TODO
					}

					if (valueIsPersistent) {
						v = executionContext.findObject(v, true, true, null); // TODO
					}

					map.put(k, v);
				}
				return op.wrapSCOField(fieldNumber, map, false, false, true);
			}
			else if (mmd.hasArray())
			{
				Object array;
				try {
					array = mmd.getType().newInstance();
				} catch (InstantiationException e) {
					throw new NucleusDataStoreException(e.getMessage(), e);
				} catch (IllegalAccessException e) {
					throw new NucleusDataStoreException(e.getMessage(), e);
				}

				Object ids = objectContainer.getValue(fieldMeta.getFieldID());
				for (int i = 0; i < Array.getLength(ids); ++i) {
					Object id = Array.get(ids, i);
					Object element = executionContext.findObject(id, true, true, null); // TODO
					Array.set(array, i, element);
				}
				return array;
			}
			else
				throw new IllegalStateException("Unexpected multi-valued relationType: " + relationType);
		}
		else
			throw new IllegalStateException("Unexpected relationType: " + relationType);
	}
}
