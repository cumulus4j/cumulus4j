package org.cumulus4j.core;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.ObjectContainer;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.fieldmanager.AbstractFieldManager;
import org.datanucleus.store.types.sco.SCOUtils;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class FetchFieldManager extends AbstractFieldManager
{
	private ObjectProvider op;
	private ExecutionContext executionContext;
	private ClassMeta classMeta;
	private AbstractClassMetaData dnClassMetaData;
	private ObjectContainer objectContainer;

	public FetchFieldManager(
			ObjectProvider op,
			ClassMeta classMeta,
			AbstractClassMetaData dnClassMetaData,
			ObjectContainer objectContainer
	)
	{
		this.op = op;
		this.executionContext = op.getExecutionContext();
		this.classMeta = classMeta;
		this.dnClassMetaData = dnClassMetaData;
		this.objectContainer = objectContainer;
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

	@Override
	public Object fetchObjectField(int fieldNumber)
	{
		AbstractMemberMetaData mmd = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
		FieldMeta fieldMeta = classMeta.getFieldMeta(mmd.getClassName(), mmd.getName());
		if (fieldMeta == null)
			throw new IllegalStateException("Unknown field! class=" + dnClassMetaData.getFullClassName() + " fieldNumber=" + fieldNumber + " fieldName=" + mmd.getName());

		if (mmd.getMappedBy() != null)
			throw new UnsupportedOperationException("NYI"); // TODO we have to look the value up in a different way than reading it directly from our ObjectContainer!

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
			Object valueID = objectContainer.getValue(fieldMeta.getFieldID());
			if (valueID == null)
				return null;

			return executionContext.findObject(valueID, true, true, null);
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
					Object element = executionContext.findObject(id, true, true, null);
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
						k = executionContext.findObject(k, true, true, null);
					}

					if (valueIsPersistent) {
						v = executionContext.findObject(v, true, true, null);
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
					Object element = executionContext.findObject(id, true, true, null);
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
