package org.cumulus4j.keystore;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class UserAlreadyExistsException extends KeyStoreException
{
	private static final long serialVersionUID = 1L;

	public UserAlreadyExistsException() { }

	public UserAlreadyExistsException(String message) {
		super(message);
	}

	public UserAlreadyExistsException(Throwable cause) {
		super(cause);
	}

	public UserAlreadyExistsException(String message, Throwable cause) {
		super(message, cause);
	}
}
