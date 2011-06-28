package org.cumulus4j.keymanager.api;

import org.cumulus4j.keystore.KeyStore;

/**
 * <p>
 * Thrown, if an {@link KeyStore#isEmpty() empty} <code>KeyStore</code> is expected, but the key-store is
 * not empty. For example, some key-store-initialisations can only be done, if the key-store is empty (i.e. new).
 * </p><p>
 * This is a wrapper for {@link org.cumulus4j.keystore.KeyStoreNotEmptyException}.
 * </p>
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyStoreNotEmptyException extends KeyManagerException
{
	private static final long serialVersionUID = 1L;

	public KeyStoreNotEmptyException() { }

	public KeyStoreNotEmptyException(String message, Throwable cause) {
		super(message, cause);
	}

	public KeyStoreNotEmptyException(String message) {
		super(message);
	}

	public KeyStoreNotEmptyException(Throwable cause) {
		super(cause);
	}
}
