package org.cumulus4j.core;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.FieldMeta;
import org.cumulus4j.core.model.ObjectContainer;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.Relation;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.datanucleus.store.fieldmanager.AbstractFieldManager;
import org.datanucleus.store.types.sco.SCO;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class InsertFieldManager extends AbstractFieldManager
{
	private ObjectProvider op;
	private ExecutionContext executionContext;
	private ClassMeta classMeta;
	private AbstractClassMetaData dnClassMetaData;
	private ObjectContainer objectContainer;
	private Cumulus4jStoreManager storeManager;

	public InsertFieldManager(
			ObjectProvider op,
			ClassMeta classMeta,
			AbstractClassMetaData dnClassMetaData,
			ObjectContainer objectContainer // populated by this class
	)
	{
		this.op = op;
		this.executionContext = op.getExecutionContext();
		this.classMeta = classMeta;
		this.dnClassMetaData = dnClassMetaData;
		this.objectContainer = objectContainer;
		this.storeManager = (Cumulus4jStoreManager) executionContext.getStoreManager();
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

		// mapped-by => skip storing it
		if (mmd.getMappedBy() != null)
			return; // TODO is this sufficient to take 'mapped-by' into account? Needs testing // TODO maybe clear the value in case it was not mapped-by before? - what about other migrations?

		FieldMeta fieldMeta = classMeta.getFieldMeta(mmd.getClassName(), mmd.getName());
		if (fieldMeta == null)
			throw new IllegalStateException("Unknown field! class=" + dnClassMetaData.getFullClassName() + " fieldNumber=" + fieldNumber + " fieldName=" + mmd.getName());

		if (value == null) {
			objectContainer.setValue(fieldMeta.getFieldID(), null);
			return;
		}

		int relationType = mmd.getRelationType(executionContext.getClassLoaderResolver());

		// Replace any SCO field that isn't already a wrapper, with its wrapper object
		boolean[] secondClassMutableFieldFlags = dnClassMetaData.getSCOMutableMemberFlags();
		if (secondClassMutableFieldFlags[fieldNumber] && !(value instanceof SCO))
			value = op.wrapSCOField(fieldNumber, value, false, true, true);

		if (relationType == Relation.NONE)
		{
			objectContainer.setValue(fieldMeta.getFieldID(), value);
		}
		else if (Relation.isRelationSingleValued(relationType))
		{
			// Persistable object - persist the related object and store the identity in the cell
			Object valuePC = executionContext.persistObjectInternal(value, op, fieldNumber, -1);
			Object valueID = executionContext.getApiAdapter().getIdForObject(valuePC);
			storeManager.setClassNameForObjectID(valueID, valuePC.getClass().getName());
			objectContainer.setValue(fieldMeta.getFieldID(), valueID);
		}
		else if (Relation.isRelationMultiValued(relationType))
		{
			// Collection/Map/Array
			if (mmd.hasCollection())
			{
				Collection<?> collection = (Collection<?>)value;
				Object[] ids = new Object[collection.size()];
				int idx = -1;
				for (Object element : collection) {
					Object elementPC = executionContext.persistObjectInternal(element, op, fieldNumber, -1);
					Object elementID = executionContext.getApiAdapter().getIdForObject(elementPC);
					storeManager.setClassNameForObjectID(elementID, elementPC.getClass().getName());
					ids[++idx] = elementID;
				}
				objectContainer.setValue(fieldMeta.getFieldID(), ids);
			}
			else if (mmd.hasMap())
			{
//				AbstractClassMetaData keyClassMetaData = mmd.getMap().getKeyClassMetaData(op.getExecutionContext().getClassLoaderResolver(), op.getExecutionContext().getMetaDataManager());
//				AbstractClassMetaData valueClassMetaData = mmd.getMap().getValueClassMetaData(op.getExecutionContext().getClassLoaderResolver(), op.getExecutionContext().getMetaDataManager());

				boolean keyIsPersistent = mmd.getMap().keyIsPersistent();
				boolean valueIsPersistent = mmd.getMap().valueIsPersistent();

				Map<?,?> valueMap = (Map<?, ?>) value;
				Map<Object,Object> idMap = new HashMap<Object, Object>(valueMap.size());
				for (Map.Entry<?, ?> me : valueMap.entrySet()) {
					Object k = me.getKey();
					Object v = me.getValue();

					if (keyIsPersistent) {
						Object kpc = executionContext.persistObjectInternal(k, op, fieldNumber, -1);
						k = executionContext.getApiAdapter().getIdForObject(kpc);
						storeManager.setClassNameForObjectID(k, kpc.getClass().getName());
					}

					if (valueIsPersistent) {
						Object vpc = executionContext.persistObjectInternal(v, op, fieldNumber, -1);
						v = executionContext.getApiAdapter().getIdForObject(vpc);
						storeManager.setClassNameForObjectID(v, vpc.getClass().getName());
					}

					idMap.put(k, v);
				}
				objectContainer.setValue(fieldMeta.getFieldID(), idMap);
			}
			else if (mmd.hasArray())
			{
				Object[] ids = new Object[Array.getLength(value)];
				for (int i=0;i<Array.getLength(value);i++)
				{
					Object element = Array.get(value, i);
					Object elementPC = executionContext.persistObjectInternal(element, op, fieldNumber, -1);
					Object elementID = executionContext.getApiAdapter().getIdForObject(elementPC);
					storeManager.setClassNameForObjectID(elementID, elementPC.getClass().getName());
					ids[i] = elementID;
				}
				objectContainer.setValue(fieldMeta.getFieldID(), ids);
			}
		}
		else
			throw new IllegalStateException("Unexpected relationType: " + relationType);
	}

}
