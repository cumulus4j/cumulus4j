package org.cumulus4j.jee.test.ejb;

import javax.transaction.SystemException;

public class TestRollbackException extends SystemException {
	private static final long serialVersionUID = 1L;

	public TestRollbackException() { }

	public TestRollbackException(String message) {
		super(message);
	}

	public TestRollbackException(Throwable cause) {
		super(cause);
	}

	public TestRollbackException(String message, Throwable cause) {
		super(message, cause);
	}
}
