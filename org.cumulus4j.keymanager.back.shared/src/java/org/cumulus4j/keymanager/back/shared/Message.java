package org.cumulus4j.keymanager.back.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * <p>
 * Base-type for {@link Request} and {@link Response}.
 * </p>
 * <p>
 * There should not be any other direct sub-classes of this class besides <code>Request</code> and
 * <code>Response</code>.
 * </p>
 * <p>
 * We implement a
 * <a href="http://en.wikipedia.org/wiki/Request-response">request-response</a>
 * <a href="http://en.wikipedia.org/wiki/Messaging_pattern">messaging-pattern</a>,
 * hence for every <code>Request</code> instance,
 * there must be exactly one <code>Response</code> instance. Both are identified
 * by the {@link #getRequestID() requestID}
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public abstract class Message implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String requestID;

	public Message() { }

	/**
	 * Get the request-identifier. Because a {@link Response} belongs
	 * to a {@link Request} in a 1-1-relationship, both use the same <code>requestID</code>.
	 * @return the identifier of the request.
	 * @see #setRequestID(String)
	 */
	public String getRequestID() {
		return requestID;
	}

	/**
	 * Set the request-identifier.
	 * @param requestID the identifier of the request.
	 * @see #getRequestID()
	 */
	public void setRequestID(String requestID) {
		this.requestID = requestID;
	}

	@Override
	public int hashCode()
	{
		return (requestID == null) ? 0 : requestID.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		Message other = (Message) obj;
		return (
				this.requestID == other.requestID ||
				(this.requestID != null && this.requestID.equals(other.requestID))
		);
	}
}
