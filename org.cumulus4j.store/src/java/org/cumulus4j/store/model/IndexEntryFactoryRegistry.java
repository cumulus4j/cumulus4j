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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.cumulus4j.store.Cumulus4jStoreManager;
import org.datanucleus.ClassLoaderResolver;
import org.datanucleus.metadata.AbstractMemberMetaData;
import org.datanucleus.metadata.ArrayMetaData;
import org.datanucleus.metadata.CollectionMetaData;
import org.datanucleus.metadata.MapMetaData;
import org.datanucleus.plugin.ConfigurationElement;
import org.datanucleus.plugin.PluginManager;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.exceptions.UnsupportedDataTypeException;
import org.datanucleus.util.StringUtils;

/**
 * <p>
 * Registry responsible for the extension-point <code>org.cumulus4j.store.index_mapping</code>.
 * </p><p>
 * This registry maps an {@link IndexEntryFactory} to a java-type or a combination of java-,
 * jdbc- and sql-type.
 * </p><p>
 * There is one instance of <code>IndexEntryFactoryRegistry</code> per {@link Cumulus4jStoreManager}.
 * </p>
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
		Class<?> javaType;
		String jdbcTypes;
		String sqlTypes;
		IndexEntryFactory factory;

		public boolean matches(Class<?> type, String jdbcType, String sqlType) {
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

	/**
	 * Create a new registry instance.
	 * @param storeMgr the owning store-manager.
	 */
	public IndexEntryFactoryRegistry(Cumulus4jStoreManager storeMgr)
	{
		// Load up plugin information
		ClassLoaderResolver clr = storeMgr.getNucleusContext().getClassLoaderResolver(storeMgr.getClass().getClassLoader());
		PluginManager pluginMgr = storeMgr.getNucleusContext().getPluginManager();
		ConfigurationElement[] elems = pluginMgr.getConfigurationElementsForExtension(
				"org.cumulus4j.store.index_mapping", null, null);
		boolean useClob = storeMgr.getBooleanProperty("cumulus4j.index.clob.enabled", true);
		if (elems != null) {
			for (int i=0;i<elems.length;i++) {
				IndexMapping mapping = new IndexMapping();
				String typeName = elems[i].getAttribute("type");
				mapping.javaType = clr.classForName(typeName);

				String indexTypeName = elems[i].getAttribute("index-entry-type");
				if (indexTypeName != null)
					indexTypeName = indexTypeName.trim();

				if (indexTypeName != null && indexTypeName.isEmpty())
					indexTypeName = null;

				String indexFactoryTypeName = elems[i].getAttribute("index-entry-factory-type");
				if (indexFactoryTypeName != null)
					indexFactoryTypeName = indexFactoryTypeName.trim();

				if (indexFactoryTypeName != null && indexFactoryTypeName.isEmpty())
					indexFactoryTypeName = null;

				if (indexFactoryTypeName != null && indexTypeName != null)
					throw new IllegalStateException("Both, 'index-entry-factory-type' and 'index-entry-type' are specified, but only exactly one must be present! index-entry-factory-type=\"" + indexFactoryTypeName + "\" index-entry-type=\"" + indexTypeName + "\"");

				if (indexFactoryTypeName == null && indexTypeName == null)
					throw new IllegalStateException("Both, 'index-entry-factory-type' and 'index-entry-type' are missing, but exactly one must be present!");

				if (indexFactoryTypeName != null) {
					@SuppressWarnings("unchecked")
					Class<? extends IndexEntryFactory> idxEntryFactoryClass = pluginMgr.loadClass(
							elems[i].getExtension().getPlugin().getSymbolicName(), indexFactoryTypeName
					);
					try {
						mapping.factory = idxEntryFactoryClass.newInstance();
					} catch (InstantiationException e) {
						throw new RuntimeException(e);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
					indexTypeName = mapping.factory.getIndexEntryClass().getName();
				}
				else {
					if (factoryByEntryType.containsKey(indexTypeName)) {
						// Reuse the existing factory of this type
						mapping.factory = factoryByEntryType.get(indexTypeName);
					}
					else {
						// Create a new factory of this type and cache it
						@SuppressWarnings("unchecked")
						Class<? extends IndexEntry> idxEntryClass = pluginMgr.loadClass(
								elems[i].getExtension().getPlugin().getSymbolicName(), indexTypeName
						);
						IndexEntryFactory factory = new DefaultIndexEntryFactory(idxEntryClass);
						factoryByEntryType.put(indexTypeName, factory);
						mapping.factory = factory;
					}
				}

				String jdbcTypes = elems[i].getAttribute("jdbc-types");
				if (!StringUtils.isWhitespace(jdbcTypes)) {
					mapping.jdbcTypes = jdbcTypes;
				}
				String sqlTypes = elems[i].getAttribute("sql-types");
				if (!StringUtils.isWhitespace(sqlTypes)) {
					mapping.sqlTypes = sqlTypes;
				}

				if (indexTypeName.equals(IndexEntryStringLong.class.getName()) && !useClob) {
					// User doesn't want to use CLOB handing
					mapping.factory = null;
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
			case arrayElement:{
				ArrayMetaData amd = mmd.getArray();
				if(amd != null){
					fieldType = clr.classForName(amd.getElementType());
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

	/**
	 * Get the special {@link IndexEntryFactory} used for container-sizes. This special index
	 * allows using {@link Collection#isEmpty()}, {@link Collection#size()} and the like in JDOQL
	 * (or "SIZE(...)" and the like in JPQL).
	 * @return the special {@link IndexEntryFactory} used for container-sizes.
	 */
	public IndexEntryFactory getIndexEntryFactoryForContainerSize() {
		return indexEntryFactoryContainerSize;
	}
}
