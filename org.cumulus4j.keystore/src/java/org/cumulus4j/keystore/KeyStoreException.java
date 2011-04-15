package org.cumulus4j.keystore;

import java.security.GeneralSecurityException;

public class KeyStoreException extends GeneralSecurityException
{

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
