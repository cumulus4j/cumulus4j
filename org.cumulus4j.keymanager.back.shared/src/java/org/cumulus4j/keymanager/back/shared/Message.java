/*
 * Cumulus4j - Securing your data in the cloud - http://cumulus4j.org
 * Copyright (C) 2011 NightLabs Consulting GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
 * <a target="_blank" href="http://en.wikipedia.org/wiki/Request-response">request-response</a>
 * <a target="_blank" href="http://en.wikipedia.org/wiki/Messaging_pattern">messaging-pattern</a>,
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
