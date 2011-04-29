package org.cumulus4j.keystore;

/**
 * Thrown by {@link KeyStore#deleteUser(String, char[], String)}, if
 * an attempt is made to delete the last user. Deleting the last user
 * would cause all data in the <code>KeyStore</code> to be lost,
 * hence this operation is not permitted.
 *
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
