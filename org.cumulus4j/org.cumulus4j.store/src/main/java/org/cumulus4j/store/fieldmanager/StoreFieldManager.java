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
import org.cumulus4j.store.crypto.CryptoContext;
import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.EmbeddedClassMeta;
import org.cumulus4j.store.model.EmbeddedObjectContainer;
import org.cumulus4j.store.model.FieldMeta;
import org.cumulus4j.store.model.FieldMetaRole;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manager for the process of persisting a user object into the datastore, handling the translation from the
 * users own object into the DataEntry object.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class StoreFieldManager extends AbstractFieldManager
{
	private static final Logger logger = LoggerFactory.getLogger(StoreFieldManager.class);

	private ObjectProvider op;
	private CryptoContext cryptoContext;
	private PersistenceManager pmData;
	private ExecutionContext ec;
	private ClassMeta classMeta;
	private AbstractClassMetaData dnClassMetaData;
	private ObjectContainer objectContainer;

	public StoreFieldManager(
			ObjectProvider op,
			CryptoContext cryptoContext,
			PersistenceManager pmData,
			ClassMeta classMeta,
			AbstractClassMetaData dnClassMetaData,
			int keyStoreRefID,
			ObjectContainer objectContainer // populated by this class
	)
	{
		if (op == null)
			throw new IllegalArgumentException("op == null");
		if (cryptoContext == null)
			throw new IllegalArgumentException("cryptoContext == null");
		if (pmData == null)
			throw new IllegalArgumentException("pmData == null");
		if (classMeta == null)
			throw new IllegalArgumentException("classMeta == null");
		if (dnClassMetaData == null)
			throw new IllegalArgumentException("dnClassMetaData == null");
		if (objectContainer == null)
			throw new IllegalArgumentException("objectContainer == null");

		this.op = op;
		this.cryptoContext = cryptoContext;
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
		if (logger.isTraceEnabled()) {
			logger.trace(
					"storeObjectField: classMeta.className={} fieldNumber={} value={}",
					new Object[] { classMeta.getClassName(), fieldNumber, value }
			);
		}

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
			storeObjectFieldWithRelationTypeNone(fieldNumber, value, mmd, fieldMeta);
		else if (Relation.isRelationSingleValued(relationType))
			storeObjectFieldWithRelationTypeSingleValue(fieldNumber, value, mmd, fieldMeta);
		else if (Relation.isRelationMultiValued(relationType))
		{
			// Collection/Map/Array
			if (mmd.hasCollection())
				storeObjectFieldWithRelationTypeCollection(fieldNumber, value, mmd, fieldMeta);
			else if (mmd.hasMap())
				storeObjectFieldWithRelationTypeMap(fieldNumber, value, mmd, fieldMeta);
			else if (mmd.hasArray())
				storeObjectFieldWithRelationTypeArray(fieldNumber, value, mmd, fieldMeta);
			else
				throw new IllegalStateException("Unexpected 1-n-sub-type for relationType: " + relationType);
		}
		else
			throw new IllegalStateException("Unexpected relationType: " + relationType);
	}

	/**
	 * Store related objects that are not persistence-capable.
	 * The related objects might be single-valued, arrays, collections or maps.
	 */
	protected void storeObjectFieldWithRelationTypeNone(int fieldNumber, Object value, AbstractMemberMetaData mmd, FieldMeta fieldMeta) {
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

	protected EmbeddedObjectContainer createEmbeddedObjectContainerFromPC(FieldMeta fieldMeta, EmbeddedClassMeta embeddedClassMeta, Object pc) {
		if (pc == null)
			return null;

		ObjectProvider embeddedOP = ec.findObjectProvider(pc);
//		EmbeddedObjectContainer embeddedObjectContainer = (EmbeddedObjectContainer) embeddedOP.getAssociatedValue(EmbeddedObjectContainer.ASSOCIATED_VALUE);
//		if (embeddedObjectContainer == null) { // We must do this ALWAYS, because otherwise changes are ignored :-(
			// embedded ONLY => not yet persisted
			// Maybe we could omit the ec.persistObjectInternal(...) completely for embedded objects to prevent double handling,
			// but I guess, we'd have to add other code, then, doing things that are also done by ec.persistObjectInternal(...)
			// besides the actual persisting (which is skipped for embedded-ONLY PCs [but done for embedded PCs that are not
			// @EmbeddedOnly]) - e.g. create & assign an ObjectProvider if none is assigned, yet. Marco :-)

//			ClassMeta embeddedClassMeta = ((Cumulus4jStoreManager)ec.getStoreManager()).getClassMeta(ec, valuePC.getClass());
			AbstractClassMetaData embeddedDNClassMetaData = embeddedOP.getClassMetaData();
			EmbeddedObjectContainer embeddedObjectContainer = new EmbeddedObjectContainer(embeddedClassMeta.getClassID());
			embeddedOP.provideFields(
					embeddedDNClassMetaData.getAllMemberPositions(),
					new StoreFieldManager(embeddedOP, cryptoContext, pmData, embeddedClassMeta, embeddedDNClassMetaData, cryptoContext.getKeyStoreRefID(), embeddedObjectContainer));
			embeddedObjectContainer.setVersion(embeddedOP.getTransactionalVersion());
//		}
		return embeddedObjectContainer;
	}

	/**
	 * Store a single related object (1-1-relationship).
	 * The related object is persistence-capable.
	 */
	protected void storeObjectFieldWithRelationTypeSingleValue(int fieldNumber, Object value, AbstractMemberMetaData mmd, FieldMeta fieldMeta) {
		// Persistable object - persist the related object and store the identity in the cell
		boolean embedded = mmd.isEmbedded();
		int objectType = embedded ? ObjectProvider.EMBEDDED_PC : ObjectProvider.PC;

		Object valuePC = ec.persistObjectInternal(value, op, fieldNumber, objectType);
		ec.flushInternal(true);

		if (embedded) {
			if (valuePC != null) {
				EmbeddedClassMeta embeddedClassMeta = fieldMeta.getEmbeddedClassMeta();
				EmbeddedObjectContainer embeddedObjectContainer = createEmbeddedObjectContainerFromPC(fieldMeta, embeddedClassMeta, valuePC);
				objectContainer.setValue(fieldMeta.getFieldID(), embeddedObjectContainer);
			}
		}
		else {
			if (mmd.getMappedBy() == null) {
				Object valueID = ObjectContainerHelper.entityToReference(cryptoContext, pmData, valuePC);
				objectContainer.setValue(fieldMeta.getFieldID(), valueID);
			}
		}
	}

	/**
	 * Store an array of related objects (1-n-relationship).
	 * The related objects are persistence-capable.
	 */
	protected void storeObjectFieldWithRelationTypeArray(int fieldNumber, Object value, AbstractMemberMetaData mmd, FieldMeta fieldMeta) {
		// TODO for mapped-by-relations, the order is not yet maintained - AFAIK.

		boolean embedded = mmd.getArray().isEmbeddedElement();
		int objectType = embedded ? ObjectProvider.EMBEDDED_COLLECTION_ELEMENT_PC : ObjectProvider.PC;
		EmbeddedClassMeta embeddedClassMeta = fieldMeta.getSubFieldMeta(FieldMetaRole.arrayElement).getEmbeddedClassMeta();

		Object[] ids = (mmd.getMappedBy() != null || embedded) ? null : new Object[Array.getLength(value)];
		EmbeddedObjectContainer[] embeddedObjectContainers = (ids != null || !embedded) ? null : new EmbeddedObjectContainer[Array.getLength(value)];
		for (int i = 0; i < Array.getLength(value); i++)
		{
			Object element = Array.get(value, i);
			Object elementPC = ec.persistObjectInternal(element, op, fieldNumber, objectType);
			ec.flushInternal(true);

			if (ids != null) {
				Object elementID = ObjectContainerHelper.entityToReference(cryptoContext, pmData, elementPC);
				ids[i] = elementID;
			}
			else if (embeddedObjectContainers != null) {
				EmbeddedObjectContainer embeddedObjectContainer = createEmbeddedObjectContainerFromPC(fieldMeta, embeddedClassMeta, elementPC);
				embeddedObjectContainers[i] = embeddedObjectContainer;
			}
		}

		if (ids != null)
			objectContainer.setValue(fieldMeta.getFieldID(), ids);
		else if (embeddedObjectContainers != null)
			objectContainer.setValue(fieldMeta.getFieldID(), embeddedObjectContainers);
	}

	/**
	 * Store a {@link Collection} (<code>List</code>, <code>Set</code>, etc.) of
	 * related objects (1-n-relationship).
	 * The related objects are persistence-capable.
	 */
	protected void storeObjectFieldWithRelationTypeCollection(int fieldNumber, Object value, AbstractMemberMetaData mmd, FieldMeta fieldMeta) {
		// TODO for mapped-by-relations, the order is not yet maintained (=> Lists) - AFAIK.

		boolean embedded = mmd.getCollection().isEmbeddedElement();
		int objectType = embedded ? ObjectProvider.EMBEDDED_COLLECTION_ELEMENT_PC : ObjectProvider.PC;
		EmbeddedClassMeta embeddedClassMeta = fieldMeta.getSubFieldMeta(FieldMetaRole.collectionElement).getEmbeddedClassMeta();

		Collection<?> collection = (Collection<?>)value;
		Object[] ids = (mmd.getMappedBy() != null || embedded) ? null : new Object[collection.size()];
		EmbeddedObjectContainer[] embeddedObjectContainers = (ids != null || !embedded) ? null : new EmbeddedObjectContainer[collection.size()];
		int idx = -1;
		for (Object element : collection) {
			Object elementPC = ec.persistObjectInternal(element, op, fieldNumber, objectType);
			ec.flushInternal(true);

			if (ids != null) {
				Object elementID = ObjectContainerHelper.entityToReference(cryptoContext, pmData, elementPC);
				ids[++idx] = elementID;
			}
			else if (embeddedObjectContainers != null) {
				EmbeddedObjectContainer embeddedObjectContainer = createEmbeddedObjectContainerFromPC(fieldMeta, embeddedClassMeta, elementPC);
				embeddedObjectContainers[++idx] = embeddedObjectContainer;
			}
		}


		if (ids != null)
			objectContainer.setValue(fieldMeta.getFieldID(), ids);
		else if (embeddedObjectContainers != null)
			objectContainer.setValue(fieldMeta.getFieldID(), embeddedObjectContainers);
	}

	/**
	 * Store a {@link Map} of related objects (1-n-relationship).
	 * The related objects are persistence-capable.
	 */
	protected void storeObjectFieldWithRelationTypeMap(int fieldNumber, Object value, AbstractMemberMetaData mmd, FieldMeta fieldMeta) {
		boolean keyIsPersistent = mmd.getMap().keyIsPersistent();
		boolean valueIsPersistent = mmd.getMap().valueIsPersistent();
		boolean embeddedKey = mmd.getMap().isEmbeddedKey();
		boolean embeddedValue = mmd.getMap().isEmbeddedValue();

		int keyObjectType;
		if (keyIsPersistent)
			keyObjectType = embeddedKey ? ObjectProvider.EMBEDDED_MAP_KEY_PC : ObjectProvider.PC;
		else
			keyObjectType = -1;

		int valueObjectType;
		if (valueIsPersistent)
			valueObjectType = embeddedValue ? ObjectProvider.EMBEDDED_MAP_VALUE_PC : ObjectProvider.PC;
		else
			valueObjectType = -1;

		Map<?,?> valueMap = (Map<?, ?>) value;
		Map<Object,Object> idMap = mmd.getMappedBy() != null ? null : new HashMap<Object, Object>(valueMap.size());
		for (Map.Entry<?, ?> me : valueMap.entrySet()) {
			Object k = me.getKey();
			Object v = me.getValue();

			if (keyIsPersistent) {
				Object kpc = ec.persistObjectInternal(k, op, fieldNumber, keyObjectType);
				ec.flushInternal(true);

				if (idMap != null)
					k = ObjectContainerHelper.entityToReference(cryptoContext, pmData, kpc);
			}

			if (valueIsPersistent) {
				Object vpc = ec.persistObjectInternal(v, op, fieldNumber, valueObjectType);
				ec.flushInternal(true);

				if (idMap != null)
					v = ObjectContainerHelper.entityToReference(cryptoContext, pmData, vpc);
			}

			if (idMap != null)
				idMap.put(k, v);
		}

		if (idMap != null)
			objectContainer.setValue(fieldMeta.getFieldID(), idMap);
	}

}
