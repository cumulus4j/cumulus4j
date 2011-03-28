package org.cumulus4j.core;

import javax.jdo.PersistenceManager;

import org.cumulus4j.core.model.ClassMeta;
import org.cumulus4j.core.model.DataEntry;
import org.cumulus4j.core.model.ObjectContainer;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.store.ExecutionContext;

public final class ObjectContainerHelper
{
	/**
	 * If <code>false</code>, store object-ID in {@link ObjectContainer}.
	 * If <code>true</code>, store {@link DataEntry#getDataEntryID() dataEntryID} in {@link ObjectContainer}.
	 */
	private static final boolean USE_DATA_ENTRY_ID = false;

	private ObjectContainerHelper() { }

	public static Object entityToReference(ExecutionContext executionContext, PersistenceManager pm, Object entity)
	{
		Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) executionContext.getStoreManager();
		Object objectID = executionContext.getApiAdapter().getIdForObject(entity);
		if (objectID == null)
			throw new IllegalStateException("executionContext.getApiAdapter().getIdForObject(entity) returned null for " + entity);

		storeManager.setClassNameForObjectID(objectID, entity.getClass().getName());

		if (USE_DATA_ENTRY_ID) {
			ClassMeta classMeta = storeManager.getClassMeta(executionContext, entity.getClass());
			return DataEntry.getDataEntryID(pm, classMeta, objectID.toString());
		}

		return objectID;
	}

	public static Object referenceToEntity(ExecutionContext executionContext, PersistenceManager pm, Object reference)
	{
		if (reference == null)
			return null;

		if (USE_DATA_ENTRY_ID) {
			DataEntry dataEntry = DataEntry.getDataEntry(pm, ((Long)reference).longValue());
			AbstractClassMetaData cmd = dataEntry.getClassMeta().getDataNucleusClassMetaData(executionContext);
			reference = IdentityUtils.getObjectFromIdString(dataEntry.getObjectID(), cmd, executionContext, true);
		}

		return executionContext.findObject(reference, true, true, null);
	}

	public static Long referenceToDataEntryID(ExecutionContext executionContext, PersistenceManager pm, Object reference)
	{
		if (reference == null)
			return null;

		if (USE_DATA_ENTRY_ID)
			return (Long)reference;

		Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) executionContext.getStoreManager();
		String clazzName = storeManager.getClassNameForObjectID(reference, executionContext.getClassLoaderResolver(), executionContext);
		Class<?> clazz = executionContext.getClassLoaderResolver().classForName(clazzName);
		ClassMeta classMeta = storeManager.getClassMeta(executionContext, clazz);
		return DataEntry.getDataEntryID(pm, classMeta, reference.toString());
	}
}
