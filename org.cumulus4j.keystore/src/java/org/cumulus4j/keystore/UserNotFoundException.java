package org.cumulus4j.keystore;

/**
 * <p>
 * Thrown by the {@link KeyStore}, if a <code>userName</code> references a non-existent
 * user.
 * </p>
 * <p>
 * Note, that this does not apply to user-names used for authentication
 * (usually named <code>authUserName</code>), as this would cause an {@link AuthenticationException}
 * instead.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class UserNotFoundException extends KeyStoreException
{
	private static final long serialVersionUID = 1L;

	public UserNotFoundException() { }

	public UserNotFoundException(String message) {
		super(message);
	}

	public UserNotFoundException(Throwable cause) {
		super(cause);
	}

	public UserNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
