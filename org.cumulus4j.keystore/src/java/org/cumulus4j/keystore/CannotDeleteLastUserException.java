package org.cumulus4j.keystore;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class CannotDeleteLastUserException extends KeyStoreException
{
	private static final long serialVersionUID = 1L;

	public CannotDeleteLastUserException() { }

	public CannotDeleteLastUserException(String message) {
		super(message);
	}

	public CannotDeleteLastUserException(Throwable cause) {
		super(cause);
	}

	public CannotDeleteLastUserException(String message, Throwable cause) {
		super(message, cause);
	}
}
