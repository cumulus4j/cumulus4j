package org.cumulus4j.store.crypto.keymanager.rest;

import org.cumulus4j.keymanager.back.shared.ErrorResponse;

public class ErrorResponseException extends Exception
{
	private static final long serialVersionUID = 1L;

	private ErrorResponse errorResponse;

	public ErrorResponseException(ErrorResponse errorResponse) {
		super(errorResponse == null ? null : errorResponse.getMessage());

		if (errorResponse == null)
			throw new IllegalArgumentException("errorResponse == null");

		this.errorResponse = errorResponse;
	}

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
