package org.cumulus4j.store;

/**
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de (added javadoc)
 */
public class UnsupportedDataTypeException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public UnsupportedDataTypeException() { }

	public UnsupportedDataTypeException(String message) {
		super(message);
	}

	public UnsupportedDataTypeException(Throwable cause) {
		super(cause);
	}

	public UnsupportedDataTypeException(String message, Throwable cause) {
		super(message, cause);
	}
}
