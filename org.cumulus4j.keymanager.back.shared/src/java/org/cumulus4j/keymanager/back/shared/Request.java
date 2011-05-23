package org.cumulus4j.keymanager.back.shared;

import java.io.Serializable;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public abstract class Request implements Serializable
{
	private static final long serialVersionUID = 1L;

	private UUID requestID;

	private String cryptoSessionID;

	public Request() { }

	public Request(String cryptoSessionID)
	{
		if (cryptoSessionID == null)
			throw new IllegalArgumentException("cryptoSessionID == null");

		this.requestID = UUID.randomUUID();
		this.cryptoSessionID = cryptoSessionID;
	}

	public UUID getRequestID() {
		return requestID;
	}

	public void setRequestID(UUID requestID) {
		this.requestID = requestID;
	}

	public String getCryptoSessionID() {
		return cryptoSessionID;
	}
	public void setCryptoSessionID(String cryptoSessionID) {
		this.cryptoSessionID = cryptoSessionID;
	}

	public String getCryptoSessionIDPrefix()
	{
		String id = cryptoSessionID;
		if (id == null)
			return null;

		int dotIdx = id.indexOf('.');
		if (dotIdx < 0)
			throw new IllegalStateException("cryptoSessionID does not contain a dot ('.')!!!");

		return id.substring(0, dotIdx);
	}

	@Override
	public String toString() {
		return super.toString() + '[' + requestID + ',' + cryptoSessionID + ']';
	}
}
