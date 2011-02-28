package org.cumulus4j.test.model;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ObjectContainer
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private Map<Long, Object> fieldID2value = new HashMap<Long, Object>();

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

	public void setValue(long fieldID, Object value)
	{
		fieldID2value.put(fieldID, value);
	}

	public Map<Long, Object> getFieldID2value() {
		return Collections.unmodifiableMap(fieldID2value);
	}
}
