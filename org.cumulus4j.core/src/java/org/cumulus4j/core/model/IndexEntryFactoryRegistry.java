package org.cumulus4j.core.model;

import java.util.Date;

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

	protected IndexEntryFactoryRegistry() { }

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

	private IndexEntryFactory indexEntryFactoryDouble = new DefaultIndexEntryFactory(IndexEntryDouble.class);
	private IndexEntryFactory indexEntryFactoryLong = new DefaultIndexEntryFactory(IndexEntryLong.class);
	private IndexEntryFactory indexEntryFactoryStringShort = new DefaultIndexEntryFactory(IndexEntryStringShort.class);
	private IndexEntryFactory indexEntryFactoryStringLong = new DefaultIndexEntryFactory(IndexEntryStringLong.class);
	private IndexEntryFactory indexEntryFactoryDate = new DefaultIndexEntryFactory(IndexEntryDate.class);

	/**
	 * Get the appropriate {@link IndexEntryFactory} subclass instance for the given {@link FieldMeta}.
	 * @param executionContext the context.
	 * @param fieldMeta either a {@link FieldMeta} for a {@link FieldMetaRole#primary primary} field or a sub-<code>FieldMeta</code>,
	 * if a <code>Collection</code> element, a <code>Map</code> key, a <code>Map</code> value or similar are indexed.
	 * @param throwExceptionIfNotFound throw an exception instead of returning <code>null</code>, if there is no {@link IndexEntryFactory} for
	 * the given <code>fieldMeta</code>.
	 * @return the appropriate {@link IndexEntryFactory} or <code>null</code>, if none is registered and <code>throwExceptionIfNotFound == false</code>.
	 */
	public IndexEntryFactory getIndexEntryFactory(ExecutionContext executionContext, FieldMeta fieldMeta, boolean throwExceptionIfNotFound)
	{
		ClassLoaderResolver clr = executionContext.getClassLoaderResolver();
		AbstractMemberMetaData mmd = fieldMeta.getDataNucleusMemberMetaData(executionContext);
		Class<?> fieldType = null;
		switch (fieldMeta.getRole()) {
			case primary:
				fieldType = mmd.getType();
				break;
			case collectionElement: {
				CollectionMetaData cmd = mmd.getCollection();
				if (cmd != null) {
					// Even though the documentation of CollectionMetaData.getElementType() says there could be a comma-separated
					// list of class names, the whole DataNucleus code-base currently ignores this possibility.
					// To verify, I just tried the following field annotation:
					//
					// @Join
					// @Element(types={String.class, Long.class})
					// private Set<Object> set = new HashSet<Object>();
					//
					// The result was that DataNucleus ignored the String.class and only took the Long.class into account - cmd.getElementType()
					// contained only "java.lang.Long" here. Since it would make our indexing much more complicated and we cannot test it anyway
					// as long as DN does not support it, we ignore this situation for now.
					// We can still implement it later (major refactoring, though), if DN ever supports it one day.
					// Marco ;-)
					fieldType = clr.classForName(cmd.getElementType());
				}
			}
			break;
			case mapKey: {
				MapMetaData mapMetaData = mmd.getMap();
				if (mapMetaData != null) {
					// Here, the same applies as for the CollectionMetaData.getElementType(). Marco ;-)
					fieldType = clr.classForName(mapMetaData.getKeyType());
				}
			}
			break;
			case mapValue: {
				MapMetaData mapMetaData = mmd.getMap();
				if (mapMetaData != null) {
					// Here, the same applies as for the CollectionMetaData.getElementType(). Marco ;-)
					fieldType = clr.classForName(mapMetaData.getValueType());
				}
			}
			break;
		}

		// TODO Introduce plugin point to extend this and maybe move this configuration into the pluggable factory.
		if (String.class.isAssignableFrom(fieldType)) {
			// TODO is this the right way to find out whether we need a long CLOB index or a short VARCHAR index?
			if (mmd.getColumnMetaData() != null && mmd.getColumnMetaData().length > 0) {
				ColumnMetaData columnMetaData = mmd.getColumnMetaData()[0];

				if (columnMetaData.getScale() != null && columnMetaData.getScale().intValue() > 255)
					return indexEntryFactoryStringLong;

				if ("CLOB".equalsIgnoreCase(columnMetaData.getJdbcType()))
					return indexEntryFactoryStringLong;

				// TODO What is a jdbc-type of TEXT ? There is no such thing. see java.sql.Types
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

		if (Date.class.isAssignableFrom(fieldType))
			return indexEntryFactoryDate;

		if (throwExceptionIfNotFound)
			throw new UnsupportedDataTypeException("No IndexEntryFactory registered for this type: " + mmd);

		return null;
	}
}
