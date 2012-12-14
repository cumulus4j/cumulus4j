package org.cumulus4j.keymanager.api;

/**
 * Base class for all checked exceptions thrown by {@link KeyManagerAPI} (and related API).
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyManagerException extends Exception
{
	private static final long serialVersionUID = 1L;

	public KeyManagerException() { }

	public KeyManagerException(String message) {
		super(message);
	}

	public KeyManagerException(Throwable cause) {
		super(cause);
	}

	public KeyManagerException(String message, Throwable cause) {
		super(message, cause);
	}
}
