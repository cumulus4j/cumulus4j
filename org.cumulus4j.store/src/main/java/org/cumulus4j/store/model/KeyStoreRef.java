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
	 * Reserved value for {@link #getKeyStoreRefID() keyStoreRefID} meaning that the object referencing this <code>keyStoreRefID</code>
	 * is not related to any key-store, at all, but global.
	 */
	public static final int GLOBAL_KEY_STORE_REF_ID = -1;

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
	private Long keyStoreRefID;

	@Persistent(nullValue=NullValue.EXCEPTION)
	@Unique(name="KeyStoreRef_keyStoreID")
	@Column(length=255)
	private String keyStoreID;

	public int getKeyStoreRefID() {
		Long result = keyStoreRefID;
		if (result == null)
			return -666;

		if (result.longValue() > Integer.MAX_VALUE)
			throw new IllegalStateException("keyStoreRefID > Integer.MAX_VALUE :: " + result + " > " + Integer.MAX_VALUE);

		return result.intValue();
	}

	public String getKeyStoreID() {
		return keyStoreID;
	}

	@Override
	public int hashCode() {
		final int keyStoreRefID = getKeyStoreRefID();
		return keyStoreRefID;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		KeyStoreRef other = (KeyStoreRef) obj;
		return this.getKeyStoreRefID() == -666 ? false : this.getKeyStoreRefID() == other.getKeyStoreRefID();
	}
}
