package org.cumulus4j.keystore;

/**
 * Thrown by any method of {@link KeyStore} which requires authentication,
 * if authentication fails. Authentication fails, if no user exists in the
 * <code>KeyStore</code> whose name equals the specified <code>authUserName</code> or the specified
 * <code>authPassword</code> is not correct for the specified user.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class AuthenticationException extends KeyStoreException
{
	private static final long serialVersionUID = 1L;

	public AuthenticationException() { }

	public AuthenticationException(String msg) {
		super(msg);
	}

	public AuthenticationException(Throwable cause) {
		super(cause);
	}

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
}
