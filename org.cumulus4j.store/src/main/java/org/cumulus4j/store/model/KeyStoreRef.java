package org.cumulus4j.store.model;

import javax.jdo.annotations.Column;
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

@PersistenceCapable(identityType=IdentityType.APPLICATION, detachable="true")
@Version(strategy=VersionStrategy.VERSION_NUMBER)
@Sequence(name="KeyStoreRefSequence", datastoreSequence="KeyStoreRefSequence", initialValue=0, strategy=SequenceStrategy.CONTIGUOUS)
@Queries({
	@Query(name="getKeyStoreRefByKeyStoreID", value="SELECT UNIQUE WHERE this.keyStoreID == :keyStoreID")
})
public class KeyStoreRef {

	/**
	 * Internal constructor. This exists only for JDO and should not be used by application code!
	 */
	protected KeyStoreRef() { }

	/**
	 * Create an instance of <code>DataEntry</code>.
	 * @param keyStoreID the <code>KeyStore</code>'s ID.
	 */
	public KeyStoreRef(String keyStoreID) {
		this.keyStoreID = keyStoreID;
	}

	@PrimaryKey
	@Persistent(valueStrategy=IdGeneratorStrategy.NATIVE, sequence="KeyStoreRefSequence")
	private int keyStoreRefID = -1;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Unique(name="KeyStoreRef_keyStoreID")
	@Column(length=255)
	private String keyStoreID;

	public int getKeyStoreRefID() {
		return keyStoreRefID;
	}

	public String getKeyStoreID() {
		return keyStoreID;
	}
}
