package org.cumulus4j.keystore;

/**
 * Thrown, if an {@link KeyStore#isEmpty() empty} KeyStore is expected, but the key-store is
 * not empty.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyStoreNotEmptyException extends KeyStoreException
{
	private static final long serialVersionUID = 1L;

	public KeyStoreNotEmptyException() {
		// TODO Auto-generated constructor stub
	}

	public KeyStoreNotEmptyException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public KeyStoreNotEmptyException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public KeyStoreNotEmptyException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
