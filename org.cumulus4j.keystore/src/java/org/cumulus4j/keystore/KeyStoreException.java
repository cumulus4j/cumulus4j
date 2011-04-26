package org.cumulus4j.keystore;

import java.security.GeneralSecurityException;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyStoreException extends GeneralSecurityException
{
	private static final long serialVersionUID = 1L;

	public KeyStoreException() {
	}

	public KeyStoreException(String message) {
		super(message);
	}

	public KeyStoreException(Throwable cause) {
		super(cause);
	}

	public KeyStoreException(String message, Throwable cause) {
		super(message, cause);
	}

}
