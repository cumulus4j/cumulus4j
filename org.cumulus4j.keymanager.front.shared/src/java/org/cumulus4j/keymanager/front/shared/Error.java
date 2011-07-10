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
package org.cumulus4j.keymanager.front.shared;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * DTO for sending an error back to the app-server.
 * It can optionally wrap a {@link Throwable} to provide more precise information
 * (the type) than just a message.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
@XmlRootElement
public class Error implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String rootType;
	private String rootMessage;

	private String type;
	private String message;

	/**
	 * Create an empty instance of <code>Error</code>.
	 * Only used for serialisation/deserialisation.
	 */
	public Error() { }

	/**
	 * Create an instance of <code>Error</code> wrapping a {@link Throwable}.
	 * @param throwable the error to be wrapped and sent back to the client instead of a normal response.
	 */
	public Error(Throwable throwable) {
		this.type = throwable.getClass().getName();
		this.message = throwable.getMessage();

		Throwable r = throwable;
		while (r.getCause() != null)
			r = r.getCause();

		if (r != throwable) {
			this.rootType = r.getClass().getName();
			this.rootMessage = r.getMessage();
		}
	}

	/**
	 * Create an instance of <code>Error</code> with an error message.
	 * @param message the message describing what went wrong.
	 */
	public Error(String message) {
		this.message = message;
	}

	/**
	 * Get the fully qualified class-name of the root-{@link Throwable} in the exception's cause-chain.
	 * @return the fully qualified class-name of the root-{@link Throwable}. Can be <code>null</code>.
	 */
	public String getRootType() {
		return rootType;
	}
	/**
	 * Get the fully qualified class-name of the root-{@link Throwable} in the exception's cause-chain.
	 * @param rootType the fully qualified class-name of the root-{@link Throwable} or <code>null</code>.
	 */
	public void setRootType(String rootType) {
		this.rootType = rootType;
	}
	/**
	 * Get the root-{@link Throwable}'s exception-{@link Throwable#getMessage() message}.
	 * @return the message of the root-{@link Throwable} in the exception's cause-chain.
	 */
	public String getRootMessage() {
		return rootMessage;
	}
	/**
	 * Set the root-{@link Throwable}'s exception-{@link Throwable#getMessage() message}.
	 * @param rootMessage the message of the root-{@link Throwable} in the exception's cause-chain.
	 */
	public void setRootMessage(String rootMessage) {
		this.rootMessage = rootMessage;
	}
	/**
	 * Get the fully qualified class-name of the wrapped {@link Throwable}.
	 * @return the fully qualified class-name of the wrapped {@link Throwable}.
	 */
	public String getType() {
		return type;
	}
	/**
	 * Set the fully qualified class-name of the wrapped {@link Throwable}.
	 * @param type the fully qualified class-name of the wrapped {@link Throwable}.
	 */
	public void setType(String type) {
		this.type = type;
	}
	/**
	 * Get the error-message. If this <code>Error</code> wraps a {@link Throwable}, this is
	 * {@link Throwable#getMessage()}
	 * @return the error-message.
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * Set the error-message.
	 * @param message the error-message.
	 */
	public void setMessage(String message) {
		this.message = message;
	}
}
