package org.cumulus4j.store.model;

import javax.jdo.JDOObjectNotFoundException;
import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;
import javax.jdo.identity.IntIdentity;

/**
 * Encryption coordinates used to encrypt a persistent record.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@Unique(name="EncryptionCoordinateSet_allAlgorithms", members={"cipherTransformation", "macAlgorithm"})
@Queries({
	@Query(
			name="getEncryptionCoordinateSetByAllAlgorithms",
			value="SELECT UNIQUE WHERE this.cipherTransformation == :cipherTransformation && this.macAlgorithm == :macAlgorithm"
	)
})
public class EncryptionCoordinateSet
{
	public static EncryptionCoordinateSet getEncryptionCoordinateSet(PersistenceManager pm, int encryptionCoordinateSetID)
	{
		IntIdentity id = new IntIdentity(EncryptionCoordinateSet.class, encryptionCoordinateSetID);
		try {
			EncryptionCoordinateSet encryptionCoordinateSet = (EncryptionCoordinateSet) pm.getObjectById(id);
			return encryptionCoordinateSet;
		} catch (JDOObjectNotFoundException x) {
			return null;
		}
	}

	public static EncryptionCoordinateSet getEncryptionCoordinateSet(PersistenceManager pm, String cipherTransformation, String macAlgorithm)
	{
		javax.jdo.Query q = pm.newNamedQuery(EncryptionCoordinateSet.class, "getEncryptionCoordinateSetByAllAlgorithms");
		return (EncryptionCoordinateSet) q.execute(cipherTransformation, macAlgorithm);
		// UNIQUE query does not need to be closed, because there is no result list lingering.
	}

	public static EncryptionCoordinateSet createEncryptionCoordinateSet(PersistenceManager pm, String cipherTransformation, String macAlgorithm)
	{
		EncryptionCoordinateSet encryptionCoordinateSet = getEncryptionCoordinateSet(pm, cipherTransformation, macAlgorithm);
		if (encryptionCoordinateSet == null)
			encryptionCoordinateSet = pm.makePersistent(new EncryptionCoordinateSet(cipherTransformation, macAlgorithm));

		return encryptionCoordinateSet;
	}

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE)
	private int encryptionCoordinateSetID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String cipherTransformation;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String macAlgorithm;

	protected EncryptionCoordinateSet() { }

	protected EncryptionCoordinateSet(String cipherTransformation, String macAlgorithm)
	{
		if (cipherTransformation == null)
			throw new IllegalArgumentException("cipherTransformation == null");

		if (macAlgorithm == null)
			throw new IllegalArgumentException("macAlgorithm == null");

		this.cipherTransformation = cipherTransformation;
		this.macAlgorithm = macAlgorithm;
	}

	public int getEncryptionCoordinateSetID() {
		return encryptionCoordinateSetID;
	}

	public String getCipherTransformation() {
		return cipherTransformation;
	}
	public String getMacAlgorithm() {
		return macAlgorithm;
	}
}
