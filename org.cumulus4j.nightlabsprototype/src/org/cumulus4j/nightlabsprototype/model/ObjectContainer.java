package org.cumulus4j.nightlabsprototype.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class ObjectContainer
implements Serializable, Cloneable
{
	private static final long serialVersionUID = 3L;

	/**
	 * Stores the concrete value of a field mapped by the persistent {@link FieldMeta#getFieldID() fieldID}.
	 * If the value originally was a persistable object, it is replaced by its object-ID.
	 */
	private Map<Long, Object> fieldID2value = new HashMap<Long, Object>();

//	/**
//	 * The key is a reference to {@link ClassMeta#getClassID()} by the object's ID.
//	 * This contains only an entry, if the field's value is a persistable object (not <code>null</code>
//	 * and not a "simple" type).
//	 */
//	private Map<Object, Long> objectID2classID = new HashMap<Object, Long>();

	private Object version;

	public ObjectContainer() { }

//	private void writeObject(java.io.ObjectOutputStream out)
//	throws IOException
//	{
//		out.defaultWriteObject();
//	}
//
//	private void readObject(java.io.ObjectInputStream in)
//	throws IOException, ClassNotFoundException
//	{
//		in.defaultReadObject();
//	}
//
//	@SuppressWarnings("unused") // this method seems to be so new to Java that the Eclipse compiler doesn't know it yet and shows a warning.
//	private void readObjectNoData()
//	throws ObjectStreamException
//	{
//		// no special handling necessary
//	}

	public Object getValue(long fieldID)
	{
		return fieldID2value.get(fieldID);
	}

//	public Long getValueTypeClassID(long fieldID)
//	{
//		return fieldID2classID.get(fieldID);
//	}

	/**
	 * Set a value.
	 * @param fieldID the field's persistent ID, i.e. a reference to {@link FieldMeta#getFieldID() FieldMeta.fieldID}.
	 * @!param valueTypeClassID either <code>null</code> or a reference to the {@link ClassMeta#getClassID() ClassMeta.classID}
	 * of the original value's concrete type (this must be either the declared type or a subclass). Original value means, if it
	 * is a persistable object, the type of the persistable object itself and not the type of its object-ID.
	 * @param value either the raw value or the object-ID of a persistable object. Persistable objects are never stored
	 * directly in an <code>ObjectContainer</code>.
	 */
	public void setValue(long fieldID, Object value)
	{
		if (value == null)
			fieldID2value.remove(fieldID);
		else
			fieldID2value.put(fieldID, value);

//		if (valueTypeClassID == null)
//			fieldID2classID.remove(fieldID);
//		else
//			fieldID2classID.put(fieldID, valueTypeClassID);
	}

	public Map<Long, Object> getFieldID2value() {
		return Collections.unmodifiableMap(fieldID2value);
	}

	/**
	 * Get the object's version or <code>null</code>, if the persistence-capable class has no versioning enabled.
	 * The version can be a {@link Long}, a {@link Date} or anything else supported by DataNucleus/JDO.
	 * @return the object's version or <code>null</code>.
	 */
	public Object getVersion() {
		return version;
	}
	public void setVersion(Object version) {
		this.version = version;
	}

	@Override
	public ObjectContainer clone()
	{
		ObjectContainer clone;
		try {
			clone = (ObjectContainer) super.clone();
		} catch (CloneNotSupportedException e) {
			throw new RuntimeException(e); // should never happen => wrap as RuntimeException!
		}
		clone.fieldID2value = new HashMap<Long, Object>(this.fieldID2value);
		return clone;
	}
}
