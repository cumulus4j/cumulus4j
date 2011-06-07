package org.cumulus4j.store;

import javax.jdo.PersistenceManager;

import org.cumulus4j.store.model.ClassMeta;
import org.cumulus4j.store.model.DataEntry;
import org.cumulus4j.store.model.ObjectContainer;
import org.datanucleus.identity.IdentityUtils;
import org.datanucleus.metadata.AbstractClassMetaData;
import org.datanucleus.store.ExecutionContext;

/**
 * Helper class for replacing object-references when storing a 1-1- or 1-n- or m-n-relationship
 * inside an {@link ObjectContainer}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public final class ObjectContainerHelper
{
	/**
	 * If <code>false</code>, store object-ID in {@link ObjectContainer}.
	 * If <code>true</code>, store {@link DataEntry#getDataEntryID() dataEntryID} in {@link ObjectContainer}.
	 */
	private static final boolean USE_DATA_ENTRY_ID = true;

	private ObjectContainerHelper() { }

	@SuppressWarnings("unused")
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

	@SuppressWarnings("unused")
	public static Object referenceToEntity(ExecutionContext executionContext, PersistenceManager pm, Object reference)
	{
		if (reference == null)
			return null;

		if (USE_DATA_ENTRY_ID) {
			DataEntry dataEntry = DataEntry.getDataEntry(pm, ((Long)reference).longValue());
			AbstractClassMetaData cmd = dataEntry.getClassMeta().getDataNucleusClassMetaData(executionContext);
			return IdentityUtils.getObjectFromIdString(dataEntry.getObjectID(), cmd, executionContext, true);
		}

		return executionContext.findObject(reference, true, true, null);
	}

	@SuppressWarnings("unused")
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
