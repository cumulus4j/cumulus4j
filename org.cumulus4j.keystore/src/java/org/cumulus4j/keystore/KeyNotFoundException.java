package org.cumulus4j.keystore;

/**
 * Thrown by {@link KeyStore#getKey(String, char[], long)}, if a non-existent
 * key is requested.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyNotFoundException extends KeyStoreException
{
	private static final long serialVersionUID = 1L;

	public KeyNotFoundException() { }

	public KeyNotFoundException(String message) {
		super(message);
	}

	public KeyNotFoundException(Throwable cause) {
		super(cause);
	}

	public KeyNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
