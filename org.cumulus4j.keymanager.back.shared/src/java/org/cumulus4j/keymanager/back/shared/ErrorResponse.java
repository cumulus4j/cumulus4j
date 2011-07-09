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

import javax.xml.bind.annotation.XmlRootElement;

/**
 * {@link Request} implementation for sending an error back to the app-server.
 * It can optionally wrap a {@link Throwable} to provide more precise information
 * (the type) than just a message.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class ErrorResponse extends Response
{
	private static final long serialVersionUID = 1L;

	private String type;
	private String message;

	/**
	 * Create an empty instance of <code>ErrorResponse</code>.
	 * Only used for serialisation/deserialisation.
	 */
	public ErrorResponse() { }

	/**
	 * Create an instance of <code>ErrorResponse</code> in order to reply the given <code>request</code>.
	 *
	 * @param request the request to be replied.
	 * @param errorMessage a description of what went wrong.
	 */
	public ErrorResponse(Request request, String errorMessage) {
		super(request);
		this.message = errorMessage;
	}

	/**
	 * Create an instance of <code>ErrorResponse</code> in order to reply the given <code>request</code>.
	 *
	 * @param request the request to be replied.
	 * @param throwable the error to be wrapped and sent back to the app-server instead of a normal response.
	 */
	public ErrorResponse(Request request, Throwable throwable) {
		super(request);
		this.type = throwable.getClass().getName();
		this.message = throwable.getMessage();
	}

	/**
	 * Get the error-type. This is a fully qualified class-name of the {@link Throwable}-sub-class
	 * passed to {@link #ErrorResponse(Request, Throwable)} or {@link #setType(String) otherwise set}.
	 * @return the error-type or <code>null</code>.
	 * @see #setType(String)
	 */
	public String getType() {
		return type;
	}
	/**
	 * Set the error-type.
	 * @param type the error-type or <code>null</code>. If not <code>null</code>, this must be a fully
	 * qualified class-name of the {@link Throwable}-sub-class.
	 * @see #getType()
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * Get the error-message. If an exception was wrapped by this <code>ErrorResponse</code> instance,
	 * it will be the result of {@link Throwable#getMessage()}.
	 * @return the error-message.
	 * @see #setMessage(String)
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * Set the error-message, i.e. a description of what went wrong and prevented successful handling
	 * of the request.
	 * @param errorMessage the error message.
	 * @see #getMessage()
	 */
	public void setMessage(String errorMessage) {
		this.message = errorMessage;
	}
}
