package org.cumulus4j.jee.test.ejb;

public class TestRollbackException extends RuntimeException {
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
