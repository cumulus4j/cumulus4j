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

	public Error() { }

	public Error(Throwable t) {
		this.type = t.getClass().getName();
		this.message = t.getMessage();

		Throwable r = t;
		while (r.getCause() != null)
			r = r.getCause();

		if (r != t) {
			this.rootType = r.getClass().getName();
			this.rootMessage = r.getMessage();
		}
	}

	public Error(String message) {
		this.message = message;
	}

	public String getRootType() {
		return rootType;
	}
	public void setRootType(String rootType) {
		this.rootType = rootType;
	}
	public String getRootMessage() {
		return rootMessage;
	}
	public void setRootMessage(String rootMessage) {
		this.rootMessage = rootMessage;
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
	public void setMessage(String message) {
		this.message = message;
	}
}
