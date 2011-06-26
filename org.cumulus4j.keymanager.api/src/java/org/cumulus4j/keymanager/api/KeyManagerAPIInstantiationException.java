package org.cumulus4j.keymanager.api;

/**
 * Thrown if a {@link KeyManagerAPI} implementation cannot be instantiated.
 * This is usually the case, if the deployment is incomplete for the implementation
 * in question (and the arguments specified). See the document
 * <a href="http://cumulus4j.org/documentation/deployment-module-location-matrix.html">Deployment: Module-location-matrix</a>
 * for details about which module needs to be deployed where.
 *
 * @author Marco หงุ่ยตระกูล-Schulze - marco at nightlabs dot de
 */
public class KeyManagerAPIInstantiationException extends KeyManagerException
{
	private static final long serialVersionUID = 1L;

	public KeyManagerAPIInstantiationException() { }

	public KeyManagerAPIInstantiationException(String message) {
		super(message);
	}

	public KeyManagerAPIInstantiationException(Throwable cause) {
		super(cause);
	}

	public KeyManagerAPIInstantiationException(String message, Throwable cause) {
		super(message, cause);
	}
}
