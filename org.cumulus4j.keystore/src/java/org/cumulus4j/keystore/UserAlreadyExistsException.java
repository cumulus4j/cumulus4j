package org.cumulus4j.keystore;

/**
 * Thrown by {@link KeyStore#createUser(String, char[], String, char[])},
 * if a user with the same <code>userName</code> already exists.
 *
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
