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
package org.cumulus4j.store.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Container holding the values of a persistent object.
 * </p>
 * <p>
 * Objects to be stored in the database, are first represented by an instance of {@link ObjectContainer} and
 * then serialised into a byte-array, which is finally encrypted and put into a {@link DataEntry}'s {@link DataEntry#getValue() value}.
 * </p>
 * <p>
 * Note, that references to other objects
 * are either not stored at all (in a "mapped-by"-relationship) or stored via the other object's
 * {@link DataEntry#getDataEntryID() dataEntryID}; a persistent object is never stored as-is.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class ObjectContainer
implements Serializable, Cloneable
{
	private static final long serialVersionUID = 4L;

	/**
	 * Stores the concrete value of a field mapped by the persistent {@link FieldMeta#getFieldID() fieldID}.
	 * If the value originally was a persistable object, it is replaced by its object-ID.
	 */
	private Map<Long, Object> fieldID2value = new HashMap<Long, Object>();

	private Object version;

	public ObjectContainer() { }

// TODO maybe do custom serialisation/deserialisation in order to better keep compatibility? Or should we maybe instead use
// ObjectContainer2, ObjectContainer3 etc. (i.e. other classes)?
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

	/**
	 * Get a value.
	 * @param fieldID the field's persistent ID, i.e. a reference to {@link FieldMeta#getFieldID() FieldMeta.fieldID}.
	 * @return the value or <code>null</code>.
	 */
	public Object getValue(long fieldID)
	{
		return fieldID2value.get(fieldID);
	}

	/**
	 * Set a value.
	 * @param fieldID the field's persistent ID, i.e. a reference to {@link FieldMeta#getFieldID() FieldMeta.fieldID}.
	 * @param value either the raw value or the object-ID of a persistable object. Persistable objects are never stored
	 * directly in an <code>ObjectContainer</code>.
	 */
	public void setValue(long fieldID, Object value)
	{
		if (value == null)
			fieldID2value.remove(fieldID);
		else
			fieldID2value.put(fieldID, value);
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
