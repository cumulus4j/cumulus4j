package org.cumulus4j.keystore;

/**
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
