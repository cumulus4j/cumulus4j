package org.cumulus4j.store.crypto.keymanager.messagebroker;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 * @deprecated Currently not used anymore. This class might disappear or it might be used again, later.
 */
@Deprecated
public class ActiveKeyManagerChannelRegistration
implements Serializable
{
	private static final long serialVersionUID = 1L;

	private UUID id = UUID.randomUUID();
	private String clusterNodeID;
	private String cryptoSessionIDPrefix;

	public ActiveKeyManagerChannelRegistration(String clusterNodeID, String cryptoSessionIDPrefix)
	{
		if (clusterNodeID == null)
			throw new IllegalArgumentException("clusterNodeID == null");

		if (cryptoSessionIDPrefix == null)
			throw new IllegalArgumentException("cryptoSessionIDPrefix == null");

		this.clusterNodeID = clusterNodeID;
		this.cryptoSessionIDPrefix = cryptoSessionIDPrefix;
	}

	public String getClusterNodeID() {
		return clusterNodeID;
	}
	public String getCryptoSessionIDPrefix() {
		return cryptoSessionIDPrefix;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActiveKeyManagerChannelRegistration other = (ActiveKeyManagerChannelRegistration) obj;
		return this.id.equals(other.id);
	}

	@Override
	public String toString() {
		return super.toString() + '[' + id + ']';
	}
}
