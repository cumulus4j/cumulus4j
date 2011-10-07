package org.cumulus4j.keymanager.api;

/**
 * Wrapper for {@link org.cumulus4j.keystore.AuthenticationException}.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class AuthenticationException extends KeyManagerException
{
	private static final long serialVersionUID = 1L;

	public AuthenticationException() { }

	public AuthenticationException(String message) {
		super(message);
	}

	public AuthenticationException(Throwable cause) {
		super(cause);
	}

	public AuthenticationException(String message, Throwable cause) {
		super(message, cause);
	}
}
