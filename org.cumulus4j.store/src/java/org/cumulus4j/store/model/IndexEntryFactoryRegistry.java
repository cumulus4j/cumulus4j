package org.cumulus4j.store.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.CollectionMetaData;
import org.datanucleus.metadata.MapMetaData;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.exceptions.UnsupportedDataTypeException;
import org.datanucleus.util.StringUtils;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class IndexEntryFactoryRegistry
{
	/** Cache of factory for use with each java-type+jdbc+sql */
	private Map<String, IndexEntryFactory> factoryByKey = new HashMap<String, IndexEntryFactory>();

	private Map<String, IndexEntryFactory> factoryByEntryType = new HashMap<String, IndexEntryFactory>();

	/** Mappings of java-type+jdbc+sql type and the factory they should use */
	private List<IndexMapping> indexMappings = new ArrayList<IndexMapping>();

	private IndexEntryFactory indexEntryFactoryContainerSize = new DefaultIndexEntryFactory(IndexEntryContainerSize.class);

	class IndexMapping {
		Class javaType;
		String jdbcTypes;
		String sqlTypes;
		IndexEntryFactory factory;

		public boolean matches(Class type, String jdbcType, String sqlType) {
			if (javaType.isAssignableFrom(type)) {
				if (jdbcTypes != null) {
					if (jdbcType == null) {
						return false;
					}
					else {
						return jdbcTypes.indexOf(jdbcType) >= 0;
					}
				}
				else if (sqlTypes != null) {
					if (sqlType == null) {
						return false;
					}
					else {
						return sqlTypes.indexOf(sqlType) >= 0;
					}
				}
				else {
					return true;
				}
			}
			return false;
		}
	}

	public IndexEntryFactoryRegistry(Cumulus4jStoreManager storeMgr) {

		// Load up plugin information
		ClassLoaderResolver clr = storeMgr.getNucleusContext().getClassLoaderResolver(storeMgr.getClass().getClassLoader());
		PluginManager pluginMgr = storeMgr.getNucleusContext().getPluginManager();
		ConfigurationElement[] elems = pluginMgr.getConfigurationElementsForExtension(
				"org.cumulus4j.store.index_mapping", null, null);
		if (elems != null) {
			for (int i=0;i<elems.length;i++) {
				IndexMapping mapping = new IndexMapping();
				String typeName = elems[i].getAttribute("type");
				mapping.javaType = clr.classForName(typeName);

				String indexTypeName = elems[i].getAttribute("index-entry-type");
				if (factoryByEntryType.containsKey(indexTypeName)) {
					// Reuse the existing factory of this type
					mapping.factory = factoryByEntryType.get(indexTypeName);
				}
				else {
					// Create a new factory of this type and cache it
					Class idxEntryClass = pluginMgr.loadClass(elems[i].getExtension().getPlugin().getSymbolicName(), indexTypeName);
					IndexEntryFactory factory = new DefaultIndexEntryFactory(idxEntryClass);
					factoryByEntryType.put(indexTypeName, factory);
					mapping.factory = factory;
				}

				if (mapping.factory.getClass().getName().equals(IndexEntryStringLong.class.getName()) &&
						storeMgr.getBooleanProperty("cumulus4j.index.clob.enabled", true)) {
					// User doesn't want to use CLOB handing
					mapping.factory = null;
				}

				String jdbcTypes = elems[i].getAttribute("jdbc-types");
				if (!StringUtils.isWhitespace(jdbcTypes)) {
					mapping.jdbcTypes = jdbcTypes;
				}
				String sqlTypes = elems[i].getAttribute("sql-types");
				if (!StringUtils.isWhitespace(sqlTypes)) {
					mapping.sqlTypes = sqlTypes;
				}
				indexMappings.add(mapping);

				// Populate the primary cache lookups
				if (jdbcTypes == null && sqlTypes == null) {
					String key = getKeyForType(typeName, null, null);
					factoryByKey.put(key, mapping.factory);
				}
				else {
					if (jdbcTypes != null) {
						StringTokenizer tok = new StringTokenizer(jdbcTypes, ",");
						while (tok.hasMoreTokens()) {
							String jdbcType = tok.nextToken();
							String key = getKeyForType(typeName, jdbcType, null);
							factoryByKey.put(key, mapping.factory);
						}
					}
					if (sqlTypes != null) {
						StringTokenizer tok = new StringTokenizer(sqlTypes, ",");
						while (tok.hasMoreTokens()) {
							String sqlType = tok.nextToken();
							String key = getKeyForType(typeName, null, sqlType);
							factoryByKey.put(key, mapping.factory);
						}
					}
				}
			}
		}
	}

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

		String jdbcType = null;
		String sqlType = null;
		if (mmd.getColumnMetaData() != null && mmd.getColumnMetaData().length > 0) {
			jdbcType = mmd.getColumnMetaData()[0].getJdbcType();
			sqlType = mmd.getColumnMetaData()[0].getSqlType();
		}
		String key = getKeyForType(fieldType.getName(), jdbcType, sqlType);

		// Check the cache
		if (factoryByKey.containsKey(key)) {
			return factoryByKey.get(key);
		}

		Iterator<IndexMapping> mappingIter = indexMappings.iterator();
		while (mappingIter.hasNext()) {
			IndexMapping mapping = mappingIter.next();
			if (mapping.matches(fieldType, jdbcType, sqlType)) {
				factoryByKey.put(key, mapping.factory);
				return mapping.factory;
			}
		}

		if (throwExceptionIfNotFound)
			throw new UnsupportedDataTypeException("No IndexEntryFactory registered for this type: " + mmd);

		factoryByKey.put(key, null);
		return null;
	}

	private String getKeyForType(String javaTypeName, String jdbcTypeName, String sqlTypeName) {
		return javaTypeName + ":" + (jdbcTypeName != null ? jdbcTypeName : "") + ":" + (sqlTypeName != null ? sqlTypeName : "");
	}

	public IndexEntryFactory getIndexEntryFactoryForContainerSize() {
		return indexEntryFactoryContainerSize;
	}
}
