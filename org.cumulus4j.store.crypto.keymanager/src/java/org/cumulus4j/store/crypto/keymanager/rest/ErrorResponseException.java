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
