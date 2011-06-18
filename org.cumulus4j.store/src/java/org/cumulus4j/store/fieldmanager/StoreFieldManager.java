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
package org.cumulus4j.store.fieldmanager;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.ObjectContainerHelper;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.ObjectContainer;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.fieldmanager.AbstractFieldManager;
import org.datanucleus.store.types.sco.SCO;
import org.datanucleus.store.types.sco.SCOUtils;

/**
 * Manager for the process of persisting a user object into the datastore, handling the translation from the
 * users own object into the DataEntry object.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class StoreFieldManager extends AbstractFieldManager
{
	private ObjectProvider op;
	private PersistenceManager pmData;
	private ExecutionContext ec;
	private ClassMeta classMeta;
	private AbstractClassMetaData dnClassMetaData;
	private ObjectContainer objectContainer;

	public StoreFieldManager(
			ObjectProvider op,
			PersistenceManager pmData,
			ClassMeta classMeta,
			AbstractClassMetaData dnClassMetaData,
			ObjectContainer objectContainer // populated by this class
	)
	{
		this.op = op;
		this.pmData = pmData;
		this.ec = op.getExecutionContext();
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
	public void storeBooleanField(int fieldNumber, boolean value) {
		objectContainer.setValue(getFieldID(fieldNumber), value);
	}

	@Override
	public void storeByteField(int fieldNumber, byte value) {
		objectContainer.setValue(getFieldID(fieldNumber), value);
	}

	@Override
	public void storeCharField(int fieldNumber, char value) {
		objectContainer.setValue(getFieldID(fieldNumber), value);
	}

	@Override
	public void storeDoubleField(int fieldNumber, double value) {
		objectContainer.setValue(getFieldID(fieldNumber), value);
	}

	@Override
	public void storeFloatField(int fieldNumber, float value) {
		objectContainer.setValue(getFieldID(fieldNumber), value);
	}

	@Override
	public void storeIntField(int fieldNumber, int value) {
		objectContainer.setValue(getFieldID(fieldNumber), value);
	}

	@Override
	public void storeLongField(int fieldNumber, long value) {
		objectContainer.setValue(getFieldID(fieldNumber), value);
	}

	@Override
	public void storeShortField(int fieldNumber, short value) {
		objectContainer.setValue(getFieldID(fieldNumber), value);
	}

	@Override
	public void storeStringField(int fieldNumber, String value) {
		objectContainer.setValue(getFieldID(fieldNumber), value);
	}

	@Override
	public void storeObjectField(int fieldNumber, Object value)
	{
		AbstractMemberMetaData mmd = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);

		FieldMeta fieldMeta = classMeta.getFieldMeta(mmd.getClassName(), mmd.getName());
		if (fieldMeta == null)
			throw new IllegalStateException("Unknown field! class=" + dnClassMetaData.getFullClassName() + " fieldNumber=" + fieldNumber + " fieldName=" + mmd.getName());

		if (value == null) {
			objectContainer.setValue(fieldMeta.getFieldID(), null);
			return;
		}

		int relationType = mmd.getRelationType(ec.getClassLoaderResolver());

		// Replace any SCO field that isn't already a wrapper, with its wrapper object
		boolean[] secondClassMutableFieldFlags = dnClassMetaData.getSCOMutableMemberFlags();
		if (secondClassMutableFieldFlags[fieldNumber] && !(value instanceof SCO))
			value = op.wrapSCOField(fieldNumber, value, false, true, true);

		if (relationType == Relation.NONE)
		{
			if (mmd.hasCollection()) {
				// Replace the special DN collection by a simple array.
				Collection<?> collection = (Collection<?>)value;
				Object[] values = collection.toArray(new Object[collection.size()]);
				objectContainer.setValue(fieldMeta.getFieldID(), values);
			}
			else if (mmd.hasMap()) {
				// replace the special DN Map by a simple HashMap.
				Map<?,?> valueMap = (Map<?, ?>) value;

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

				map.putAll(valueMap);

				objectContainer.setValue(fieldMeta.getFieldID(), map);
			}
			else // arrays are not managed (no special DN instances) and thus stored 'as is'...
				objectContainer.setValue(fieldMeta.getFieldID(), value);
		}
		else if (Relation.isRelationSingleValued(relationType))
		{
			// Persistable object - persist the related object and store the identity in the cell
			Object valuePC = ec.persistObjectInternal(value, op, fieldNumber, -1);
			if (mmd.getMappedBy() == null) {
				Object valueID = ObjectContainerHelper.entityToReference(ec, pmData, valuePC);
				objectContainer.setValue(fieldMeta.getFieldID(), valueID);
			}
		}
		else if (Relation.isRelationMultiValued(relationType))
		{
			// Collection/Map/Array
			if (mmd.hasCollection())
			{
				Collection<?> collection = (Collection<?>)value;
				Object[] ids = mmd.getMappedBy() != null ? null : new Object[collection.size()];
				int idx = -1;
				for (Object element : collection) {
					Object elementPC = ec.persistObjectInternal(element, op, fieldNumber, -1);
					if (ids != null) {
						Object elementID = ObjectContainerHelper.entityToReference(ec, pmData, elementPC);
						ids[++idx] = elementID;
					}
				}

				if (ids != null)
					objectContainer.setValue(fieldMeta.getFieldID(), ids);
			}
			else if (mmd.hasMap())
			{
				boolean keyIsPersistent = mmd.getMap().keyIsPersistent();
				boolean valueIsPersistent = mmd.getMap().valueIsPersistent();

				Map<?,?> valueMap = (Map<?, ?>) value;
				Map<Object,Object> idMap = mmd.getMappedBy() != null ? null : new HashMap<Object, Object>(valueMap.size());
				for (Map.Entry<?, ?> me : valueMap.entrySet()) {
					Object k = me.getKey();
					Object v = me.getValue();

					if (keyIsPersistent) {
						Object kpc = ec.persistObjectInternal(k, op, fieldNumber, -1);

						if (idMap != null)
							k = ObjectContainerHelper.entityToReference(ec, pmData, kpc);
					}

					if (valueIsPersistent) {
						Object vpc = ec.persistObjectInternal(v, op, fieldNumber, -1);

						if (idMap != null)
							v = ObjectContainerHelper.entityToReference(ec, pmData, vpc);
					}

					if (idMap != null)
						idMap.put(k, v);
				}

				if (idMap != null)
					objectContainer.setValue(fieldMeta.getFieldID(), idMap);
			}
			else if (mmd.hasArray())
			{
				if (mmd.getMappedBy() != null)
					throw new UnsupportedOperationException("NYI");

				Object[] ids = new Object[Array.getLength(value)];
				for (int i=0;i<Array.getLength(value);i++)
				{
					Object element = Array.get(value, i);
					Object elementPC = ec.persistObjectInternal(element, op, fieldNumber, -1);
					Object elementID = ObjectContainerHelper.entityToReference(ec, pmData, elementPC);
					ids[i] = elementID;
				}
				objectContainer.setValue(fieldMeta.getFieldID(), ids);
			}
		}
		else
			throw new IllegalStateException("Unexpected relationType: " + relationType);
	}

}
