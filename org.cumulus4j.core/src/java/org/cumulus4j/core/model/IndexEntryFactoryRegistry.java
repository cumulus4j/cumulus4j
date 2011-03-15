package org.cumulus4j.core.model;

import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.properties.PropertyStore;
import org.datanucleus.store.exceptions.UnsupportedDataTypeException;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class IndexEntryFactoryRegistry
{
	private static IndexEntryFactoryRegistry sharedInstance = null;

	public static void createSharedInstance(PropertyStore propertyStore)
	{
		sharedInstance = new IndexEntryFactoryRegistry();

		if (!propertyStore.getBooleanProperty("cumulus4j.index.clob.enabled", true))
			sharedInstance.indexEntryFactoryStringLong = null;
	}

	public static IndexEntryFactoryRegistry sharedInstance()
	{
		if (sharedInstance == null)
			throw new IllegalStateException("createSharedInstance(...) not yet called!");

		return sharedInstance;
	}

	private IndexEntryFactory indexEntryFactoryDouble = new IndexEntryFactoryDouble();
	private IndexEntryFactory indexEntryFactoryLong = new IndexEntryFactoryLong();
	private IndexEntryFactory indexEntryFactoryStringShort = new IndexEntryFactoryStringShort();
	private IndexEntryFactory indexEntryFactoryStringLong = new IndexEntryFactoryStringLong();

	public IndexEntryFactory getIndexEntryFactory(AbstractMemberMetaData mmd, boolean throwExceptionIfNotFound)
	{
		Class<?> fieldType = mmd.getType();

		if (String.class.isAssignableFrom(fieldType)) {
			// TODO is this the right way to find out whether we need a long CLOB index or a short VARCHAR index?
			if (mmd.getColumnMetaData() != null && mmd.getColumnMetaData().length > 0) {
				ColumnMetaData columnMetaData = mmd.getColumnMetaData()[0];

				if (columnMetaData.getScale() != null && columnMetaData.getScale().intValue() > 255)
					return indexEntryFactoryStringLong;

				if ("CLOB".equalsIgnoreCase(columnMetaData.getJdbcType()))
					return indexEntryFactoryStringLong;

				if ("TEXT".equalsIgnoreCase(columnMetaData.getJdbcType()))
					return indexEntryFactoryStringLong;

				if ("CLOB".equalsIgnoreCase(columnMetaData.getSqlType()))
					return indexEntryFactoryStringLong;

				if ("TEXT".equalsIgnoreCase(columnMetaData.getSqlType()))
					return indexEntryFactoryStringLong;
			}

			return indexEntryFactoryStringShort;
		}

		if (Long.class.isAssignableFrom(fieldType) || Integer.class.isAssignableFrom(fieldType) || Short.class.isAssignableFrom(fieldType) || Byte.class.isAssignableFrom(fieldType) || long.class == fieldType || int.class == fieldType || short.class == fieldType || byte.class == fieldType)
			return indexEntryFactoryLong;

		if (Double.class.isAssignableFrom(fieldType) || Float.class.isAssignableFrom(fieldType) || double.class == fieldType || float.class == fieldType)
			return indexEntryFactoryDouble;

		if (throwExceptionIfNotFound)
			throw new UnsupportedDataTypeException("No IndexEntryFactory registered for this type: " + mmd);

		return null;
	}
}
