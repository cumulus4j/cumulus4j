package org.cumulus4j.store;

import java.util.HashMap;
import java.util.Map;

import javax.jdo.FetchPlan;
import javax.jdo.PersistenceManager;

import org.cumulus4j.store.model.EncryptionCoordinateSet;

public class EncryptionCoordinateSetManager
{
	protected Map<Integer, EncryptionCoordinateSet> encryptionCoordinateSetID2EncryptionCoordinateSet = new HashMap<Integer, EncryptionCoordinateSet>();

	protected Map<String, EncryptionCoordinateSet> encryptionCoordinateString2EncryptionCoordinateSet = new HashMap<String, EncryptionCoordinateSet>();

	protected static String getEncryptionCoordinateString(String cipherTransformation, String macAlgorithm)
	{
		return cipherTransformation + "::" + macAlgorithm;
	}
	protected static String getEncryptionCoordinateString(EncryptionCoordinateSet encryptionCoordinateSet)
	{
		return getEncryptionCoordinateString(encryptionCoordinateSet.getCipherTransformation(), encryptionCoordinateSet.getMACAlgorithm());
	}

	public EncryptionCoordinateSetManager() { }

	public synchronized EncryptionCoordinateSet getEncryptionCoordinateSet(PersistenceManagerConnection persistenceManagerConnection, int encryptionCoordinateSetID)
	{
		EncryptionCoordinateSet encryptionCoordinateSet = encryptionCoordinateSetID2EncryptionCoordinateSet.get(encryptionCoordinateSetID);
		if (encryptionCoordinateSet == null) {
			PersistenceManager pm = persistenceManagerConnection.getDataPM();
			encryptionCoordinateSet = EncryptionCoordinateSet.getEncryptionCoordinateSet(pm, encryptionCoordinateSetID);
			if (encryptionCoordinateSet != null) {
				pm.getFetchPlan().setMaxFetchDepth(-1);
				pm.getFetchPlan().setGroup(FetchPlan.ALL);
				encryptionCoordinateSet = pm.detachCopy(encryptionCoordinateSet);
				encryptionCoordinateSetID2EncryptionCoordinateSet.put(
						encryptionCoordinateSet.getEncryptionCoordinateSetID(), encryptionCoordinateSet
				);
				encryptionCoordinateString2EncryptionCoordinateSet.put(
						getEncryptionCoordinateString(encryptionCoordinateSet), encryptionCoordinateSet
				);
			}
		}
		return encryptionCoordinateSet;
	}

	protected EncryptionCoordinateSet _createOrGetEncryptionCoordinateSet(boolean create, PersistenceManagerConnection persistenceManagerConnection, String cipherTransformation, String macAlgorithm)
	{
		String encryptionCoordinateString = getEncryptionCoordinateString(cipherTransformation, macAlgorithm);
		EncryptionCoordinateSet encryptionCoordinateSet = encryptionCoordinateString2EncryptionCoordinateSet.get(encryptionCoordinateString);
		if (encryptionCoordinateSet == null) {
			PersistenceManager pm = persistenceManagerConnection.getDataPM();

			if (create)
				encryptionCoordinateSet = EncryptionCoordinateSet.createEncryptionCoordinateSet(pm, cipherTransformation, macAlgorithm);
			else
				encryptionCoordinateSet = EncryptionCoordinateSet.getEncryptionCoordinateSet(pm, cipherTransformation, macAlgorithm);

			if (encryptionCoordinateSet != null) {
				pm.getFetchPlan().setMaxFetchDepth(-1);
				pm.getFetchPlan().setGroup(FetchPlan.ALL);
				encryptionCoordinateSet = pm.detachCopy(encryptionCoordinateSet);
				encryptionCoordinateSetID2EncryptionCoordinateSet.put(
						encryptionCoordinateSet.getEncryptionCoordinateSetID(), encryptionCoordinateSet
				);
				encryptionCoordinateString2EncryptionCoordinateSet.put(
						getEncryptionCoordinateString(encryptionCoordinateSet), encryptionCoordinateSet
				);
			}
		}
		return encryptionCoordinateSet;
	}

	public synchronized EncryptionCoordinateSet getEncryptionCoordinateSet(PersistenceManagerConnection persistenceManagerConnection, String cipherTransformation, String macAlgorithm)
	{
		return _createOrGetEncryptionCoordinateSet(false, persistenceManagerConnection, cipherTransformation, macAlgorithm);
	}

	public synchronized EncryptionCoordinateSet createEncryptionCoordinateSet(PersistenceManagerConnection persistenceManagerConnection, String cipherTransformation, String macAlgorithm)
	{
		return _createOrGetEncryptionCoordinateSet(true, persistenceManagerConnection, cipherTransformation, macAlgorithm);
	}
}
