package org.cumulus4j.store.model;

import javax.jdo.PersistenceManager;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NullValue;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Queries;
import javax.jdo.annotations.Query;
import javax.jdo.annotations.Sequence;
import javax.jdo.annotations.SequenceStrategy;
import javax.jdo.annotations.Unique;
import javax.jdo.annotations.Version;
import javax.jdo.annotations.VersionStrategy;

import org.cumulus4j.crypto.Cipher;
import org.cumulus4j.crypto.CryptoRegistry;
import org.cumulus4j.store.EncryptionCoordinateSetManager;
import org.cumulus4j.store.crypto.CryptoManager;
import org.cumulus4j.store.crypto.CryptoSession;

/**
 * <p>
 * Encryption coordinates used to encrypt a persistent record.
 * </p>
 * <p>
 * Via the {@link EncryptionCoordinateSetManager}, the {@link CryptoManager}
 * (or {@link CryptoSession}) implementation can map the {@link Cipher#getTransformation()
 * cipher-transformation} and other encryption-coordinates (e.g. the {@link #getMACAlgorithm() MAC algorithm})
 * to a number in order to save space in each persistent record.
 * </p>
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
@Sequence(name="EncryptionCoordinateSetSequence", datastoreSequence="EncryptionCoordinateSetSequence", initialValue=0, strategy=SequenceStrategy.CONTIGUOUS)
public class EncryptionCoordinateSet
{
	/**
	 * <p>
	 * Constant for deactivating the <a target="_blank" href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a>.
	 * </p>
	 * <p>
	 * <b>Important: Deactivating the MAC is dangerous!</b> Choose this value only, if you are absolutely
	 * sure that your {@link #getCipherTransformation() cipher-transformation} already
	 * provides authentication - like <a target="_blank" href="http://en.wikipedia.org/wiki/Galois/Counter_Mode">GCM</a>
	 * does for example.
	 * </p>
	 */
	public static final String MAC_ALGORITHM_NONE = "NONE";

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE, sequence="EncryptionCoordinateSetSequence")
	private int encryptionCoordinateSetID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String cipherTransformation;

	@Persistent(nullValue=NullValue.EXCEPTION)
	private String macAlgorithm;

	/**
	 * Create a new <code>EncryptionCoordinateSet</code>. This default constructor only exists
	 * for JDO and should never be used directly!
	 */
	protected EncryptionCoordinateSet() { }

	/**
	 * Create a new <code>EncryptionCoordinateSet</code>. Instead of using this constructor,
	 * you should use {@link #createEncryptionCoordinateSet(PersistenceManager, String, String)}!
	 *
	 * @param cipherTransformation the cipher-transformation.
	 * @param macAlgorithm the MAC-algorithm.
	 */
	protected EncryptionCoordinateSet(String cipherTransformation, String macAlgorithm)
	{
		if (cipherTransformation == null)
			throw new IllegalArgumentException("cipherTransformation == null");

		if (macAlgorithm == null)
			throw new IllegalArgumentException("macAlgorithm == null");

		this.cipherTransformation = cipherTransformation;
		this.macAlgorithm = macAlgorithm;
	}

	/**
	 * <p>
	 * Get the unique numeric identifier of this <code>EncryptionCoordinateSet</code>.
	 * </p>
	 * <p>
	 * Note: Implementors of {@link CryptoManager} (or {@link CryptoSession} respectively) might
	 * choose not to store the entire int value (4 bytes), but reduce the size. Every time the
	 * encryption configuration is changed, a new instance of this class is persisted. Restricting
	 * the size to 2 bytes, for example, still gives the administrator the possibility to change
	 * the configuration 65535 times - which is likely enough.
	 * </p>
	 *
	 * @return the unique numeric identifier (primary key).
	 */
	public int getEncryptionCoordinateSetID() {
		return encryptionCoordinateSetID;
	}

	/**
	 * Get the {@link Cipher#getTransformation() cipher-transformation} that identifies the encryption
	 * algorithm, the mode and the padding used to encrypt a record. The system usually passes
	 * this value to {@link CryptoRegistry#createCipher(String)}.
	 * @return the {@link Cipher#getTransformation() cipher-transformation}. Never <code>null</code>.
	 */
	public String getCipherTransformation() {
		return cipherTransformation;
	}
	/**
	 * <p>
	 * Get the <a target="_blank" href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a>-algorithm
	 * used to protect a record against corruption/manipulation.
	 * </p>
	 * <p>
	 * Implementors of {@link CryptoManager}/{@link CryptoSession} should take {@link #MAC_ALGORITHM_NONE}
	 * into account! If this value equals that constant, MAC calculation and storage should be skipped.
	 * </p>
	 * @return the <a target="_blank" href="http://en.wikipedia.org/wiki/Message_authentication_code">MAC</a>-algorithm.
	 */
	public String getMACAlgorithm() {
		return macAlgorithm;
	}
}
