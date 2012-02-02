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
package org.cumulus4j.store;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.ObjectContainer;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.store.ExecutionContext;
import org.datanucleus.store.ObjectProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helper class for replacing object-references when storing a 1-1- or 1-n- or m-n-relationship
 * inside an {@link ObjectContainer}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class ObjectContainerHelper
{
	private static final Logger logger = LoggerFactory.getLogger(ObjectContainerHelper.class);

	/**
	 * If <code>false</code>, store object-ID in {@link ObjectContainer}.
	 * If <code>true</code>, store {@link DataEntry#getDataEntryID() dataEntryID} in {@link ObjectContainer}.
	 */
	private static final boolean USE_DATA_ENTRY_ID = true;

	private ObjectContainerHelper() { }

	@SuppressWarnings("unused")
	public static Object entityToReference(ExecutionContext ec, PersistenceManager pmData, Object entity)
	{
		if (entity == null)
			return null;

		ObjectProvider objectProvider = ec.findObjectProvider(entity);
		if (objectProvider == null)
			throw new IllegalStateException("ec.findObjectProvider(entity) returned null for " + entity);

		logger.trace("entityToReference: BEFORE objectProvider.flush() >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		objectProvider.flush();

		logger.trace("entityToReference: AFTER objectProvider.flush() <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

		logger.trace("entityToReference: BEFORE pmData.flush() >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");

		pmData.flush();

		logger.trace("entityToReference: AFTER pmData.flush() <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");

		Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) ec.getStoreManager();
		Object objectID = ec.getApiAdapter().getIdForObject(entity);
		if (objectID == null)
			throw new IllegalStateException("executionContext.getApiAdapter().getIdForObject(entity) returned null for " + entity);

		storeManager.setClassNameForObjectID(objectID, entity.getClass().getName());

		if (USE_DATA_ENTRY_ID) {
			ClassMeta classMeta = storeManager.getClassMeta(ec, entity.getClass());
			Long dataEntryID = DataEntry.getDataEntryID(pmData, classMeta, objectID.toString());
			if (dataEntryID == null)
				throw new IllegalStateException("DataEntry.getDataEntryID(...) returned null for entity=\"" + entity + "\" with objectID=\"" + objectID +  "\"");

			return dataEntryID;
		}

		return objectID;
	}

	@SuppressWarnings("unused")
	public static Object referenceToEntity(ExecutionContext ec, PersistenceManager pmData, Object reference)
	{
		if (reference == null)
			return null;

		if (USE_DATA_ENTRY_ID) {
			DataEntry dataEntry = DataEntry.getDataEntry(pmData, ((Long)reference).longValue());
			if (dataEntry == null)
				throw new IllegalStateException("DataEntry.getDataEntry(...) returned null for reference=\"" + reference + "\"!");

			AbstractClassMetaData cmd = dataEntry.getClassMeta().getDataNucleusClassMetaData(ec);
			return IdentityUtils.getObjectFromIdString(dataEntry.getObjectID(), cmd, ec, true);
		}

		return ec.findObject(reference, true, true, null);
	}

	@SuppressWarnings("unused")
	public static Long referenceToDataEntryID(ExecutionContext ec, PersistenceManager pmData, Object reference)
	{
		if (reference == null)
			return null;

		if (USE_DATA_ENTRY_ID)
			return (Long)reference;

		Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) ec.getStoreManager();
		String clazzName = storeManager.getClassNameForObjectID(reference, ec.getClassLoaderResolver(), ec);
		Class<?> clazz = ec.getClassLoaderResolver().classForName(clazzName);
		ClassMeta classMeta = storeManager.getClassMeta(ec, clazz);
		return DataEntry.getDataEntryID(pmData, classMeta, reference.toString());
	}
}
