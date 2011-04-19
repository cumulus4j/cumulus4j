package org.cumulus4j.keystore;


public class LoginException extends KeyStoreException
{
	private static final long serialVersionUID = 1L;

	public LoginException() { }

	public LoginException(String msg) {
		super(msg);
	}

	public LoginException(Throwable cause) {
		super(cause);
	}

	public LoginException(String message, Throwable cause) {
		super(message, cause);
	}
}