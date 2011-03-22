package org.cumulus4j.core.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.CollectionMetaData;
import org.datanucleus.metadata.ColumnMetaData;
import org.datanucleus.metadata.MapMetaData;
import org.datanucleus.properties.PropertyStore;
import org.datanucleus.store.ExecutionContext;
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

	private Map<String, Class<?>> typeName2ClassMap = Collections.synchronizedMap(new HashMap<String, Class<?>>());

	/**
	 * @param typeName the type name from {@link CollectionMetaData#getElementType()}, {@link MapMetaData#getKeyType()},
	 * {@link MapMetaData#getValueType()} or similar. Since this String might contain multiple comma-separated entries,
	 * this method tries to find the closest common super-type.
	 * @return the type
	 */
	private Class<?> getType(ClassLoaderResolver clr, String typeName)
	{
		Class<?> result = typeName2ClassMap.get(typeName);
		if (result != null)
			return result;

		int commaIndex = typeName.indexOf(',');
		if (commaIndex < 0) {
			result = clr.classForName(typeName);
		}
		else {
			throw new UnsupportedOperationException("typeName really contains a comma: " + typeName); // DN's code seems to never expect this.
//			Set<Class<?>> classes = new HashSet<Class<?>>();
//			String[] typeNames = typeName.split(",");
//			for (String tn : typeNames) {
//				tn = tn.trim();
//				if (!tn.isEmpty()) {
//					Class<?> c = clr.classForName(tn);
//					classes.add(c);
//				}
//			}
//
//			result = classes.iterator().next();
//			boolean
		}

		typeName2ClassMap.put(typeName, result);
		return result;
	}

	/**
	 * @param executionContext the context.
	 * @param fieldMeta either a {@link FieldMeta} for a {@link FieldMetaRole#primary primary} field or a sub-<code>FieldMeta</code>,
	 * if a <code>Collection</code> element, a <code>Map</code> key, a <code>Map</code> value or similar are indexed.
	 * @param throwExceptionIfNotFound
	 * @return
	 */
	public IndexEntryFactory getIndexEntryFactory(ExecutionContext executionContext, FieldMeta fieldMeta, boolean throwExceptionIfNotFound)
	{
		AbstractMemberMetaData mmd = fieldMeta.getDataNucleusMemberMetaData(executionContext);
		Class<?> fieldType = null;
		switch (fieldMeta.getRole()) {
			case primary:
				fieldType = mmd.getType();
				break;
			case collectionElement: {
				CollectionMetaData cmd = mmd.getCollection();
				if (cmd != null)
					fieldType = getType(executionContext.getClassLoaderResolver(), cmd.getElementType());
			}
			break;
			case mapKey: {
				MapMetaData mapMetaData = mmd.getMap();
				if (mapMetaData != null)
					fieldType = getType(executionContext.getClassLoaderResolver(), mapMetaData.getKeyType());
			}
			break;
			case mapValue: {
				MapMetaData mapMetaData = mmd.getMap();
				if (mapMetaData != null)
					fieldType = getType(executionContext.getClassLoaderResolver(), mapMetaData.getValueType());
			}
			break;
		}

//	public IndexEntryFactory getIndexEntryFactory(AbstractMemberMetaData mmd, boolean throwExceptionIfNotFound)
//	{
//		Class<?> fieldType = mmd.getType();

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
