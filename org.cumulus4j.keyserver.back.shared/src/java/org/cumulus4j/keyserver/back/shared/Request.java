package org.cumulus4j.keyserver.back.shared;

import java.io.Serializable;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public abstract class Request implements Serializable
{
	private static final long serialVersionUID = 1L;

	private UUID requestID;

	private String keyServerID;

	public Request() { }

	public Request(String keyServerID)
	{
		if (keyServerID == null)
			throw new IllegalArgumentException("keyServerID == null");

		this.requestID = UUID.randomUUID();
	}

	public UUID getRequestID() {
		return requestID;
	}

	public void setRequestID(UUID requestID) {
		this.requestID = requestID;
	}

	public String getKeyServerID() {
		return keyServerID;
	}
	public void setKeyServerID(String keyServerID) {
		this.keyServerID = keyServerID;
	}


	@Override
	public String toString() {
		return super.toString() + '[' + requestID + ',' + keyServerID + ']';
	}
}
