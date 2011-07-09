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

	public ErrorResponse() { }

	public ErrorResponse(Request request, String errorMessage) {
		super(request);
		this.message = errorMessage;
	}

	public ErrorResponse(Request request, Throwable t) {
		super(request);
		this.type = t.getClass().getName();
		this.message = t.getMessage();
	}

	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}

	public String getMessage() {
		return message;
	}
	public void setMessage(String errorMessage) {
		this.message = errorMessage;
	}
}
