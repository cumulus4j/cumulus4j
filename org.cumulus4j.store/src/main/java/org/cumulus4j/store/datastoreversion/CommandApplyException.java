package org.cumulus4j.store.datastoreversion;

public class CommandApplyException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public CommandApplyException() { }

	public CommandApplyException(String message) {
		super(message);
	}

	public CommandApplyException(Throwable cause) {
		super(cause);
	}

	public CommandApplyException(String message, Throwable cause) {
		super(message, cause);
	}

}
