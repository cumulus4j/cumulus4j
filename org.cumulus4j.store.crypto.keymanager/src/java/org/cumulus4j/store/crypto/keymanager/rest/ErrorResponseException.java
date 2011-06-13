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

package org.cumulus4j.store.crypto.keymanager.rest;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;

/**
 * Exception used to wrap and throw an {@link ErrorResponse}.
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class ErrorResponseException extends Exception
{
	private static final long serialVersionUID = 1L;

	private ErrorResponse errorResponse;

	/**
	 * Instantiate an <code>ErrorResponseException</code> for a given {@link ErrorResponse}.
	 * @param errorResponse the {@link ErrorResponse} to be wrapped and thrown.
	 */
	public ErrorResponseException(ErrorResponse errorResponse) {
		super(errorResponse == null ? null : errorResponse.getMessage());

		if (errorResponse == null)
			throw new IllegalArgumentException("errorResponse == null");

		this.errorResponse = errorResponse;
	}

	/**
	 * Get the {@link ErrorResponse} wrapped by this exception.
	 * @return the {@link ErrorResponse} wrapped by this exception.
	 */
	public ErrorResponse getErrorResponse() {
		return errorResponse;
	}

//	public ErrorResponseException() { }
//
//	public ErrorResponseException(String message) {
//		super(message);
//	}
//
//	public ErrorResponseException(Throwable cause) {
//		super(cause);
//	}
//
//	public ErrorResponseException(String message, Throwable cause) {
//		super(message, cause);
//	}

}
