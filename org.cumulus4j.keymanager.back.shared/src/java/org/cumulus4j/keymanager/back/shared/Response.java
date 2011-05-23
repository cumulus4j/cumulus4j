package org.cumulus4j.keymanager.back.shared;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public abstract class Response extends Message
{
	private static final long serialVersionUID = 1L;

	public Response() { }

	public Response(Request request)
	{
		if (request == null)
			throw new IllegalArgumentException("request == null");

		setRequestID(request.getRequestID());
	}

	@Override
	public String toString() {
		return super.toString() + '[' + getRequestID() + ']';
	}
}
