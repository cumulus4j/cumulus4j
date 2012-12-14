package org.cumulus4j.store;

public class Cumulus4jException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public Cumulus4jException() { }

	public Cumulus4jException(String message) {
		super(message);
	}

	public Cumulus4jException(Throwable cause) {
		super(cause);
	}

	public Cumulus4jException(String message, Throwable cause) {
		super(message, cause);
	}

}
