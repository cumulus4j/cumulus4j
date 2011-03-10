package org.cumulus4j.nightlabsprototype.model;

import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.store.exceptions.UnsupportedDataTypeException;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class IndexEntryFactoryRegistry
{
	private static IndexEntryFactoryRegistry sharedInstance = new IndexEntryFactoryRegistry();

	public static IndexEntryFactoryRegistry sharedInstance()
	{
		return sharedInstance;
	}

	private IndexEntryFactory indexEntryFactoryDouble = new IndexEntryFactoryDouble();
	private IndexEntryFactory indexEntryFactoryLong = new IndexEntryFactoryLong();
	private IndexEntryFactory indexEntryFactoryString = new IndexEntryFactoryString();

	public IndexEntryFactory getIndexEntryFactory(AbstractMemberMetaData mmd, boolean throwExceptionIfNotFound)
	{
		Class<?> fieldType = mmd.getType();

		if (String.class.isAssignableFrom(fieldType))
			return indexEntryFactoryString;

		if (Long.class.isAssignableFrom(fieldType) || Integer.class.isAssignableFrom(fieldType) || Short.class.isAssignableFrom(fieldType) || Byte.class.isAssignableFrom(fieldType) || long.class == fieldType || int.class == fieldType || short.class == fieldType || byte.class == fieldType)
			return indexEntryFactoryLong;

		if (Double.class.isAssignableFrom(fieldType) || Float.class.isAssignableFrom(fieldType) || double.class == fieldType || float.class == fieldType)
			return indexEntryFactoryDouble;

		if (throwExceptionIfNotFound)
			throw new UnsupportedDataTypeException("No IndexEntryFactory registered for this type: " + mmd);

		return null;
	}
}
