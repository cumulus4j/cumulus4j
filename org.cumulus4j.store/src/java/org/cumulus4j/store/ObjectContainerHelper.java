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
	public static Object entityToReference(ExecutionContext ec, PersistenceManager pmData, Object entity)
	{
		Cumulus4jStoreManager storeManager = (Cumulus4jStoreManager) ec.getStoreManager();
		Object objectID = ec.getApiAdapter().getIdForObject(entity);
		if (objectID == null)
			throw new IllegalStateException("executionContext.getApiAdapter().getIdForObject(entity) returned null for " + entity);

		storeManager.setClassNameForObjectID(objectID, entity.getClass().getName());

		if (USE_DATA_ENTRY_ID) {
			ClassMeta classMeta = storeManager.getClassMeta(ec, entity.getClass());
			return DataEntry.getDataEntryID(pmData, classMeta, objectID.toString());
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
