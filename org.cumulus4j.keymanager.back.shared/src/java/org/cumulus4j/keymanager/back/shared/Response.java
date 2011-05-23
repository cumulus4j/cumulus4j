package org.cumulus4j.keymanager.back.shared;

import java.io.Serializable;
import java.util.UUID;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public abstract class Response implements Serializable
{
	private static final long serialVersionUID = 1L;

	private UUID requestID;

	public Response() { }

	public Response(Request request)
	{
		if (request == null)
			throw new IllegalArgumentException("request == null");

		this.requestID = request.getRequestID();
	}

	public UUID getRequestID() {
		return requestID;
	}

	public void setRequestID(UUID requestID) {
		this.requestID = requestID;
	}

}
