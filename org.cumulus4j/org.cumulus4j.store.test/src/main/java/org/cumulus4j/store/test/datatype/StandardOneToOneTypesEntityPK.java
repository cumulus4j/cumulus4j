package org.cumulus4j.store.test.datatype;

import java.io.Serializable;
import java.util.UUID;

public class StandardOneToOneTypesEntityPK implements Serializable {

	private static final long serialVersionUID = 1L;

	public UUID uuid;

	public StandardOneToOneTypesEntityPK() { }

	public StandardOneToOneTypesEntityPK(String s) {
		uuid = UUID.fromString(s);
	}

	@Override
	public String toString() {
		return uuid.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((uuid == null) ? 0 : uuid.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StandardOneToOneTypesEntityPK other = (StandardOneToOneTypesEntityPK) obj;
		if (uuid == null) {
			if (other.uuid != null)
				return false;
		} else if (!uuid.equals(other.uuid))
			return false;
		return true;
	}

}
