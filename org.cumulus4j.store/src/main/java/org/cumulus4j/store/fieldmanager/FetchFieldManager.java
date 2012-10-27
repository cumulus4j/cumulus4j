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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jdo.PersistenceManager;
import javax.jdo.spi.JDOImplHelper;
import javax.jdo.spi.PersistenceCapable;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.cumulus4j.store.EncryptionHandler;
import org.cumulus4j.store.ObjectContainerHelper;
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntryDAO;
import org.cumulus4j.store.model.EmbeddedObjectContainer;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.FieldMetaRole;
import org.cumulus4j.store.model.IndexEntry;
import org.cumulus4j.store.model.IndexEntryObjectRelationHelper;
import org.cumulus4j.store.model.IndexValue;
import org.cumulus4j.store.model.ObjectContainer;
import org.datanucleus.exceptions.NucleusDataStoreException;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
import org.datanucleus.state.ObjectProviderFactory;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.fieldmanager.AbstractFieldManager;
import org.datanucleus.store.types.sco.SCOUtils;

/**
 * Manager for the process of fetching a user object from the datastore, handling the translation from the
 * DataEntry object into the users own object.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class FetchFieldManager extends AbstractFieldManager
{
	private ObjectProvider op;
	private CryptoContext cryptoContext;
	private PersistenceManager pmData;
	private PersistenceManager pmIndex;
	private ExecutionContext ec;
	private ClassMeta classMeta;
	private AbstractClassMetaData dnClassMetaData;
	private ObjectContainer objectContainer;

	public FetchFieldManager(
			ObjectProvider op,
			CryptoContext cryptoContext,
			ClassMeta classMeta,
			AbstractClassMetaData dnClassMetaData,
			ObjectContainer objectContainer
	)
	{
		if (op == null)
			throw new IllegalArgumentException("op == null");
		if (cryptoContext == null)
			throw new IllegalArgumentException("cryptoContext == null");
		if (classMeta == null)
			throw new IllegalArgumentException("classMeta == null");
		if (dnClassMetaData == null)
			throw new IllegalArgumentException("dnClassMetaData == null");
		if (objectContainer == null)
			throw new IllegalArgumentException("objectContainer == null");

		this.op = op;
		this.cryptoContext = cryptoContext;
		this.pmData = cryptoContext.getPersistenceManagerForData();
		this.pmIndex = cryptoContext.getPersistenceManagerForIndex();
		this.ec = op.getExecutionContext();
		this.classMeta = classMeta;
		this.dnClassMetaData = dnClassMetaData;
		this.objectContainer = objectContainer;
	}

	protected EncryptionHandler getEncryptionHandler()
	{
		return ((Cumulus4jStoreManager) ec.getStoreManager()).getEncryptionHandler();
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
			thisDataEntryID = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntryID(classMeta, op.getObjectId().toString());

		return thisDataEntryID;
	}

	@Override
	public Object fetchObjectField(int fieldNumber)
	{ // TODO refactor this *too* *long* method! split into multiple! Marco :-)
		AbstractMemberMetaData mmd = dnClassMetaData.getMetaDataForManagedMemberAtAbsolutePosition(fieldNumber);
		FieldMeta fieldMeta = classMeta.getFieldMeta(mmd.getClassName(), mmd.getName());
		if (fieldMeta == null)
			throw new IllegalStateException("Unknown field! class=" + dnClassMetaData.getFullClassName() + " fieldNumber=" + fieldNumber + " fieldName=" + mmd.getName());

		Set<Long> mappedByDataEntryIDs = null;
		if (mmd.getMappedBy() != null) {
			IndexEntry indexEntry = IndexEntryObjectRelationHelper.getIndexEntry(cryptoContext, pmIndex, fieldMeta.getMappedByFieldMeta(ec), getThisDataEntryID());
			if (indexEntry == null)
				mappedByDataEntryIDs = Collections.emptySet();
			else {
				IndexValue indexValue = getEncryptionHandler().decryptIndexEntry(cryptoContext, indexEntry);
				mappedByDataEntryIDs = indexValue.getDataEntryIDs();
			}
		}

		int relationType = mmd.getRelationType(ec.getClassLoaderResolver());

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
				if (array != null) {
					for (int idx = 0; idx < Array.getLength(array); ++idx) {
						Object element = Array.get(array, idx);
						collection.add(element);
					}
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
			if (mmd.isEmbedded()) {
				Object value = objectContainer.getValue(fieldMeta.getFieldID());
				if (value == null)
					return null;

				if (!(value instanceof EmbeddedObjectContainer))
					throw new IllegalStateException("field claims to be embedded, but persistent field value is not an EmbeddedObjectContainer! fieldID=" + fieldMeta.getFieldID() + " fieldName=" + fieldMeta.getFieldName());

				EmbeddedObjectContainer embeddedObjectContainer = (EmbeddedObjectContainer) value;

				// TODO the newest DN version has a StateManagerFactory that makes the following more convenient (I think) - maybe switch later?!
				ClassMeta embeddedClassMeta = ((Cumulus4jStoreManager)ec.getStoreManager()).getClassMeta(ec, embeddedObjectContainer.getClassID(), true);
				Class<?> embeddedClass = ec.getClassLoaderResolver().classForName(embeddedClassMeta.getClassName());

				AbstractClassMetaData embeddedDNClassMeta = embeddedClassMeta.getDataNucleusClassMetaData(ec);
				PersistenceCapable pc = JDOImplHelper.getInstance().newInstance(embeddedClass, null);

				ObjectProvider embeddedOP = ObjectProviderFactory.newForEmbedded(ec, pc, false, op, fieldNumber);
				embeddedOP.replaceFields(
						embeddedDNClassMeta.getAllMemberPositions(),
						new FetchFieldManager(embeddedOP, cryptoContext, embeddedClassMeta, embeddedDNClassMeta, embeddedObjectContainer)
				);
				return embeddedOP.getObject();
			}
			else {
				if (mmd.getMappedBy() != null) {
					if (mappedByDataEntryIDs.isEmpty())
						return null;

					if (mappedByDataEntryIDs.size() != 1)
						throw new IllegalStateException("There are multiple objects referencing a 1-1-mapped-by-relationship! Expected 0 or 1! fieldMeta=" + fieldMeta + " dataEntryIDsForMappedBy=" + mappedByDataEntryIDs);

					long dataEntryID = mappedByDataEntryIDs.iterator().next();
					return getObjectFromDataEntryID(dataEntryID);
				}

				Object valueID = objectContainer.getValue(fieldMeta.getFieldID());
				return ObjectContainerHelper.referenceToEntity(cryptoContext, pmData, valueID);
			}
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

				if (mmd.getMappedBy() != null) {
					for (Long mappedByDataEntryID : mappedByDataEntryIDs) {
						Object element = getObjectFromDataEntryID(mappedByDataEntryID);
						collection.add(element);
					}
				}
				else {
					Object ids = objectContainer.getValue(fieldMeta.getFieldID());
					if (ids != null) {
						for (int idx = 0; idx < Array.getLength(ids); ++idx) {
							Object id = Array.get(ids, idx);
							Object element = ObjectContainerHelper.referenceToEntity(cryptoContext, pmData, id);
							if (element != null) // orphaned reference - https://sourceforge.net/tracker/?func=detail&aid=3515529&group_id=517465&atid=2102914
								collection.add(element);
						}
					}
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

				if (mmd.getMappedBy() != null) {
					FieldMeta oppositeFieldMetaKey = fieldMeta.getSubFieldMeta(FieldMetaRole.mapKey).getMappedByFieldMeta(ec);
					FieldMeta oppositeFieldMetaValue = fieldMeta.getSubFieldMeta(FieldMetaRole.mapValue).getMappedByFieldMeta(ec);

					for (Long mappedByDataEntryID : mappedByDataEntryIDs) {
						Object element = getObjectFromDataEntryID(mappedByDataEntryID);
						ObjectProvider elementOP = ec.findObjectProvider(element);
						if (elementOP == null)
							throw new IllegalStateException("executionContext.findObjectProvider(element) returned null for " + element);

						Object key;
						if (keyIsPersistent)
							key = element;
						else
							key = elementOP.provideField(oppositeFieldMetaKey.getDataNucleusAbsoluteFieldNumber());

						Object value;
						if (valueIsPersistent)
							value = element;
						else
							value = elementOP.provideField(oppositeFieldMetaValue.getDataNucleusAbsoluteFieldNumber());

						map.put(key, value);
					}
				}
				else {
					Map<?,?> idMap = (Map<?,?>) objectContainer.getValue(fieldMeta.getFieldID());
					if (idMap != null) {
						for (Map.Entry<?, ?> me : idMap.entrySet()) {
							Object k = me.getKey();
							Object v = me.getValue();

							if (keyIsPersistent) {
								k = ObjectContainerHelper.referenceToEntity(cryptoContext, pmData, k);
								if (k == null) // orphaned reference - https://sourceforge.net/tracker/?func=detail&aid=3515529&group_id=517465&atid=2102914
									continue;
							}

							if (valueIsPersistent) {
								v = ObjectContainerHelper.referenceToEntity(cryptoContext, pmData, v);
								if (v == null) // orphaned reference - https://sourceforge.net/tracker/?func=detail&aid=3515529&group_id=517465&atid=2102914
									continue;
							}

							map.put(k, v);
						}
					}
				}

				return op.wrapSCOField(fieldNumber, map, false, false, true);
			}
			else if (mmd.hasArray())
			{
				Class<?> elementType = ec.getClassLoaderResolver().classForName(mmd.getArray().getElementType());

				Object array;
				if (mmd.getMappedBy() != null) {
					int arrayLength = mappedByDataEntryIDs.size();
					array = Array.newInstance(elementType, arrayLength);
					Iterator<Long> it = mappedByDataEntryIDs.iterator();
					for (int i = 0; i < arrayLength; ++i) {
						Long dataEntryID = it.next();
						Object element = getObjectFromDataEntryID(dataEntryID);
						Array.set(array, i, element);
					}
				}
				else {
					Object ids = objectContainer.getValue(fieldMeta.getFieldID());
					if (ids == null)
						array = null;
					else {
						if (ec.getStoreManager().getPersistenceHandler().useReferentialIntegrity()) {
							// Directly fill the array.
							int arrayLength = Array.getLength(ids);
							array = Array.newInstance(elementType, arrayLength);
							for (int i = 0; i < arrayLength; ++i) {
								Object id = Array.get(ids, i);
								Object element = ObjectContainerHelper.referenceToEntity(cryptoContext, pmData, id);
								Array.set(array, i, element);
							}
						}
						else {
							// https://sourceforge.net/tracker/?func=detail&aid=3515529&group_id=517465&atid=2102914
							// First fill a list and then transfer everything into an array, because there might
							// be elements missing (orphaned references).
							int arrayLength = Array.getLength(ids);
							ArrayList<Object> tmpList = new ArrayList<Object>();
							for (int i = 0; i < arrayLength; ++i) {
								Object id = Array.get(ids, i);
								Object element = ObjectContainerHelper.referenceToEntity(cryptoContext, pmData, id);
								if (element != null)
									tmpList.add(element);
							}
							array = Array.newInstance(elementType, tmpList.size());
							array = tmpList.toArray((Object[]) array);
						}
					}
				}
				return array;
			}
			else
				throw new IllegalStateException("Unexpected multi-valued relationType: " + relationType);
		}
		else
			throw new IllegalStateException("Unexpected relationType: " + relationType);
	}

	private Object getObjectFromDataEntryID(long dataEntryID)
	{
		String idStr = new DataEntryDAO(pmData, cryptoContext.getKeyStoreRefID()).getDataEntry(dataEntryID).getObjectID();
		return IdentityUtils.getObjectFromIdString(
				idStr, classMeta.getDataNucleusClassMetaData(ec), ec, true
		);
	}
}
