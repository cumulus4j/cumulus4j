package org.cumulus4j.keystore;

public class UserDoesNotExistException extends KeyStoreException
{
	private static final long serialVersionUID = 1L;

	public UserDoesNotExistException() { }

	public UserDoesNotExistException(String message) {
		super(message);
	}

	public UserDoesNotExistException(Throwable cause) {
		super(cause);
	}

	public UserDoesNotExistException(String message, Throwable cause) {
		super(message, cause);
	}
}
