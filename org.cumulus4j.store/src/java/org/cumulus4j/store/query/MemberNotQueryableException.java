package org.cumulus4j.store.query;

import org.datanucleus.exceptions.NucleusUserException;

/**
 * Exception thrown when a field/property is being accessed in a Query and is (marked as) not queryable.
 */
public class MemberNotQueryableException extends NucleusUserException {

	/**
	 * @param message The message
	 */
	public MemberNotQueryableException(String message) {
		super(message);
	}
}
