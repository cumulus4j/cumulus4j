package org.cumulus4j.keymanager.api;

public class KeyStoreNotEmptyException extends KeyManagerException
{
	private static final long serialVersionUID = 1L;

	public KeyStoreNotEmptyException() { }

	public KeyStoreNotEmptyException(String message, Throwable cause) {
		super(message, cause);
	}

	public KeyStoreNotEmptyException(String message) {
		super(message);
	}

	public KeyStoreNotEmptyException(Throwable cause) {
		super(cause);
	}
}
