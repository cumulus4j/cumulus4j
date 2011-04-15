package org.cumulus4j.keystore;

public class NotEmptyException extends KeyStoreException
{
	private static final long serialVersionUID = 1L;

	public NotEmptyException() { }

	public NotEmptyException(String message) {
		super(message);
	}

	public NotEmptyException(Throwable cause) {
		super(cause);
	}

	public NotEmptyException(String message, Throwable cause) {
		super(message, cause);
	}

}
